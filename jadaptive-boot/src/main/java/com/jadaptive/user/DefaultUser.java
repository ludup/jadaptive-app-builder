package com.jadaptive.user;

import com.jadaptive.repository.NamedUUIDEntity;
import com.jadaptive.utils.PasswordEncryptionType;

public class DefaultUser extends NamedUUIDEntity {

	String username;
	String encodedPassword;
	String salt;
	PasswordEncryptionType encodingType;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
	
	
}
