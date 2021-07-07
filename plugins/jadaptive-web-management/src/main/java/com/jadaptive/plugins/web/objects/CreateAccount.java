package com.jadaptive.plugins.web.objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = CreateAccount.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class CreateAccount extends UUIDEntity {

	private static final long serialVersionUID = -4229613403580844237L;

	public static final String RESOURCE_KEY = "createAccount";
	
	@ObjectField(required = true,
			type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = "^[a-zA-Z0-9]{4,32}$", bundle = RESOURCE_KEY, i18n = "usernane.invalid")
	String username;
	
	@ObjectField(required = true,
			type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", bundle = RESOURCE_KEY, i18n = "email.invalid")
	String email;
	
	@ObjectField(required = true,type = FieldType.PASSWORD)
	@Validator(bundle = CreateAccount.RESOURCE_KEY, type = ValidationType.REGEX, i18n = "invalid.password", value = "^(?=^.{8,}$)(?=.*\\d)|(?=.*\\W+)(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")
	String firstPassword;
	
	@ObjectField(required = true, type = FieldType.PASSWORD)
	@Validator(bundle = CreateAccount.RESOURCE_KEY, type = ValidationType.REGEX, i18n = "invalid.password", value = "^(?=^.{8,}$)(?=.*\\d)|(?=.*\\W+)(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$")
	String secondPassword;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstPassword() {
		return firstPassword;
	}

	public void setFirstPassword(String firstPassword) {
		this.firstPassword = firstPassword;
	}

	public String getSecondPassword() {
		return secondPassword;
	}

	public void setSecondPassword(String secondPassword) {
		this.secondPassword = secondPassword;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}
