package com.jadaptive.app.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.user.User;

@Service
public class AuthenticationPolicyServiceImpl implements AuthenticationPolicyService {

	
	@Autowired
	private AssignableObjectDatabase<AuthenticationPolicy> policyDatabase;

	@Override
	public AuthenticationPolicy getAssignedPolicy(User user) {
		
		List<AuthenticationPolicy> results =  new ArrayList<>();
		
		for(AuthenticationPolicy policy : policyDatabase.getAssignedObjects(AuthenticationPolicy.class, user)) {
			results.add(policy);
		}
		
		Collections.sort(results, new Comparator<AuthenticationPolicy>() {

			@Override
			public int compare(AuthenticationPolicy o1, AuthenticationPolicy o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
		
		if(results.isEmpty()) {
			throw new IllegalStateException("Login not allowed.");
		}
		
		return results.get(0);
	}

}
