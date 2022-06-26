package com.jadaptive.plugins.builtin;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.IncludeView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.ViewType;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(resourceKey = BuiltinUser.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@ObjectServiceBean(bean = BuiltinUserDatabase.class)
@ObjectViews({ @ObjectViewDefinition(type = ViewType.ACCORDION, bundle = "users", value = "passwordOptions") })
public class BuiltinUser extends PasswordEnabledUser {

	private static final long serialVersionUID = -4186606233520076592L;

	public static final String RESOURCE_KEY = "builtinUsers";

	@ObjectField(type = FieldType.PASSWORD)
	@IncludeView(values = FieldView.CREATE)
	@ObjectView(value = "passwordOptions")
	String encodedPassword;

	@ObjectField(hidden = true, type = FieldType.TEXT)
	String salt;

	@ObjectField(defaultValue = "PBKDF2_SHA512_50000", type = FieldType.ENUM, hidden = true)
	@ObjectView(value = "passwordOptions")
	PasswordEncryptionType encodingType;

	@ObjectField(defaultValue = "false", type = FieldType.BOOL)
	@ObjectView(value = "passwordOptions")
	boolean passwordChangeRequired;

	@Override
	public String getEncodedPassword() {
		return encodedPassword;
	}

	@Override
	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	@Override
	public String getSalt() {
		return salt;
	}

	@Override
	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Override
	public PasswordEncryptionType getEncodingType() {
		return encodingType;
	}

	@Override
	public void setEncodingType(PasswordEncryptionType encodingType) {
		this.encodingType = encodingType;
	}

	public boolean getPasswordChangeRequired() {
		return passwordChangeRequired;
	}

	public void setPasswordChangeRequired(boolean passwordChangeRequired) {
		this.passwordChangeRequired = passwordChangeRequired;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
