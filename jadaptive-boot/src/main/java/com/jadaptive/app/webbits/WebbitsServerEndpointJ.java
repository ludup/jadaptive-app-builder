package com.jadaptive.app.webbits;

import javax.websocket.server.ServerEndpoint;

import com.codesmith.webbits.io.WebbitsEndpointConfiguration;
import com.codesmith.webbits.io.WebbitsServerEndpoint;

@ServerEndpoint(value = "/app/ui/_webbits_ws", configurator = WebbitsEndpointConfiguration.class)
public class WebbitsServerEndpointJ extends WebbitsServerEndpoint{

}
