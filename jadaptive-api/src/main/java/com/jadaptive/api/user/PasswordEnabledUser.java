package com.jadaptive.api.user;

import com.jadaptive.utils.PasswordEncryptionType;

public abstract class PasswordEnabledUser extends UserImpl {

	public abstract String getEncodedPassword();

	public abstract void setEncodedPassword(String encodedPassword);

	public abstract String getSalt();

	public abstract void setSalt(String salt);

	public abstract PasswordEncryptionType getEncodingType();

	public abstract void setEncodingType(PasswordEncryptionType encodingType);

}
