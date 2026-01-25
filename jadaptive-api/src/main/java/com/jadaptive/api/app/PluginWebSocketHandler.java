package com.jadaptive.api.app;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.pf4j.ExtensionPoint;

public interface PluginWebSocketHandler<T extends WebSocketOutput> extends ExtensionPoint {

	boolean handles(String handler);

	void handleError(WebSocketClient<T> websocket, Throwable exception);

	void connectionClosed(WebSocketClient<T> websocket, String reason);

	void handleBinaryMessage(WebSocketClient<T> websocket, ByteBuffer payload, int payloadLength) throws IOException;

	void handleTextMessage(WebSocketClient<T> websocket, String payload, int payloadLength) throws IOException;

	void connectionOpened(WebSocketClient<T> websocket) throws IOException;

}
