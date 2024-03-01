package com.jadaptive.app;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.session.StandardSession;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.app.session.SessionStatusFilter;

@Configuration(proxyBeanMethods = false)
public class TomcatConfiguration {
	
	@Bean
	public ServletWebServerFactory servletContainer() {
		var tomcat = new TomcatServletWebServerFactory();
		
		/* This is (along with SessionStatusFilter) is all for just preventing a single URL
		 * from updating the last access time. There is probably a better way, but damned
		 * if i can find it! */
		tomcat.addContextLifecycleListeners(new LifecycleListener() {

			@Override
			public void lifecycleEvent(LifecycleEvent event) {
				if(event.getType().equals("before_init") && event.getSource() instanceof Context ctx) {
                    ctx.setLoginConfig(new LoginConfig("NONE", null, null, null));
				}
			}
			
		});
		tomcat.addContextCustomizers(new TomcatContextCustomizer() {

			public void customize(Context context) {
				context.setManager(new StandardManager() {
					@SuppressWarnings("serial")
					@Override
				    protected StandardSession getNewSession() {
				        return new StandardSession(this) {

				        	private boolean prevented = true;
				        	
							@Override
							public void access() {
								prevented = SessionStatusFilter.isPreventAccess(Request.get());
								if(!prevented) {
									super.access();
								}
							}

							@Override
							public void endAccess() {
								try {
									if(!prevented)
										super.endAccess();
								}
								finally {
									prevented = false;
								}
							}
				        	
				        };
				    }
				});
			}
		});
		
		/* This is for SSL reloading */
		tomcat.addConnectorCustomizers(new DefaultSSLConnectorCustomizer());
		return tomcat;
	}

	/**
	 * NOTE: This will not be needed when we update to Spring Boot 3.2+, it can
	 * do it's own SSL configuration reloading
	 */
	@Deprecated
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