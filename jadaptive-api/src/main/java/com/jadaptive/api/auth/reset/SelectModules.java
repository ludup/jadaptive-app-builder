package com.jadaptive.api.auth.reset;

import java.util.Collection;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = SelectModules.RESOURCE_KEY, type = ObjectType.OBJECT, bundle = PasswordResetPolicyWizard.RESOURCE_KEY)
public class SelectModules extends AbstractUUIDEntity {

	private static final String IGNORE_PASSWORD_MODULE = "ignoreUUIDs=" + AuthenticationService.PASSWORD_MODULE_UUID;
	
	private static final long serialVersionUID = -6387354891743383657L;
	public static final String RESOURCE_KEY = "selectPasswordResetModules";
	
	@ObjectField(type = FieldType.OPTIONS, meta = IGNORE_PASSWORD_MODULE, references = AuthenticationModule.RESOURCE_KEY)
	Collection<AuthenticationModule> requiredModules;
	
	@ObjectField(type = FieldType.OPTIONS, meta = IGNORE_PASSWORD_MODULE, references = AuthenticationModule.RESOURCE_KEY)
	Collection<AuthenticationModule> optionalModules;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "1")
	Integer requireOptional;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Collection<AuthenticationModule> getRequiredModules() {
		return requiredModules;
	}

	public void setRequiredModules(Collection<AuthenticationModule> requiredModules) {
		this.requiredModules = requiredModules;
	}

	public Collection<AuthenticationModule> getOptionalModules() {
		return optionalModules;
	}

	public void setOptionalModules(Collection<AuthenticationModule> optionalModules) {
		this.optionalModules = optionalModules;
	}

	public Integer getRequireOptional() {
		return requireOptional;
	}

	public void setRequireOptional(Integer requireOptional) {
		this.requireOptional = requireOptional;
	}
	
	

}
