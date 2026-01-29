package com.jadaptive.app.tomcat;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import com.jadaptive.api.app.ApplicationProperties;

@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.http.port:-0}")
    private int httpPort;
    
    public int getHttpPort() {
		return httpPort;
	}


	@Override
    public void customize(TomcatServletWebServerFactory factory) {
		// 1. This handles the primary connector AND the additional one
	    factory.addConnectorCustomizers(connector -> {
	        configureConnector(connector);
	    });
	    
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        configureConnector(connector);
        if(httpPort > 0) {
	        connector.setPort(httpPort);
	        factory.addAdditionalTomcatConnectors(connector);
        }
        
        factory.addProtocolHandlerCustomizers(new TomcatProtocolHandlerCustomizer<Http11NioProtocol>() {

			@Override
			public void customize(Http11NioProtocol protocolHandler) {
				protocolHandler.setSslImplementationName(CustomSSLImplementation.class.getCanonicalName());
			}
        	
        });
   
    }

	private void configureConnector(Connector connector) {
		connector.setMaxParameterCount(ApplicationProperties.getValue("jadaptive.maxParameterCount", 10000));
        connector.setMaxPartCount(ApplicationProperties.getValue("jadaptive.maxPartCount", 100));
	}
}