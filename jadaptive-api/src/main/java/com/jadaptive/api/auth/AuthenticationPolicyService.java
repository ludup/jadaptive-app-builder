package com.jadaptive.api.auth;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.user.User;

public interface AuthenticationPolicyService extends AbstractUUIDObjectService<AuthenticationPolicy>{

	AuthenticationPolicy getAssignedPolicy(User user);

}
