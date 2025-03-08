package com.jadaptive.api.auth.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.servlet.Request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class OAuth2DiscoveryController {
	public static final String PATH_PREFIX = ".well-known/oauth2-configuration";
	
	static final Logger LOG = LoggerFactory.getLogger(OAuth2DiscoveryController.class);
	
	@Autowired
	private OAuth2DiscoveryService discoveryService;

	@RequestMapping(value = OAuth2DiscoveryController.PATH_PREFIX, method = RequestMethod.GET, produces = "text/json")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public OAuth2Discovery discovery(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return discoveryService.discover(Request.getThisHost(request));
	}
}
