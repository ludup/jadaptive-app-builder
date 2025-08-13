package com.jadaptive.app;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.PluginWebSocketHandler;
import com.jadaptive.api.app.WebSocketClient;
import com.jadaptive.api.app.WebSocketOutput;
import com.jadaptive.api.session.SessionUtils;

import jakarta.servlet.http.HttpSession;

public class ExtensionWebSocketHandler implements WebSocketHandler, HandshakeInterceptor {

	static Logger log = LoggerFactory.getLogger(ExtensionWebSocketHandler.class);
	
	static final String HTTP_SESSION = "httpSession";
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		HttpSession httpSession = (HttpSession) session.getAttributes().get(HTTP_SESSION);
		
		if(Objects.isNull(httpSession)) {
			onConnect(session);
		} else {
			App.bean(SessionUtils.class).doInSession(httpSession, ()->{
				onConnect(session);
			});
		}
		
		
	}

	private void onConnect(WebSocketSession session) throws IOException {
		StandardWebSocketSession s = (StandardWebSocketSession) session;
		String path = s.getUri().getPath().substring(8);
		String handler = path.indexOf('/') > -1 ? path.substring(0, path.indexOf('/')) : path;

		for(PluginWebSocketHandler wshandler : App.beans(PluginWebSocketHandler.class)) {
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
		
		HttpSession httpSession = (HttpSession) session.getAttributes().get(HTTP_SESSION);
		
		if(Objects.isNull(httpSession)) {
			onMessage(session, message);
		} else {
			App.bean(SessionUtils.class).doInSession(httpSession, ()->{
				onMessage(session, message);
			});
		}
	}

	private void onMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
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
		
		HttpSession httpSession = (HttpSession) session.getAttributes().get(HTTP_SESSION);
		
		if(Objects.isNull(httpSession)) {
			onError(session, exception);
		} else {
			App.bean(SessionUtils.class).doInSession(httpSession, ()->{
				onError(session, exception);
			});
		}
	}

	private void onError(WebSocketSession session, Throwable exception) {
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		PluginWebSocketClient client = (PluginWebSocketClient) session.getAttributes().get("client");
		
		if(Objects.nonNull(handler)) {
			handler.handleError(client, exception);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
		HttpSession httpSession = (HttpSession) session.getAttributes().get(HTTP_SESSION);
		
		if(Objects.isNull(httpSession)) {
			onClose(session, closeStatus);
		} else {
			App.bean(SessionUtils.class).doInSession(httpSession, ()->{
				onClose(session, closeStatus);
			});
		}
	}

	private void onClose(WebSocketSession session, CloseStatus closeStatus) {
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


	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		
		if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession(false); // `false` means don't create a new session if one doesn't exist

            if (session != null) {
                attributes.put(HTTP_SESSION, session);
            } 
        }
        return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		
	}
}
