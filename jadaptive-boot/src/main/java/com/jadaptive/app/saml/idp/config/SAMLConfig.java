package com.jadaptive.app.saml.idp.config;
import java.util.ArrayList;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;
import org.springframework.security.saml.saml2.attribute.Attribute;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.saml.idp.filters.CustomIDPAuthenticationRequestFilter;
import com.jadaptive.app.saml.idp.filters.CustomIDPInitiatedLoginFilter;

@Configuration
public class SAMLConfig extends SamlIdentityProviderServerBeanConfiguration {
    
	@Autowired
	private final SAMLProperties config;

	@Autowired
	private UserService userService; 
	
    public SAMLConfig(@Qualifier("samlProperties") SAMLProperties config) {
        this.config = config;
    }

    @Override
    protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
        return config;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new MappedUserDetailsManager();
    }

    @Bean
    public Filter idpInitatedLoginFilter() {
        return new CustomIDPInitiatedLoginFilter(getSamlProvisioning(), samlAssertionStore());
    }

	@Bean
    public Filter idpAuthnRequestFilter() {
        return new CustomIDPAuthenticationRequestFilter(getSamlProvisioning(), samlAssertionStore());
    }
    
    
    class MappedUserDetailsManager implements UserDetailsManager {

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			User user = userService.getUser(username);
			return new IDPUserDetails(user, new ArrayList<Attribute>());
		}

		@Override
		public void createUser(UserDetails user) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void updateUser(UserDetails user) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteUser(String username) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void changePassword(String oldPassword, String newPassword) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean userExists(String username) {
			try {
				userService.getUser(username);
				return true;
			} catch(ObjectNotFoundException e) {
				return false;
			}
		}
    	
    }
}