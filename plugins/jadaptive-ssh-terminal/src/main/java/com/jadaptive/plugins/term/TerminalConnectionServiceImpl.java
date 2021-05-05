package com.jadaptive.plugins.term;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.WebSocketClient;

@Service
public class TerminalConnectionServiceImpl implements TerminalConnectionService {

	static Logger log = LoggerFactory.getLogger(TerminalConnectionServiceImpl.class);
	
	Map<String,SshClientConnector> connectors = new HashMap<>();
	
	@Override
	public TerminalConnection createConnection(String sessionId, WebSocketClient websocket) throws IOException{
		
		if(log.isInfoEnabled()) {
			log.info("Created connection for session {}", sessionId);
		}
		
		SshClientConnector connector = connectors.get(sessionId);
		if(Objects.isNull(connector)) {
			throw new IOException(String.format("%s is not a valid session identifier", sessionId));
		}
	
		return new SshTerminalConnection(websocket, connector, sessionId);
		
		
	}
	
	@Override
	public String registerConnection(SshClientConnector connector) {
		
		String sessionId = UUID.randomUUID().toString();
		
		if(log.isInfoEnabled()) {
			log.info("Registering connector for session {}", sessionId);
		}
		connectors.put(sessionId, connector);
		return sessionId;
	}

}
