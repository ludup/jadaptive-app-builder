package com.jadaptive.api.user;

import java.util.Date;

public interface PasswordChangeSupport {
 
	boolean getPasswordChangeRequired();
	
	Date getPasswordExpiry();
}
