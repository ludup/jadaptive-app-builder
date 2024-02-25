package com.jadaptive.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.stereotype.Component;

import com.jadaptive.app.TomcatSSLConfiguration.DefaultSSLConnectorCustomizer;
import com.jadaptive.utils.TomcatUtil;

/**
 * NOTE: This will not be needed when we update to Spring Boot 3.2+, it can
 * do it's own SSL configuration reloading
 */
@Component
@Deprecated
public class TomcatUtilImpl implements TomcatUtil {

    public static final String DEFAULT_SSL_HOSTNAME_CONFIG_NAME = "_default_";

	static Logger LOG = LoggerFactory.getLogger(TomcatUtilImpl.class);

    private ServletWebServerFactory servletWebServerFactory;

    public TomcatUtilImpl(ServletWebServerFactory servletWebServerFactory) {
        this.servletWebServerFactory = servletWebServerFactory;
    }

    @Override
    public void reloadSSLHostConfig() {

        var tomcatFactoty = (TomcatServletWebServerFactory) servletWebServerFactory;
        var customizers = tomcatFactoty.getTomcatConnectorCustomizers();
        for (var tomcatConnectorCustomizer : customizers) {

            if (tomcatConnectorCustomizer instanceof DefaultSSLConnectorCustomizer) {
                var customizer = (DefaultSSLConnectorCustomizer) tomcatConnectorCustomizer;
                var protocol = customizer.getProtocol();
                try {
                    protocol.reloadSslHostConfig(DEFAULT_SSL_HOSTNAME_CONFIG_NAME);
                    LOG.info("Reloaded SSL host configuration");
                } catch (IllegalArgumentException e) {
                	LOG.warn("Cannot reload SSL host configuration", e);
                }
            }
        }

    }
}