package com.jadaptive.plugins.web.ui;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.ui.AuthenticationFlow;
import com.jadaptive.api.ui.Page;

@Service
public class WebUserInterfaceServiceImpl implements WebUserInterfaceService {
	
	public static final String PASSWORD_RESOURCE_KEY = "password";
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage(PASSWORD_RESOURCE_KEY, Login.class);
		
		authenticationService.registerDefaultAuthenticationFlow(new AuthenticationFlow() {

			@Override
			public List<Class<? extends Page>> getAuthenticators() {
				return Arrays.asList(Login.class);
			}
			
		});
	}

}

