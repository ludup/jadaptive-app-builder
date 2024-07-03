package com.jadaptive.app.auth;

import java.lang.reflect.InvocationTargetException;
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
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.TenantService;
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
	
	@Autowired
	private TenantService tenantService; 
	
	private AuthenticationPolicyResolver resolver;
	
	@Override
	public AuthenticationPolicy getAssignedPolicy(User user, String ipAddress, Class<? extends AuthenticationPolicy> policyClz, AuthenticationPolicy... additionalPolicies) {
		
		List<AuthenticationPolicy> results =  new ArrayList<>();
		
		for(AuthenticationPolicy policy : policyDatabase.getAssignedObjectsA(AuthenticationPolicy.class, user)) {
			if(!policy.getClass().equals(policyClz)) {
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
			if(!additional.getClass().equals(policyClz)) {
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
				return getDefaultPolicy(policyClz);
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

		if(tenantService.isReady()) {
			/**
			 * Only validate when the tenant is "ready" as this could be called in initialization
			 * when the default policy is created. We don't want to error out when thats created
			 */
			authenticationService.validateModules(policy);
			if(Request.isAvailable() && policy instanceof UserLoginAuthenticationPolicy) {
				AuthenticationPolicy assigned = getAssignedPolicy(getCurrentUser(), Request.getRemoteAddress(), policy.getClass(), policy);
				if(Objects.isNull(assigned)) {
					throw new IllegalStateException("The policy is invalid because it would lock the current user out from this location");
				}
				
				
			}
		}
	}


	@Override
	protected Class<AuthenticationPolicy> getResourceClass() {
		return AuthenticationPolicy.class;
	}

	@Override
	public AuthenticationPolicy getDefaultPolicy(Class<? extends AuthenticationPolicy> clz) {
		if(!clz.equals(UserLoginAuthenticationPolicy.class)) {
			return getWeightedPolicy(clz);
		} else {
			return policyDatabase.getObject(getResourceClass(), SearchField.eq("system", true),
					SearchField.eq("resourceKey", UserLoginAuthenticationPolicy.RESOURCE_KEY));
		}
		
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

	private AuthenticationPolicy getWeightedPolicy(Class<? extends AuthenticationPolicy> scope) {
		
		try {
			List<AuthenticationPolicy> tmp = new ArrayList<>(policyDatabase.searchObjects(
					AuthenticationPolicy.class, 
					SearchField.eq("resourceKey", 
							scope.getConstructor().newInstance().getResourceKey())));
			
			if(tmp.isEmpty()) {
				throw new IllegalStateException("No " + scope.getClass().getSimpleName() + " policies are configured!");
			}

			Collections.sort(tmp, new Comparator<AuthenticationPolicy>() {
				@Override
				public int compare(AuthenticationPolicy o1, AuthenticationPolicy o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			
			return tmp.get(0);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasPolicy(String resourceKey) {
		return policyDatabase.countObjects(AuthenticationPolicy.class, SearchField.eq("resourceKey", resourceKey)) > 0;
	}
}
