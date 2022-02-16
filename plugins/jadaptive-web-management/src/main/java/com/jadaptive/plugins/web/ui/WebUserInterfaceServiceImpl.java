package com.jadaptive.plugins.web.ui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.ui.AuthenticationFlow;
import com.jadaptive.api.ui.Page;

@Service
public class WebUserInterfaceServiceImpl implements WebUserInterfaceService {
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage(AuthenticationService.USERNAME_AND_PASSWORD, Login.class);
		
		authenticationService.registerDefaultAuthenticationFlow(new AuthenticationFlow() {

			@Override
			public List<Class<? extends Page>> getAuthenticators() {
				
				String[] authenticators = ApplicationProperties.getValue(
						AuthenticationService.DEFAULT_AUTHENTICATION_FLOW, 
							AuthenticationService.USERNAME_AND_PASSWORD).split(",");
				List<Class<? extends Page>> results = new ArrayList<>();
				for(String authenticator : authenticators) {
					results.add(authenticationService.getAuthenticationPage(authenticator));
				}
				return results;
			}
			
		});
	}

}

