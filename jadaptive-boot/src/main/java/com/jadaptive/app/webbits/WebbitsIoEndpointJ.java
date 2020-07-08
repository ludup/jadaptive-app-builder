package com.jadaptive.app.webbits;

import javax.websocket.server.ServerEndpoint;

import com.codesmith.webbits.io.WebbitsEndpointConfiguration;
import com.codesmith.webbits.io.WebbitsIoEndpoint;

@ServerEndpoint(value = "/app/ui/_webbits_io/", configurator = WebbitsEndpointConfiguration.class)
public class WebbitsIoEndpointJ extends WebbitsIoEndpoint {

}
