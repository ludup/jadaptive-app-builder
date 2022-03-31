package com.jadaptive.api.auth;

import com.jadaptive.api.user.User;

public interface AuthenticationPolicyService {

	AuthenticationPolicy getAssignedPolicy(User user);

}
