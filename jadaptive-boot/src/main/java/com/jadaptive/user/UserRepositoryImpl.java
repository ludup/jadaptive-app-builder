package com.jadaptive.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.entity.EntityException;
import com.jadaptive.tenant.TenantService;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

@Repository
public class UserRepositoryImpl extends AbstractObjectDatabaseImpl implements UserRepository {

	@Autowired
	TenantService tenantService;
	
	protected UserRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public void createUser(DefaultUser user, char[] password) {
		
		try {
			byte[] salt = PasswordUtils.generateSalt();
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			user.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
			user.setSalt(Base64.getEncoder().encodeToString(salt));
			user.setEncodedPassword(Base64.getEncoder().encodeToString(encodedPassword));
			
			saveObject(user, tenantService.getCurrentTenant().getUuid());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new EntityException(e);
		}
		
	}
}
