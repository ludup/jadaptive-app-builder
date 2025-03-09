package com.jadaptive.app.auth.oauth2;

import java.util.Optional;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Extension
@Controller
public class GetUserDetailsScope implements OAuth2Scope, PluginController {

	@Autowired
	private OAuth2TokenService tokenService;

	public GetUserDetailsScope() {
	}

	@Override
	public String getId() {
		return "getUserDetails";
	}

	@RequestMapping(value = "oauth2/user", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public User getUserDetails(HttpServletRequest request, HttpServletResponse response,
			@RequestHeader("Authorization") String authentication) throws Exception {
		return tokenService.authenticate(authentication, this).getOwner();
	}

	@Override
	public void verifyPermissions(Optional<OAuth2Request> oauthRequest, User principal) throws AccessDeniedException {
	}
}
