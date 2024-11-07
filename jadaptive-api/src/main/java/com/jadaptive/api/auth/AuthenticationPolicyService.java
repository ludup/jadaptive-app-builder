package com.jadaptive.api.auth;

import java.util.Collection;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.user.User;

public interface AuthenticationPolicyService extends AbstractUUIDObjectService<AuthenticationPolicy>, DynamicColumnService  {

	AuthenticationPolicy getAssignedPolicy(User user, String ipAddress, Class<? extends AuthenticationPolicy> scope, AuthenticationPolicy... additionalPolicies);

	AuthenticationPolicy getDefaultPolicy(Class<? extends AuthenticationPolicy> scope);
	
	void setResolver(AuthenticationPolicyResolver resolver);

	boolean hasPolicy(String resourceKey);

	Iterable<AuthenticationPolicy> getAssignedPolicies(User currentUser);

}
