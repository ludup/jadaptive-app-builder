package com.jadaptive.plugins.builtin;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(resourceKey = BuiltinUser.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class BuiltinUser extends PasswordEnabledUser implements EmailEnabledUser {

	private static final long serialVersionUID = -4186606233520076592L;

	public static final String RESOURCE_KEY = "builtinUsers";
	@ObjectField(required = true,
			searchable = true,
			type = FieldType.TEXT, 
			unique = true)
	String username;
	
	@ObjectField(required = true,
			searchable = true,
			type = FieldType.TEXT)
	String name;
	
	String encodedPassword;
	String salt;
	PasswordEncryptionType encodingType;
	boolean passwordChangeRequired;
	
	@ObjectField(required = false,
			searchable = true,
			type = FieldType.TEXT)
	String email;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@Override
	public String getSystemName() {
		return getUsername();
	}
}
