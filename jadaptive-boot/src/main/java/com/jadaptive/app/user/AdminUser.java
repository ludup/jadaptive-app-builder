package com.jadaptive.app.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(resourceKey = AdminUser.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, creatable = false)
@ObjectServiceBean(bean = UserService.class)
@ObjectViewDefinition(bundle = "users", value = "passwordOptions", weight = 9999)
public class AdminUser extends PasswordEnabledUser {

	private static final long serialVersionUID = -4995333149629598100L;

	public static final String RESOURCE_KEY = "adminUser";
	
	@ObjectField(hidden = true, type = FieldType.PASSWORD)
	String encodedPassword;
	
	@ObjectField(
			hidden = true,
			type = FieldType.PASSWORD)
	@Validator(type = ValidationType.REQUIRED)
	String salt;
	
	@ObjectField(
			defaultValue = "PBKDF2_SHA512_50000",
			type = FieldType.ENUM,
			hidden = true)
	@Validator(type = ValidationType.REQUIRED)
	@ObjectView(value = "passwordOptions")
	PasswordEncryptionType encodingType;
	
	@ObjectField(
			defaultValue = "false",
			type = FieldType.BOOL)
	@ObjectView(value = "passwordOptions")
	@Validator(type = ValidationType.REQUIRED)
	boolean passwordChangeRequired;
	
	@Override
	public String getName() {
		return "Administrator";
	}

	@Override
	public boolean getPasswordChangeRequired() {
		return passwordChangeRequired;
	}

	@Override
	public void setPasswordChangeRequired(boolean change) {
		this.passwordChangeRequired = change;
	}

	@Override
	@JsonIgnore
	public String getEncodedPassword() {
		return encodedPassword;
	}

	@Override
	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	@Override
	@JsonIgnore
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	@JsonIgnore
	public PasswordEncryptionType getEncodingType() {
		return encodingType;
	}

	@Override
	public void setEncodingType(PasswordEncryptionType encodingType) {
		this.encodingType = encodingType;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@Override
	public String getEventGroup() {
		return User.RESOURCE_KEY;
	}

}
