package com.jadaptive.plugins.web.objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = ConfigureDomain.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class ConfigureDomain extends AbstractUUIDEntity {

	private static final long serialVersionUID = 3983538400549962784L;

	public static final String RESOURCE_KEY = "configureSubDomain";

	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = "^[a-zA-Z0-9]{4,32}$", bundle = RESOURCE_KEY, i18n = "tenant.domain.invalid")
	String subdomain;
	
	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
