package com.jadaptive.api.user;

import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.utils.PasswordEncryptionType;

@TableAction(bundle = "default", icon = "fa-key", resourceKey = "setPassword", target = Target.ROW, url = "/app/ui/set-password/{uuid}")
public abstract class PasswordEnabledUser extends User implements PasswordChangeRequired {

	private static final long serialVersionUID = 8159475827968045376L;

	public abstract String getEncodedPassword();

	public abstract void setEncodedPassword(String encodedPassword);

	public abstract String getSalt();

	public abstract void setSalt(String salt);

	public abstract PasswordEncryptionType getEncodingType();

	public abstract void setEncodingType(PasswordEncryptionType encodingType);

	public abstract void setPasswordChangeRequired(boolean passwordChangeRequired);

}
