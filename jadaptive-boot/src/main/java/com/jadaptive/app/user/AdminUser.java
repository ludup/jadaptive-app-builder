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
import com.jadaptive.api.template.ViewType;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(resourceKey = AdminUser.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, creatable = false)
@ObjectServiceBean(bean = AdminUserDatabase.class)
@ObjectViews({ 
	@ObjectViewDefinition(type = ViewType.ACCORDION, bundle = "users", value = "passwordOptions")})
public class AdminUser extends PasswordEnabledUser implements EmailEnabledUser {

	private static final long serialVersionUID = -4995333149629598100L;

	public static final String RESOURCE_KEY = "adminUser";
	
	@ObjectField(required = true,
			searchable = true,
			type = FieldType.TEXT)
	String username;
	
	@ObjectField(required = false,
			type = FieldType.HIDDEN)
	String encodedPassword;
	
	@ObjectField(required = false,
			type = FieldType.HIDDEN)
	String salt;
	
	@ObjectField(required = false,
			defaultValue = "PBKDF2_SHA512_50000",
			type = FieldType.ENUM)
	@ObjectView(value = "passwordOptions")
	PasswordEncryptionType encodingType;
	
	@ObjectField(required = false,
			defaultValue = "true",
			type = FieldType.BOOL)
	@ObjectView(value = "passwordOptions")
	boolean passwordChangeRequired;
	
	@ObjectField(required = false,
			searchable = true,
			type = FieldType.TEXT)
	String email;
	
	@Override
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
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
	public String getSystemName() {
		return getUsername();
	}

}
