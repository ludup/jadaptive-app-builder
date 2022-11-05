package com.jadaptive.api.auth;

import java.util.List;

import com.jadaptive.api.user.User;

@FunctionalInterface
public interface AuthenticationPolicyResolver {

	List<AuthenticationPolicy> resolveUserPolicy(User user, List<AuthenticationPolicy> policies);

}
