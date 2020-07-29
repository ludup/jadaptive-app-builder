package com.jadaptive.plugins.universal;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.user.User;

@Service
public class UAServiceImpl implements UAService {

	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private PersonalObjectDatabase<UACredentials> credentialsStore;
	
	@PostConstruct
	private void postConstruct() {
		authenticationService.registerAuthenticationPage("ua", UniversalAuthentication.class);
	}

	@Override
	public boolean hasCredentials(User user) {
		try {
			credentialsStore.getPersonalObjects(UACredentials.class, user);
			return true;
		} catch(ObjectNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public void saveRegistration(User user, Properties properties) {
		
		UACredentials creds = new UACredentials();
		creds.setOwnerUUID(user.getUuid());
		creds.fromProperties(properties);
		
		credentialsStore.saveOrUpdate(creds);
	}

	@Override
	public Properties getCredentials(User user) {
		UACredentials creds = credentialsStore.getPersonalObject(UACredentials.class, user);
		return creds.toProperties();
	}
	
	
}
