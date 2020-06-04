package com.jadaptive.api.user;

import com.jadaptive.utils.PasswordEncryptionType;

public abstract class PasswordEnabledUser extends UserImpl {

	private static final long serialVersionUID = 8159475827968045376L;

	public abstract String getEncodedPassword();

	public abstract void setEncodedPassword(String encodedPassword);

	public abstract String getSalt();

	public abstract void setSalt(String salt);

	public abstract PasswordEncryptionType getEncodingType();

	public abstract void setEncodingType(PasswordEncryptionType encodingType);

	public abstract void setPasswordChangeRequired(boolean passwordChangeRequired);

	public abstract boolean getPasswordChangeRequired();

}
