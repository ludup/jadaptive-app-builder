package com.jadaptive.api.user;

public class UserUtils {

	public static String getEmailAddress(User user) {
		if(user instanceof EmailEnabledUser) {
			return ((EmailEnabledUser)user).getEmail();
		}
		return "";
	}
	
	public static void setEmailAddress(User user, String email) {
		if(user instanceof EmailEnabledUser) {
			 ((EmailEnabledUser)user).setEmail(email);
			 return;
		}
		throw new UnsupportedOperationException("User is not an email enabled user");
	}

	public static boolean getPasswordChangeRequired(User user) {
		if(user instanceof PasswordEnabledUser) {
			return ((PasswordEnabledUser)user).getPasswordChangeRequired();
		}
		return false;
	}
}
