package com.jadaptive.api.app;

public interface WebSocketOutput {

	void receive(byte[] data);

	void close();

}
