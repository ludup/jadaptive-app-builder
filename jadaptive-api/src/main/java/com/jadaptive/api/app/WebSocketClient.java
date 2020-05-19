package com.jadaptive.api.app;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;

public interface WebSocketClient extends Closeable {

	void sendTextMessage(String text) throws IOException;
	
	void sendTextMessage(CharBuffer text) throws IOException;

	void sendBinaryMessage(byte[] payload) throws IOException;

	void sendBinaryMessage(ByteBuffer payload) throws IOException;
	
	void close() throws IOException;

	List<String> getParameters(String name);

	String getParameter(String name);

	

}
