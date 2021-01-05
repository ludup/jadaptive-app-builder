package com.jadaptive.plugins.term;

import java.io.IOException;

import com.jadaptive.api.app.WebSocketClient;

public interface TerminalConnectionService {

	TerminalConnection createConnection(String sessionId, WebSocketClient websocket) throws IOException;

}
