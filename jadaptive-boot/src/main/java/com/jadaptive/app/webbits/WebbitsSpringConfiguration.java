package com.jadaptive.app.webbits;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.codesmith.webbits.io.WebbitsIoEndpoint;
import com.codesmith.webbits.io.WebbitsServerEndpoint;

@Configuration
public class WebbitsSpringConfiguration {
    @Bean
    public ServerEndpointExporter endpointExporter() {
	ServerEndpointExporter sex = new ServerEndpointExporter();
	sex.setAnnotatedEndpointClasses(WebbitsServerEndpoint.class, WebbitsIoEndpoint.class);
	return sex;
    }
}
