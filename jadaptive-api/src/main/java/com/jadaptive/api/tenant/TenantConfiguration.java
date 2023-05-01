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
@ObjectViews({@ObjectViewDefinition(value = TenantConfiguration.DOMAIN_VIEW, bundle = TenantConfiguration.RESOURCE_KEY),
		@ObjectViewDefinition(value = TenantConfiguration.DOMAIN_VIEW, bundle = TenantConfiguration.RESOURCE_KEY, weight = 9999)})
public class TenantConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = 4952852039617671471L;
	
	public static final String RESOURCE_KEY = "tenantConfiguration";
	
	public static final String DOMAIN_VIEW = "domain";
	public static final String ANALYTICS_VIEW = "analytics";
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(DOMAIN_VIEW)
	String rootDomain = "";
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(DOMAIN_VIEW)
	String registrationDomain = "";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(DOMAIN_VIEW)
	Boolean requireValidDomain;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@Validator(type = ValidationType.URL)
	@ObjectView(DOMAIN_VIEW)
	String invalidDomainRedirect;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(ANALYTICS_VIEW)
	String analyticsId = "";
	
	public String getRootDomain() {
		return rootDomain;
	}

	public void setRootDomain(String rootDomain) {
		this.rootDomain = rootDomain;
	}

	public Boolean getRequireValidDomain() {
		return requireValidDomain;
	}

	public String getRegistrationDomain() {
		return registrationDomain;
	}

	public void setRegistrationDomain(String registrationDomain) {
		this.registrationDomain = registrationDomain;
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

	public String getAnalyticsId() {
		return analyticsId;
	}

	public void setAnalyticsId(String analyticsId) {
		this.analyticsId = analyticsId;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
