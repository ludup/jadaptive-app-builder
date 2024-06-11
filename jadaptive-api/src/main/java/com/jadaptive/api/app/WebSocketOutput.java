package com.jadaptive.api.app;

import java.io.Closeable;

public interface WebSocketOutput extends Closeable {

	void receive(byte[] data);

	@Override
	void close();

}
