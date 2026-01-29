package com.jadaptive.api.auth.oauth2;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.session.UnauthorizedException;

@Service
public class OAuth2TokenServiceImpl extends AbstractUUIDObjectServceImpl<OAuth2Token>
		implements OAuth2TokenService {

	public static final String RESOURCE_BUNDLE = "OAuth2TokenService";

	@Override
	protected Class<OAuth2Token> getResourceClass() {
		return OAuth2Token.class;
	}

	@Override
	public OAuth2Token byRefreshToken(String refreshToken) throws AccessDeniedException {
		return objectDatabase.get(OAuth2Token.class, SearchField.eq("refreshToken", refreshToken));
	}

	@Override
	public OAuth2Token byToken(String token) throws AccessDeniedException, UnauthorizedException {
		var tokenObj = objectDatabase.get(OAuth2Token.class, SearchField.eq("name", token));
		if(tokenObj.isExpired())
			throw new TokenExpiredException();
		return tokenObj;
	}

	@Override
	public OAuth2Token authenticate(String authentication, OAuth2Scope scope)
			throws AccessDeniedException, UnauthorizedException, ResponseEntityException {
		if(Objects.nonNull(authentication) &&  authentication.startsWith("Bearer ")) {
			var token = byToken(authentication.substring(7).trim());
			for(String scopeName : token.getScopes()) {
				if(scopeName.equals(scope.getId())) {
					return token;
				}
			}
		}
		throw new ResponseEntityException(new ResponseEntity<>(new OAuth2ErrorResponse("invalid_request", "Missing authentication"), HttpStatus.BAD_REQUEST));
	}


}
