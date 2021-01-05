package com.jadaptive.plugins.term;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.PluginWebSocketHandler;
import com.jadaptive.api.app.WebSocketClient;

@Extension
public class TerminalWebsocket implements PluginWebSocketHandler {

	static Logger log = LoggerFactory.getLogger(TerminalWebsocket.class);
	
	@Autowired
	TerminalConnectionService connectionService; 
	
	public boolean handles(String handler) {
		return handler.equals("term");
	}

	@Override
	public void handleError(WebSocketClient websocket, Throwable exception) {
		exception.printStackTrace();
		websocket.close();
	}

	@Override
	public void connectionClosed(WebSocketClient websocket, String reason) {
		websocket.close();
	}

	@Override
	public void handleBinaryMessage(WebSocketClient websocket, ByteBuffer payload, int payloadLength) throws IOException {
		byte[] data = new byte[payloadLength];
		payload.get(data);
		websocket.getAttachment().receive(data);
	}

	@Override
	public void handleTextMessage(WebSocketClient websocket, String payload, int payloadLength) throws IOException {
		log.error("Unexpected text payload in terminal websocket");
	}

	@Override
	public void connectionOpened(WebSocketClient websocket) throws IOException {
		String sessionId = websocket.getParameter("session");
		
		TerminalConnection con = connectionService.createConnection(sessionId, websocket);
		websocket.setAttachment(con);
		
	}
}
