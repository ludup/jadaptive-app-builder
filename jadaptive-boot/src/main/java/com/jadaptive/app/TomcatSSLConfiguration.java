package com.jadaptive.app;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NOTE: This will not be needed when we update to Spring Boot 3.2+, it can
 * do it's own SSL configuration reloading
 */
@Configuration(proxyBeanMethods = false)
@Deprecated
public class TomcatSSLConfiguration {

	@Bean
	public ServletWebServerFactory servletContainer() {

		var tomcat = new TomcatServletWebServerFactory();
		tomcat.addConnectorCustomizers(new DefaultSSLConnectorCustomizer());
		return tomcat;
	}

	public static class DefaultSSLConnectorCustomizer implements TomcatConnectorCustomizer {

		private Http11NioProtocol protocol;

		@Override
		public void customize(Connector connector) {

			var protocol = (Http11NioProtocol) connector.getProtocolHandler();
			if (connector.getSecure()) {
				this.protocol = protocol;
			}
		}

		protected Http11NioProtocol getProtocol() {
			return protocol;
		}
	}
}