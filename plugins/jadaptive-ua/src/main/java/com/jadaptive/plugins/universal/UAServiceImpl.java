package com.jadaptive.plugins.universal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;

@Service
public class UAServiceImpl implements UAService {

	
	@Autowired
	private AuthenticationService authenticationService;
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage("ua", UniversalAuthentication.class);
	}
}
