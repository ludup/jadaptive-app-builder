package com.jadaptive.app.auth;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.jadaptive.api.app.App;
import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyResolver;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.LoginAuthenticationPolicy;
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.product.ProductService.ProductId;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.FeatureEnablementService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.CIDRUtils;

@Service
@LicensedFeature(value = AuthenticationPolicyServiceImpl.FEATURE_2FA, group = FeatureGroup.PROFESSIONAL, excludeProducts = ProductId.PASSWORD_EXPRESS_ONPREM)
@LicensedFeature(value = AuthenticationPolicyServiceImpl.FEATURE_2FA, group = FeatureGroup.FREE, includeProducts = ProductId.PASSWORD_EXPRESS_ONPREM)

public class AuthenticationPolicyServiceImpl extends AbstractUUIDObjectServceImpl<AuthenticationPolicy> implements AuthenticationPolicyService {

	private static final Logger log = LoggerFactory.getLogger(AuthenticationPolicyService.class);
	
	public static final String FEATURE_2FA = "2FA";
	
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
		
		
		
		if(LoginAuthenticationPolicy.class.isAssignableFrom(policyClz) && !App.bean(FeatureEnablementService.class).isEnabled(FEATURE_2FA)) {
			return getSystemDefaultPolicy(policyClz);
		}
		
		List<AuthenticationPolicy> results =  new ArrayList<>();
		
		for(AuthenticationPolicy policy : getAssignedPolicies(user)) {
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
		
		if(permissionService.isAdministrator(user)) {
			
			if(log.isInfoEnabled()) {
				log.info("Administrator has multiple policies. Looking for a specific Administration policy first");
			}
			for(AuthenticationPolicy policy : results) {
				if(policy.getRoles().contains(roleService.getAdministrationRole())) {
					return policy;
				}
			}
			
			if(results.isEmpty()) {

				if(log.isInfoEnabled()) {
					log.info("Administrator not in any policy so returning default");
				}
				return getDefaultPolicy(policyClz);
			}
		}
		if(results.isEmpty()) {
			return null;
		}
		
		return results.get(0);
	}
	
	@Override
	public AuthenticationPolicy getSystemDefaultPolicy(Class<? extends AuthenticationPolicy> clz) {
		
		AuthenticationPolicy defaultPolicy;
		try {
			defaultPolicy = clz.getConstructor().newInstance();
			defaultPolicy.setName("Default Authentication Policy");
			defaultPolicy.setOptionalRequired(0);
			defaultPolicy.setPasswordRequired(true);
			defaultPolicy.setSystem(true);
			defaultPolicy.getRoles().add(roleService.getEveryoneRole());	
			return defaultPolicy;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Authenticaiton policy must have a default constructor!", e);
		}
		
	}

	@Override
	public boolean assertIPAddress(String remoteAddress, AuthenticationPolicy policy ) {
		
		var allowAll = policy.getAllowedIPs().isEmpty();
		var blockAll = policy.getBlockedIPs().isEmpty();
		
		if(allowAll && blockAll) {
			/* Both allow and block lists are empty, we so we always allow by default */
			return true;
		}
		else {
			var allow = policy.getAllowedIPs().stream().
					filter(address -> matchesAddress(address, remoteAddress)).findFirst().isPresent();
			
			if(blockAll) {
				/* The block list is empty, but the allow list has addresses. Immediately allow
				 * if the remote address matches any in this list, otherwise immediately block */
				return allow;
			}
			
			var block = policy.getBlockedIPs().stream().
					filter(address -> matchesAddress(address, remoteAddress)).findFirst().isPresent();
			
			if(allowAll) {
				/* The allow list empty, but the block list has some addresses. Immediately allow 
				 * if the remote addresses does NOT match any in the block list, otherwise immediately 
				 * block  
				 */
				return !block;
			}
			else {
				/* The allow list has addresses, so does the block list. Immediately allow
				 * if the remote address IS in the allow list OR it is NOT in the block list.
				 */
				return allow || !block;
			}
			
		}
		

	}

	private boolean matchesAddress(String address, String remoteAddress) {
		try {
			return new CIDRUtils(address).isInRange(remoteAddress);
		} catch (UnknownHostException e) {
			log.warn("Invalid IP address in blocked IPs {}", address);
			return false;
		}
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
			AuthenticationPolicy policy = getWeightedPolicy(clz);
			if(Objects.nonNull(resolver)) {
				resolver.assertDefaultPolicy(policy);
			}
			return policy;
		} else {
			
			if(!App.bean(FeatureEnablementService.class).isEnabled(FEATURE_2FA)) {
				return getSystemDefaultPolicy(clz);
			}
			
			AuthenticationPolicy policy = policyDatabase.getObject(getResourceClass(), 
					SearchField.eq("system", true),
					SearchField.eq("resourceKey", 
					UserLoginAuthenticationPolicy.RESOURCE_KEY));
			return policy;
		}
		
	}

	public void setResolver(AuthenticationPolicyResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {
		switch(column) {
		case "scope":
			return Html.i18n(rowTemplate.getBundle(), rowTemplate.getResourceKey() + ".name");
		default:
			throw new IllegalStateException("Unsupported dynamic column " + column);
		}
		
	}

	private AuthenticationPolicy getWeightedPolicy(Class<? extends AuthenticationPolicy> clz) {
		
		try {
			
			if(!App.bean(FeatureEnablementService.class).isEnabled(FEATURE_2FA) && LoginAuthenticationPolicy.class.isAssignableFrom(clz)) {
				return getSystemDefaultPolicy(clz);
			}
			
			List<AuthenticationPolicy> tmp = new ArrayList<>(policyDatabase.searchObjects(
					AuthenticationPolicy.class, 
					SearchField.eq("resourceKey", 
							clz.getConstructor().newInstance().getResourceKey())));
			
			if(tmp.isEmpty()) {
				throw new IllegalStateException("No " + clz.getClass().getSimpleName() + " policies are configured!");
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

	@Override
	public Iterable<AuthenticationPolicy> getAssignedPolicies(User user) {
		if(!App.bean(FeatureEnablementService.class).isEnabled(FEATURE_2FA)) {
			return Arrays.asList(getSystemDefaultPolicy(UserLoginAuthenticationPolicy.class));
		}
		return policyDatabase.getAssignedObjectsA(AuthenticationPolicy.class, user);
	}
}
