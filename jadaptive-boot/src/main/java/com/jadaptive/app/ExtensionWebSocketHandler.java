package com.jadaptive.app;

import java.io.IOException;
import java.net.URI;
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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.J;
import com.jadaptive.api.app.PluginWebSocketHandler;
import com.jadaptive.api.app.WebSocketClient;
import com.jadaptive.api.app.WebSocketClient.RunnableWithIOException;
import com.jadaptive.api.app.WebSocketOutput;
import com.jadaptive.api.session.SessionUtils;

import jakarta.servlet.http.HttpSession;

public class ExtensionWebSocketHandler implements WebSocketHandler, HandshakeInterceptor {

	static Logger log = LoggerFactory.getLogger(ExtensionWebSocketHandler.class);
	
	static final String HTTP_SESSION = "httpSession";
	
	
	private void doInSessionMaybe(@NonNull WebSocketSession socket, RunnableWithIOException r) throws IOException {
		/**
		 * LDP = HttpSession does not mean a user is logged on? Check the JAD session for state.
		 */
		HttpSession httpSession = (HttpSession) socket.getAttributes().get(HTTP_SESSION);
		if(Objects.isNull(httpSession) || !J.b(SessionUtils.class).isLoggedOn()) {
			r.run(httpSession);
		} else {
			J.b(SessionUtils.class).doInSession(httpSession, ()->{
				r.run(httpSession);
			});
		}
	}
	@Override
	public void afterConnectionEstablished(@NonNull WebSocketSession socket) throws Exception {

		doInSessionMaybe(socket, (session)->{
			onConnect(socket);
		});
		
		
	}

	private void onConnect(WebSocketSession session) throws IOException {
		StandardWebSocketSession s = (StandardWebSocketSession) session;
		URI uri = s.getUri();
		if(uri==null) {
			throw new IOException("Unexpected null URI");
		}
 		String path = uri.getPath().substring(8);
		String handler = path.indexOf('/') > -1 ? path.substring(0, path.indexOf('/')) : path;

		for(PluginWebSocketHandler<?> wshandler : App.beans(PluginWebSocketHandler.class)) {
			if(wshandler.handles(handler)) {
				PluginWebSocketClient client;
				session.getAttributes().put("handler", wshandler);
				session.getAttributes().put("client", client = new PluginWebSocketClient(s));
				wshandler.connectionOpened(client);
			}
		}
	}

	@Override
	public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws Exception {
		
		doInSessionMaybe(session, (httpSession)->{
			onMessage(session, message);
		});

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
	public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
		
		doInSessionMaybe(session, (httpSession)->{
			onError(session, exception);
		});
	}

	private void onError(WebSocketSession session, Throwable exception) {
		PluginWebSocketHandler handler = (PluginWebSocketHandler) session.getAttributes().get("handler");
		PluginWebSocketClient client = (PluginWebSocketClient) session.getAttributes().get("client");
		
		if(Objects.nonNull(handler)) {
			handler.handleError(client, exception);
		}
	}

	@Override
	public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) throws Exception {
		
		doInSessionMaybe(session, (httpSession)->{
			onClose(session, closeStatus);
		});
	
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

	
	class PluginWebSocketClient<T extends WebSocketOutput> implements WebSocketClient<T> {
		
		StandardWebSocketSession session;
		T attachment;
		public PluginWebSocketClient(StandardWebSocketSession session) {
			this.session = session;
		}
		
		public void doInSession(RunnableWithIOException r) throws IOException {
			doInSessionMaybe((WebSocketSession)session, r);
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
		public void setAttachment(T attachment) {
			this.attachment = attachment;
		}

		@Override
		public T getAttachment() {
			return attachment;
		}
	}


	@Override
	public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler,
			@NonNull Map<String, Object> attributes) throws Exception {
		
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
	public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler,
			@Nullable Exception exception) {
		
	}
}
