package com.jadaptive.plugins.web.objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.Validators;
import com.jadaptive.plugins.web.ui.tenant.TenantWizard;

@ObjectDefinition(resourceKey = CreateTenant.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, bundle = TenantWizard.RESOURCE_KEY)
public class CreateTenant extends AbstractUUIDEntity {

	private static final long serialVersionUID = 3983538400549962784L;

	public static final String RESOURCE_KEY = "createTenant";
	
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String company;

	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String name;
	
	@ObjectField(type = FieldType.TEXT)
	@Validators({@Validator(type = ValidationType.REQUIRED),
		@Validator(type = ValidationType.REGEX, value = EMAIL_PATTERN)})
	String emailAddress;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
