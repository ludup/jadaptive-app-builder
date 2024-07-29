package com.jadaptive.api.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

public abstract class PasswordEnabledUserDatabaseImpl 
	extends AuthenticatedService implements PasswordEnabledUserDatabase {

	private static final String ENCRYPTION_PREFIX = "!ENC!";
	
	@Autowired
	private TenantAwareObjectDatabase<User> objectDatabase;
	
	@Override
	public void setPassword(User u, char[] password, boolean passwordChangeRequired) {
		
		try {
			
			PasswordEnabledUser user = (PasswordEnabledUser)u;
			
			if(Objects.nonNull(password) &&
					password.length > 0 && 
					!new String(password).startsWith(ENCRYPTION_PREFIX)) {
				byte[] salt = PasswordUtils.generateSalt();
				byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
						password, 
						salt, 
						PasswordEncryptionType.PBKDF2_SHA512_100000);
				
				user.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
				user.setSalt(Base64.getEncoder().encodeToString(salt));
				user.setEncodedPassword(ENCRYPTION_PREFIX + Base64.getEncoder().encodeToString(encodedPassword));
			
			}
			
			user.setPasswordChangeRequired(passwordChangeRequired);
			objectDatabase.saveOrUpdate(user);
			
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	
	@Override
	public boolean hasPassword(User u) {

		PasswordEnabledUser user = (PasswordEnabledUser)u;
		
		return Objects.nonNull(user.getEncodedPassword()) &&
				user.getEncodedPassword().length() > 0 && 
				user.getEncodedPassword().startsWith(ENCRYPTION_PREFIX);
			
	}
	
	@Override
	public boolean verifyPassword(User u, char[] password) {
		
		try {
			if(!(u instanceof PasswordEnabledUser)) {
				return false;
			}
			
			PasswordEnabledUser user = (PasswordEnabledUser)u;
			byte[] salt = Base64.getDecoder().decode(user.getSalt());
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					user.getEncodingType());
			
			byte[] storedPassword = Base64.getDecoder().decode(user.getEncodedPassword().replace(ENCRYPTION_PREFIX, ""));
			
			return Arrays.equals(encodedPassword, storedPassword);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new ObjectException(e);
		}
	}
	
	public User getObjectByUUID(String uuid) {
		return objectDatabase.get(uuid, User.class);
	}

	public String saveOrUpdate(User u) {
		objectDatabase.saveOrUpdate(u);
		return u.getUuid();
	}

}
