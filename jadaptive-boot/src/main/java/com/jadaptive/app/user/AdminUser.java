package com.jadaptive.app.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(name = "Administration Users", resourceKey = AdminUser.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class AdminUser extends PasswordEnabledUser implements EmailEnabledUser {

	private static final long serialVersionUID = -4995333149629598100L;

	public static final String RESOURCE_KEY = "adminUser";
	
	String encodedPassword;
	String salt;
	PasswordEncryptionType encodingType;
	boolean passwordChangeRequired;
	
	@ObjectField(name = "Username", 
			description = "The logon name of the user",
			required = true,
			searchable = true,
			type = FieldType.TEXT, 
			unique = true)
	String username;
	
	@ObjectField(name = "Full Name", 
			description = "The full name of the user",
			required = true,
			searchable = true,
			type = FieldType.TEXT)
	String name;
	
	@ObjectField(name = "Email", 
			description = "The user's email address",
			required = false,
			searchable = true,
			type = FieldType.TEXT)
	String email;
	
	@Override
	public String getUsername() {
		return "admin";
	}

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
	public String getEmail() {
		return email;
	}

	@Override
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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

}
