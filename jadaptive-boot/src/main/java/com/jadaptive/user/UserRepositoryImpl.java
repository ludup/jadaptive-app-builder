package com.jadaptive.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.entity.EntityException;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabaseImpl;
import com.jadaptive.tenant.TenantService;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

@Repository
public class UserRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<DefaultUser> implements UserRepository {

	@Autowired
	TenantService tenantService;
	
	protected UserRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public void createUser(DefaultUser user, char[] password) {
		
		setPassword(user, password);
		saveObject(user, tenantService.getCurrentTenant().getUuid());

	}
	
	
	@Override
	public void setPassword(User u, char[] password) {
		
		try {
			
			DefaultUser user = convertUser(u);
			
			byte[] salt = PasswordUtils.generateSalt();
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			user.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
			user.setSalt(Base64.getEncoder().encodeToString(salt));
			user.setEncodedPassword(Base64.getEncoder().encodeToString(encodedPassword));
			
			saveOrUpdate(user);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new EntityException(e);
		}
	}
	
	private DefaultUser convertUser(User u) {
		return (DefaultUser) u;
	}

	@Override
	public boolean verifyPassword(User u, char[] password) {
		
		try {
			
			DefaultUser user = convertUser(u);
			
			byte[] salt = Base64.getDecoder().decode(user.getSalt());
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			byte[] storedPassword = Base64.getDecoder().decode(user.getEncodedPassword());
			
			return Arrays.equals(encodedPassword, storedPassword);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new EntityException(e);
		}
	}

	@Override
	protected Class<DefaultUser> getResourceClass() {
		return DefaultUser.class;
	}

	@Override
	public DefaultUser findUsername(String username) {
		return findObject("username", username, tenantService.getCurrentTenant().getUuid(), DefaultUser.class);
	}
}
