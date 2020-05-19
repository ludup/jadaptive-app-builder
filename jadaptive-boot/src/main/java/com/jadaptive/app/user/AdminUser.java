package com.jadaptive.app.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.utils.PasswordEncryptionType;

@Template(name = "Administration Users", resourceKey = AdminUser.RESOURCE_KEY, scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
public class AdminUser extends PasswordEnabledUser {

	public static final String RESOURCE_KEY = "adminUser";
	
	String encodedPassword;
	String salt;
	PasswordEncryptionType encodingType;
	boolean passwordChangeRequired;
	
	@Column(name = "Username", 
			description = "The logon name of the user",
			required = true,
			searchable = true,
			type = FieldType.TEXT, 
			unique = true)
	String username;
	
	@Column(name = "Full Name", 
			description = "The full name of the user",
			required = true,
			searchable = true,
			type = FieldType.TEXT)
	String name;
	
	@Column(name = "Email", 
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
	public void setName(String value) {
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
