package com.jadaptive.plugins.email;

import javax.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codesmith.webbits.Request;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.StaticResolver;
import com.jadaptive.utils.Utils;

@Service
public class EmailVerificationServiceImpl extends AuthenticatedService implements EmailVerificationService {

	static Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

	public final static String USER_REGISTRATION_CONFIRMATION_CODE = "9a1fe4b0-e0c4-4b9e-a9e2-913b25fb39df";
	
	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private MessageService messageService; 
	
	@Override
	public boolean verifyEmail(String email) {

		try {
			userService.getUserByEmail(email);
			return true;
		} catch (ObjectNotFoundException e) {
		}
		
		String code;
		
		if(Boolean.getBoolean("jadaptive.development")) {
			code = "123456";
		} else {
			code = Utils.generateRandomAlphaNumericString(6).toUpperCase();
		}

		StaticResolver data = new StaticResolver();
		data.addToken("code", code);
		data.addToken("hostname", Request.get().underlyingRequest().getServerName());
		
		messageService.sendMessage(USER_REGISTRATION_CONFIRMATION_CODE, data, email);
		
		Cache<String,String> codes = cacheService.getCacheOrCreate("emailVerification", String.class, String.class);
		log.info("Registration code is {}", code);
		codes.put(email, code);
		return false;
	}
	
	@Override
	public boolean assertCode(String email, String code) throws AccessDeniedException {
		Cache<String,String> codes = cacheService.getCacheOrCreate("emailVerification", String.class, String.class);
		if(codes.containsKey(email)) {
			return codes.get(email).equalsIgnoreCase(code);
		}
		return false;
	}
}
