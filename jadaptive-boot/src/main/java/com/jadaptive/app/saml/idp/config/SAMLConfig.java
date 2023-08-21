package com.jadaptive.app.saml.idp.config;
import java.util.Collection;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;

import com.jadaptive.app.saml.idp.filters.CustomIDPAuthenticationRequestFilter;
import com.jadaptive.app.saml.idp.filters.CustomIDPInitiatedLoginFilter;

@Configuration
public class SAMLConfig extends SamlIdentityProviderServerBeanConfiguration {
    
	@Autowired
	private final SAMLProperties config;

    public SAMLConfig(@Qualifier("samlProperties") SAMLProperties config) {
        this.config = config;
    }

    @Override
    protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
        return config;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        Collection<IDPUserDetails> allUsers = UserUtils.getAllUserLoginDetails();
        return new IDPInMemoryUserDetailsManager(allUsers);
    }

    @Bean
    public Filter idpInitatedLoginFilter() {
        return new CustomIDPInitiatedLoginFilter(getSamlProvisioning(), samlAssertionStore());
    }


    @Bean
    public Filter idpAuthnRequestFilter() {
        return new CustomIDPAuthenticationRequestFilter(getSamlProvisioning(), samlAssertionStore());
    }
}