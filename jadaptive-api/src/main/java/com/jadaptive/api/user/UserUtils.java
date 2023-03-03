package com.jadaptive.api.user;

public class UserUtils {

	public static boolean getPasswordChangeRequired(User user) {
		if(user instanceof PasswordEnabledUser) {
			return ((PasswordEnabledUser)user).getPasswordChangeRequired();
		}
		return false;
	}
}
