package com.jadaptive.api.auth;

import java.util.List;

import com.jadaptive.api.user.User;

public interface AuthenticationPolicyResolver {

	List<AuthenticationPolicy> resolveUserPolicy(User user, List<AuthenticationPolicy> policies);

	void assertDefaultPolicy(AuthenticationPolicy policy);

}
