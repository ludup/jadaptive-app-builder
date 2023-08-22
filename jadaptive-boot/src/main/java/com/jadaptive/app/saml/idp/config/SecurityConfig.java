package com.jadaptive.app.saml.idp.config;
import static org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityDsl.identityProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityConfiguration;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.app.saml.idp.filters.CustomLogoutFilter;

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
            http.userDetailsService(samlConfig.userDetailsService())
                    .formLogin()
                    .loginPage("/app/ui/login")
                    .and()
                    .logout()
                    .addLogoutHandler(ApplicationServiceImpl.getInstance().getBean(CustomLogoutFilter.class));

            http.apply(identityProvider())
                    .configure(appProperties);
        }
    }
}