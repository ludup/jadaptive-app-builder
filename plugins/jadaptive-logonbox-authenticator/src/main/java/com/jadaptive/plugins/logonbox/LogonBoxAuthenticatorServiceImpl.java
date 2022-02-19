package com.jadaptive.plugins.logonbox;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.plugins.logonbox.ui.Authenticator;
import com.jadaptive.plugins.logonbox.ui.AuthenticatorStart;

@Service
public class LogonBoxAuthenticatorServiceImpl implements LogonBoxAuthenticatorService {

	@Autowired
	private AuthenticationService authenticationService; 
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage("logonbox-directory", AuthenticatorStart.class);
	}
}
