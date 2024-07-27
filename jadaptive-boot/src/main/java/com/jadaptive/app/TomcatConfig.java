package com.jadaptive.app;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.http.port:-0}")
    private int httpPort;
    
    public int getHttpPort() {
		return httpPort;
	}


	@Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        if(httpPort > 0) {
	        connector.setPort(httpPort);
	        factory.addAdditionalTomcatConnectors(connector);
        }
    }
}