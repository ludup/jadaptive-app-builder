package com.jadaptive.api.auth.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;

@Service
public class OAuth2DiscoveryServiceImpl implements OAuth2DiscoveryService {
	
	@Autowired
	private App applicationService;

	@Override
	public OAuth2Discovery discover(String requestHostname) {
		return new OAuth2Discovery(requestHostname, applicationService);
	}
}
