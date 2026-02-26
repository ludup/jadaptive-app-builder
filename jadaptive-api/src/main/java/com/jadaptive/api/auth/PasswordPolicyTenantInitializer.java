package com.jadaptive.api.auth;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.VerifyPasswordEvent;

@Component
public class PasswordPolicyTenantInitializer implements TenantAware, StartupAware {

    private static final String DEFAULT_POLICY_UUID = "bd7fb558-dadc-4df5-a814-45b76e9f2bb6";
    private static final String BUILT_IN_USERS_ROLE_UUID = "255a5fee-685b-4447-a1be-f912d77ebe5c";

    @Autowired
    private PasswordPolicyService passwordPolicyService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EventService eventService;
    
    @Override
    public void initializeTenant(Tenant tenant, boolean newSchema) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setUuid(DEFAULT_POLICY_UUID);
        policy.setName("Default Policy");
        policy.setSystem(true);
        policy.setMinimumLength(10);
        policy.setMinimumLowercase(1);
        policy.setMinimumUppercase(1);
        policy.setMinimumNumeric(1);
        policy.setMinimumSpecial(1);

        Role builtinUsers = roleService.getRoleByUUID(BUILT_IN_USERS_ROLE_UUID);
        policy.setRoles(Collections.singletonList(builtinUsers));

        passwordPolicyService.createIfNotExisting(policy);
    }
    
    public Integer getOrder() { return Integer.MAX_VALUE; }
    
    @Override
	public void onApplicationStartup() {
		
		eventService.on(VerifyPasswordEvent.RESOURCE_KEY, (e)->{
			
			VerifyPasswordEvent evt = (VerifyPasswordEvent)e;
			
			passwordPolicyService.verifyPassword(evt.getUser(), evt.getPassword());
			
		});
		
	}
}
