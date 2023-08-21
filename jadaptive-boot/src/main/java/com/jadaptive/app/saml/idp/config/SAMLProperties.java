package com.jadaptive.app.saml.idp.config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.saml.provider.SamlServerConfiguration;

@ConfigurationProperties(prefix = "saml2")
@Configuration(value = "samlProperties")
@PropertySource(value = "file:./conf.d/saml.yml", factory = SAMLPropertySourceFactory.class)
public class SAMLProperties extends SamlServerConfiguration {

}