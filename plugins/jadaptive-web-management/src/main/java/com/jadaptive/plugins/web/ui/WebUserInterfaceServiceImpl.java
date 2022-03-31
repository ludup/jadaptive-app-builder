package com.jadaptive.plugins.web.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;

@Service
public class WebUserInterfaceServiceImpl implements WebUserInterfaceService {
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage(AuthenticationService.PASSWORD, Password.class);
	}

}

