package com.jadaptive.app;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }
}