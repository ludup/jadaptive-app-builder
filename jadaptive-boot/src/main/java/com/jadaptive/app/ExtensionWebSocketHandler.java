package com.jadaptive.app;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.jadaptive.api.app.WebSocketOutput;

public class ExtensionWebSocketHandler implements WebSocketHandler {

	static Logger log = LoggerFactory.getLogger(ExtensionWebSocketHandler.class);
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		StandardWebSocketSession s = (StandardWebSocketSession) session;
		String path = s.getUri().getPath().substring(8);
		String handler = path.indexOf('/') > -1 ? path.substring(0, path.indexOf('/')) : path;

		for(PluginWebSocketHandler wshandler : applicationService.getBeans(PluginWebSocketHandler.class)) {
			if(wshandler.handles(handler)) {
				PluginWebSocketClient client;
				session.getAttributes().put("handler", wshandler);
				session.getAttributes().put("client", client = new PluginWebSocketClient(s));
				wshandler.connectionOpened(client);
			}
		}
		
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		PluginWebSocketClient client = (PluginWebSocketClient) session.getAttributes().get("client");
		if(Objects.nonNull(handler)) {
			if(message instanceof BinaryMessage) {
				BinaryMessage msg = (BinaryMessage) message;
				handler.handleBinaryMessage(client, msg.getPayload(), message.getPayloadLength());
			} else if(message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;
				handler.handleTextMessage(client, msg.getPayload(), msg.getPayloadLength());
			} else {
				throw new IOException("Unsupported message type");
			}	
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		PluginWebSocketClient client = (PluginWebSocketClient) session.getAttributes().get("client");
		
		if(Objects.nonNull(handler)) {
			handler.handleError(client, exception);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		PluginWebSocketClient client = (PluginWebSocketClient) session.getAttributes().get("client");
		if(Objects.nonNull(handler)) {
			handler.connectionClosed(client, closeStatus.getReason());
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	
	class PluginWebSocketClient implements WebSocketClient {
		
		StandardWebSocketSession session;
		WebSocketOutput attachment;
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
		public void close() {
			if(log.isInfoEnabled()) {
				log.info("Closing websocket");
			}
			try {
				session.close();
			} catch (IOException e) {
			}
			if(Objects.nonNull(attachment)) {
				attachment.close();
			}
		}

		@Override
		public void setAttachment(WebSocketOutput attachment) {
			this.attachment = attachment;
		}

		@Override
		public WebSocketOutput getAttachment() {
			return attachment;
		}
	}
}
