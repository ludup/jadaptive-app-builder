package com.jadaptive.app.auth;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyResolver;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.PasswordResetAuthenticationPolicy;
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.CIDRUtils;

@Service
public class AuthenticationPolicyServiceImpl extends AbstractUUIDObjectServceImpl<AuthenticationPolicy> implements AuthenticationPolicyService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationPolicyService.class);
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private AssignableObjectDatabase<AuthenticationPolicy> policyDatabase;

	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private PermissionService permissionService;
	
	private AuthenticationPolicyResolver resolver;
	
	@Override
	public AuthenticationPolicy getAssignedPolicy(User user, String ipAddress, Class<? extends AuthenticationPolicy> clz, AuthenticationPolicy... additionalPolicies) {
		
		List<AuthenticationPolicy> results =  new ArrayList<>();
		
		for(AuthenticationPolicy policy : policyDatabase.getAssignedObjectsA(AuthenticationPolicy.class, user)) {
			if(!clz.isAssignableFrom(policy.getClass())) {
				continue;
			}
			boolean update = false;
			for(AuthenticationPolicy additional : additionalPolicies) {
				if(StringUtils.isNotBlank(additional.getUuid())
						&& additional.getUuid().equals(policy.getUuid())) {
					update = true;
					break;
				}
			}
			if(!update) {
				if(assertIPAddress(ipAddress, policy)) {
					results.add(policy);
				}
			}
		}
		
		for(AuthenticationPolicy additional : additionalPolicies) {
			if(!clz.isAssignableFrom(additional.getClass())) {
				continue;
			}
			if(additional.getUsers().contains(user)
					|| roleService.hasRole(user, additional.getRoles())) {
				if(assertIPAddress(ipAddress, additional)) {
					results.add(additional);
				}
			}
		}
		
		Collections.sort(results, new Comparator<AuthenticationPolicy>() {

			@Override
			public int compare(AuthenticationPolicy o1, AuthenticationPolicy o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
		
		if(Objects.nonNull(resolver)) {
			results = resolver.resolveUserPolicy(user, results);
		}
		
		if(results.isEmpty()) {
			if(permissionService.isAdministrator(user)) {
				return getDefaultPolicy();
			}
			return null;
		}
		
		return results.get(0);
	}
	
	protected boolean assertIPAddress(String remoteAddress, AuthenticationPolicy policy ) {
		
		boolean assertion = !policy.getAllowedIPs().isEmpty();
		
		if(assertion) {
			for(String address : policy.getAllowedIPs()) {
				try {
					if(matchesAddress(address, remoteAddress)) {
						return true;
					}
				} catch (UnknownHostException e) {
					log.warn("Invalid IP address in allowed IPs {}", address);
				}
			}
			return false;
		}
		
		for(String address : policy.getBlockedIPs()) {
			try {
				if(matchesAddress(address, remoteAddress)) {
					return false;
				}
			} catch (UnknownHostException e) {
				log.warn("Invalid IP address in blocked IPs {}", address);
			}
		}
		
		return true;
	}

	private boolean matchesAddress(String address, String remoteAddress) throws UnknownHostException {
		return new CIDRUtils(address).isInRange(remoteAddress);
	}

	@Override
	protected void beforeSave(AuthenticationPolicy policy) {		
		authenticationService.validateModules(policy);
		if(Request.isAvailable() && policy instanceof UserLoginAuthenticationPolicy) {
			if(Objects.isNull(getAssignedPolicy(getCurrentUser(), Request.getRemoteAddress(), policy.getClass(), policy))) {
				throw new IllegalStateException("The policy is invalid because it would lock the current user out from this location");
			}
		}
	}


	@Override
	protected Class<AuthenticationPolicy> getResourceClass() {
		return AuthenticationPolicy.class;
	}

	@Override
	public AuthenticationPolicy getDefaultPolicy() {
		return policyDatabase.getObject(getResourceClass(), SearchField.eq("system", true));
	}

	public void setResolver(AuthenticationPolicyResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {
		switch(column) {
		case "scope":
			return Html.i18n(AuthenticationPolicy.RESOURCE_KEY, rowTemplate.getResourceKey() + ".name");
		default:
			throw new IllegalStateException("Unsupported dynamic column " + column);
		}
		
	}

	@Override
	public boolean hasPasswordResetPolicy() {
		return policyDatabase.countObjects(AuthenticationPolicy.class, 
				SearchField.eq("resourceKey", PasswordResetAuthenticationPolicy.RESOURCE_KEY)) > 0;
	}

	@Override
	public AuthenticationPolicy getPasswordResetPolicy() {
		return null;
	}
}
