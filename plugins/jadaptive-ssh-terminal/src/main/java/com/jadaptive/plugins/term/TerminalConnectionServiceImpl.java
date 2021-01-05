package com.jadaptive.plugins.term;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.jadaptive.api.app.WebSocketClient;

@Service
public class TerminalConnectionServiceImpl implements TerminalConnectionService {

	Map<String,SshClientConnector> connectors = new HashMap<>();
	
	@Override
	public TerminalConnection createConnection(String sessionId, WebSocketClient websocket) throws IOException{
		

		SshClientConnector connector = connectors.get(sessionId);
		if(Objects.isNull(connector)) {
			connector = new DebugSshClientConnector();
			//throw new IOException(String.format("%s is not a valid session ID", sessionId));
		}
	
		return new SshTerminalConnection(websocket, connector, sessionId);
		
		
	}

}
