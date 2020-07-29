package com.jadaptive.plugins.icon;

import com.jadaptive.api.user.User;
import com.jadaptive.utils.PasswordEncryptionType;

public interface IconPasswordService {

	boolean verifyIconPassword(User user, String picturePassword);

	boolean hasCredentials(User user);

	void setIconPassword(User user, String picturePassword, PasswordEncryptionType encodingType);
}

