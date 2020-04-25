package com.jadaptive.plugins.email;

import com.jadaptive.api.permissions.AccessDeniedException;

public interface EmailVerificationService {

	boolean verifyEmail(String email);

	boolean assertCode(String email, String code) throws AccessDeniedException;

}
