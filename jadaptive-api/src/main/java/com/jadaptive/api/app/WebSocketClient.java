package com.jadaptive.api.app;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.List;
import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpSession;

public interface WebSocketClient<T extends WebSocketOutput> extends Closeable {

	void sendTextMessage(String text) throws IOException;
	
	void sendTextMessage(CharBuffer text) throws IOException;

	void sendBinaryMessage(byte[] payload) throws IOException;

	void sendBinaryMessage(ByteBuffer payload) throws IOException;
	
	void close();

	List<String> getParameters(String name);

	String getParameter(String name);

	void setAttachment(T channel);
	
	T getAttachment();
	
	void doInSession(RunnableWithIOException r) throws IOException;
	
	<X> X doInSession(CallableWithIOException<X> r) throws IOException;
	
	@FunctionalInterface
	public interface RunnableWithIOException {
		void run(HttpSession session) throws IOException;
	}
	
	@FunctionalInterface
	public interface CallableWithIOException<X> {
		X run(HttpSession session) throws IOException;
	}

}
