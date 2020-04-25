package com.jadaptive.plugins.email;

import javax.cache.Cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.Utils;

@Service
public class EmailVerificationServiceImpl extends AuthenticatedService implements EmailVerificationService {

	static Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

	
	@Autowired
	private CacheService cacheService; 
	
	@Autowired
	private UserService userService; 
	
	@Override
	public boolean verifyEmail(String email) {


		try {
			userService.getUserByEmail(email);
			return true;
		} catch (EntityNotFoundException e) {
		
		}
		
		String code;
		
		if(Boolean.getBoolean("jadaptive.development")) {
			code = "123456";
		} else {
			code = Utils.generateRandomAlphaNumericString(6).toUpperCase();
		}

		/**
		 * TODO send message
		 */
		
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
