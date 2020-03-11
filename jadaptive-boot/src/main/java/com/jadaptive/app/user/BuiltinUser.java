package com.jadaptive.app.user;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.PasswordEncryptionType;

@Template(name = "Builtin User", resourceKey = "builtinUsers", scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
public class BuiltinUser extends NamedUUIDEntity implements User {

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
	
	String encodedPassword;
	String salt;
	PasswordEncryptionType encodingType;
	boolean passwordChangeRequired;
	
	@Column(name = "Email", 
			description = "The user's email address",
			required = false,
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

	public String getEncodedPassword() {
		return encodedPassword;
	}

	public void setEncodedPassword(String encodedPassword) {
		this.encodedPassword = encodedPassword;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public PasswordEncryptionType getEncodingType() {
		return encodingType;
	}

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
}
