package com.jadaptive.plugins.icon;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

@Service
public class IconPasswordServiceImpl implements IconPasswordService {

	public static final String ICON_PASSWORD_RESOURCE_KEY = "iconPassword";
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private PersonalObjectDatabase<IconPasswordCredentials> objectDatabase;
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage(ICON_PASSWORD_RESOURCE_KEY, IconPassword.class);
	}
	
	
	public boolean verifyIconPassword(User user, String iconPassword) {
		
		try {
			IconPasswordCredentials creds = objectDatabase.getPersonalObject(IconPasswordCredentials.class, user);
			byte[] providedCreds = PasswordUtils.getEncryptedPassword(iconPassword.toCharArray(), 
					Base64.getDecoder().decode(creds.getSalt()), 
					creds.getEncodingType());
			
			return Arrays.equals(Base64.getDecoder().decode(creds.getIconPassword()), providedCreds);
			
		} catch(ObjectNotFoundException e) {
			return false;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RepositoryException(e);
		}
	}
	
	@Override
	public void setIconPassword(User user, String iconPassword, PasswordEncryptionType encodingType) {
		
		IconPasswordCredentials creds;
		try {
			creds = objectDatabase.getPersonalObject(IconPasswordCredentials.class, user);
		} catch(ObjectNotFoundException e) {
			creds = new IconPasswordCredentials();
			creds.setOwnerUUID(user.getUuid());
		}
		
		try {
			byte[] salt = PasswordUtils.generateSalt();
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					iconPassword.toCharArray(), 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			creds.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
			creds.setSalt(Base64.getEncoder().encodeToString(salt));
			creds.setIconPassword(Base64.getEncoder().encodeToString(encodedPassword));
			
			objectDatabase.saveOrUpdate(creds);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RepositoryException(e);
		}
		
	}


	@Override
	public boolean hasCredentials(User user) {
		try {
			objectDatabase.getPersonalObject(IconPasswordCredentials.class, user);
			return true;
		} catch(ObjectNotFoundException e) {
			return false;
		} 
	}
}
