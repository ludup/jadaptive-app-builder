package com.jadaptive.api.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

public abstract class PasswordEnabledUserDatabaseImpl 
	extends AuthenticatedService implements PasswordEnabledUserDatabase, UUIDObjectService<User> {

	@Autowired
	private TenantAwareObjectDatabase<UserImpl> objectDatabase;
	
	@Override
	public void setPassword(User u, char[] password, boolean passwordChangeRequired) {
		
		try {
			PasswordEnabledUser user = (PasswordEnabledUser)u;
			byte[] salt = PasswordUtils.generateSalt();
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			user.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
			user.setSalt(Base64.getEncoder().encodeToString(salt));
			user.setEncodedPassword(Base64.getEncoder().encodeToString(encodedPassword));
			user.setPasswordChangeRequired(passwordChangeRequired);
			
			objectDatabase.saveOrUpdate(user);
			
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	
	@Override
	public boolean verifyPassword(User u, char[] password) {
		
		try {
			PasswordEnabledUser user = (PasswordEnabledUser)u;
			byte[] salt = Base64.getDecoder().decode(user.getSalt());
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			byte[] storedPassword = Base64.getDecoder().decode(user.getEncodedPassword());
			
			return Arrays.equals(encodedPassword, storedPassword);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	
	public UserImpl getObjectByUUID(String uuid) {
		return objectDatabase.get(uuid, UserImpl.class);
	}

	public String saveOrUpdate(User u) {
		PasswordEnabledUser user = (PasswordEnabledUser)u;
		setPassword(user, user.getEncodedPassword().toCharArray(), user.getPasswordChangeRequired());
		return user.getUuid();
	}
}
