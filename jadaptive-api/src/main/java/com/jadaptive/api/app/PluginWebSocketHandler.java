package com.jadaptive.api.app;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.pf4j.ExtensionPoint;

public interface PluginWebSocketHandler extends ExtensionPoint {

	boolean handles(String handler);

	void handleError(WebSocketClient websocket, Throwable exception);

	void connectionClosed(WebSocketClient websocket, String reason);

	void handleBinaryMessage(WebSocketClient websocket, ByteBuffer payload, int payloadLength) throws IOException;

	void handleTextMessage(WebSocketClient websocket, String payload, int payloadLength) throws IOException;

	void connectionOpened(WebSocketClient websocket) throws IOException;

}
