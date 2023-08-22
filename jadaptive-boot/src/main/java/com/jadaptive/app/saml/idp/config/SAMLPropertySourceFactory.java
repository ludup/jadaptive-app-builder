package com.jadaptive.app.saml.idp.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class SAMLPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) 
      throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        try {
        	Properties properties = factory.getObject();
        	return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
        } catch(Throwable e) {
        	return new PropertiesPropertySource("saml.yml", new Properties());
        }
    }
}