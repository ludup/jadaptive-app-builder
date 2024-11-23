/*******************************************************************************
 * Copyright (c) 2019 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package com.jadaptive.api.auth.oauth2;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.session.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2TokenService  extends AbstractUUIDObjectService<OAuth2Token> {

	OAuth2Token byRefreshToken(String refreshToken) throws AccessDeniedException;

	/**
	 * Get a token only if it is still valid and not expired, otherwise {@link UnauthorizedException}
	 * should be thrown. This would be the normal method that scope controllers
	 * use to check their bearer tokens.
	 * 
	 * @param token  
	 * @return token object
	 * @throws AccessDeniedException if access denied
	 * @throws UnauthorizedException if token is expired
	 */
	OAuth2Token byToken(String token)
			throws AccessDeniedException, UnauthorizedException;

	/**
	 * Get a token given a bearer token (probably supplied from the Authentication
	 * HTTP header.).
	 *  
	 * @param authentication
	 * @param scope the scope executing. This will be checked against the scope the token requires.
	 * @return token
	 * @throws AccessDeniedException if access denied
	 * @throws UnauthorizedException if token is expired
	 * @throws ResponseEntityException other error
	 */
	OAuth2Token authenticate(String authentication, OAuth2Scope scope)
			throws AccessDeniedException, UnauthorizedException, ResponseEntityException;
	
	/**
	 * Get a token given a bearer token (probably supplied from the Authentication
	 * HTTP header). If the token is invalid, the exception will be caught and a 400 
	 * error response, along with the WWW-Authenticate header will be returned. All
	 * other error types will be caught an an error response constructed.  
	 *  
	 * @param request
	 * @param authentication
	 * @param scope the scope executing. This will be checked against the scope the token requires.
	 * @return token
	 * @throws AccessDeniedException if access denied
	 * @throws UnauthorizedException if token is expired
	 * @throws ResponseEntityException other error
	 */
	default OAuth2Token authenticateRequest(HttpServletRequest request, HttpServletResponse response, String authentication, OAuth2Scope scope)
			throws  AccessDeniedException, UnauthorizedException, ResponseEntityException {
		try {
			return authenticate(authentication, scope);
		}
		catch(TokenExpiredException tpe) {
			response.addHeader("WWW-Authentication", "Bearer error=\"invalid_token\" error_description=\"The access token expired\"");
			throw new ResponseEntityException(new ResponseEntity<>(new OAuth2ErrorResponse("invalid_token", "The access token expired"), HttpStatus.BAD_REQUEST));
		}
		
	}

}
