package com.jadaptive.plugins.icon;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.utils.PasswordEncryptionType;

@ObjectDefinition(resourceKey = IconPasswordCredentials.RESOURCE_KEY, type = ObjectType.COLLECTION, scope = ObjectScope.PERSONAL)
public class IconPasswordCredentials extends PersonalUUIDEntity{

	private static final long serialVersionUID = -6257979710259065502L;
	
	public static final String RESOURCE_KEY = "iconasswordCredentials";

	@ObjectField(type = FieldType.TEXT)
	String iconPassword;
	
	@ObjectField(type = FieldType.TEXT)
	String salt;
	
	@ObjectField(type = FieldType.ENUM)
	PasswordEncryptionType encodingType;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getIconPassword() {
		return iconPassword;
	}

	public void setIconPassword(String picturePassword) {
		this.iconPassword = picturePassword;
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
	
}
