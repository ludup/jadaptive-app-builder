package com.jadaptive.app.saml.idp.config;
import static org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityDsl.identityProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityConfiguration;

@EnableWebSecurity
public class SecurityConfig {

    @Configuration
    @Order(1)
    public static class SamlSecurity extends SamlIdentityProviderSecurityConfiguration {

        private final SAMLProperties appProperties;
        private final SAMLConfig samlConfig;

        public SamlSecurity(SAMLConfig samlConfig, @Qualifier("samlProperties") SAMLProperties appProperties) {
            super("/saml/idp", samlConfig);
            this.appProperties = appProperties;
            this.samlConfig = samlConfig;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.
                    userDetailsService(samlConfig.userDetailsService())
                    .formLogin();

            http.
                    apply(identityProvider())
                    .configure(appProperties);
        }
    }

    @Configuration
    @AutoConfigureAfter(SecurityAutoConfiguration.class)
    public static class AppSecurity extends WebSecurityConfigurerAdapter {

        private final SAMLConfig samlConfig;

        public AppSecurity(SAMLConfig samlConfig) {
            this.samlConfig = samlConfig;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/**")
                    .authorizeRequests()
                    .antMatchers("/**").authenticated()
                    .and()
                    .userDetailsService(samlConfig.userDetailsService()).formLogin()
            ;
        }
    }


}