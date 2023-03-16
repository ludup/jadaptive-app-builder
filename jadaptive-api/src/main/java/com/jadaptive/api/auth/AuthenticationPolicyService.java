package com.jadaptive.api.auth;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;
import com.jadaptive.api.user.User;

public interface AuthenticationPolicyService extends AbstractUUIDObjectService<AuthenticationPolicy>, DynamicColumnService  {

	AuthenticationPolicy getAssignedPolicy(User user, String ipAddress, Class<? extends AuthenticationPolicy> clz, AuthenticationPolicy... additionalPolicies);

	AuthenticationPolicy getDefaultPolicy();
	
	void setResolver(AuthenticationPolicyResolver resolver);

	AuthenticationPolicy getPasswordResetPolicy();

	boolean hasPasswordResetPolicy();

}
