package com.jadaptive.api.app;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.pf4j.ExtensionPoint;

public interface PluginWebSocketHandler extends ExtensionPoint {

	boolean handles(String handler);

	void handleError(Throwable exception);

	void connectionClosed(String reason);

	void handleBinaryMessage(ByteBuffer payload, int payloadLength) throws IOException;

	void handleTextMessage(String payload, int payloadLength) throws IOException;

	void connectionOpened(WebSocketClient websocket) throws IOException;

}
