package com.jadaptive.plugins.web.objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.plugins.web.ui.tenant.TenantWizard;

@ObjectDefinition(resourceKey = ValidateEmail.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, bundle = TenantWizard.RESOURCE_KEY)
public class ValidateEmail extends AbstractUUIDEntity {

	private static final long serialVersionUID = 3983538400549962784L;

	public static final String RESOURCE_KEY = "validateEmail";

	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = "^[a-zA-Z0-9]{6}$", bundle = RESOURCE_KEY, i18n = "tenant.code.invalid")
	String code;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
