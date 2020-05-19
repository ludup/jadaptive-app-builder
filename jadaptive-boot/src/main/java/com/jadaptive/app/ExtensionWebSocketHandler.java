package com.jadaptive.app;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.PluginWebSocketHandler;
import com.jadaptive.api.app.WebSocketClient;

public class ExtensionWebSocketHandler implements WebSocketHandler {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		StandardWebSocketSession s = (StandardWebSocketSession) session;
		String path = s.getUri().getPath().substring(8);
		String handler = path.indexOf('/') > -1 ? path.substring(0, path.indexOf('/')) : path;

		for(PluginWebSocketHandler wshandler : applicationService.getBeans(PluginWebSocketHandler.class)) {
			if(wshandler.handles(handler)) {
				session.getAttributes().put("handler", wshandler);
				wshandler.connectionOpened(new PluginWebSocketClient(s));
			}
		}
		
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		if(Objects.nonNull(handler)) {
			if(message instanceof BinaryMessage) {
				BinaryMessage msg = (BinaryMessage) message;
				handler.handleBinaryMessage(msg.getPayload(), message.getPayloadLength());
			} else if(message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				handler.handleTextMessage(msg.getPayload(), msg.getPayloadLength());
			} else {
				throw new IOException("Unsupported message type");
			}	
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		if(Objects.nonNull(handler)) {
			handler.handleError(exception);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		if(Objects.nonNull(handler)) {
			handler.connectionClosed(closeStatus.getReason());
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	
	class PluginWebSocketClient implements WebSocketClient {
		
		StandardWebSocketSession session;
		public PluginWebSocketClient(StandardWebSocketSession session) {
			this.session = session;
		}
		
		@Override
		public List<String> getParameters(String name) {
			return session.getNativeSession().getRequestParameterMap().get(name);
		}
		
		@Override
		public String getParameter(String name) {
			List<String> values = getParameters(name);
			if(values.isEmpty()) {
				return null;
			}
			return values.get(0);
		}
		
		@Override
		public void sendTextMessage(String text) throws IOException {
			session.sendMessage(new TextMessage(text));
		}
		
		@Override
		public void sendTextMessage(CharBuffer text) throws IOException {
			session.sendMessage(new TextMessage(text));
		}
		
		@Override
		public void sendBinaryMessage(byte[] payload) throws IOException {
			session.sendMessage(new BinaryMessage(payload));
		}
		
		@Override
		public void sendBinaryMessage(ByteBuffer payload) throws IOException {
			session.sendMessage(new BinaryMessage(payload));
		}
		
		@Override
		public void close() throws IOException {
			session.close();
		}
	}
}
