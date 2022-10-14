package com.jadaptive.api.tenant;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = TenantConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ObjectViews({@ObjectViewDefinition(value = TenantConfiguration.DOMAIN_VIEW, bundle = TenantConfiguration.RESOURCE_KEY)})

public class TenantConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = 4952852039617671471L;
	
	public static final String RESOURCE_KEY = "tenantConfiguration";
	
	public static final String DOMAIN_VIEW = "domain";
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(DOMAIN_VIEW)
	String rootDomain = "";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(DOMAIN_VIEW)
	Boolean requireValidDomain;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@Validator(type = ValidationType.URL)
	@ObjectView(DOMAIN_VIEW)
	String invalidDomainRedirect;
	
	
	public String getRootDomain() {
		return rootDomain;
	}

	public void setRootDomain(String rootDomain) {
		this.rootDomain = rootDomain;
	}

	public Boolean getRequireValidDomain() {
		return requireValidDomain;
	}

	public void setRequireValidDomain(Boolean requireValidDomain) {
		this.requireValidDomain = requireValidDomain;
	}

	public String getInvalidDomainRedirect() {
		return invalidDomainRedirect;
	}

	public void setInvalidDomainRedirect(String invalidDomainRedirect) {
		this.invalidDomainRedirect = invalidDomainRedirect;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}