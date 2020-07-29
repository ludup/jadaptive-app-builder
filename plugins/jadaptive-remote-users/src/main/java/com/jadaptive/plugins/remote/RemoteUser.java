package com.jadaptive.plugins.remote;

import com.identity4j.connector.Media;
import com.identity4j.connector.jndi.activedirectory.ActiveDirectoryConnector;
import com.identity4j.connector.principal.Identity;
import com.jadaptive.api.user.EmailEnabledUser;
import com.jadaptive.api.user.User;

public class RemoteUser implements User, EmailEnabledUser {

	private static final long serialVersionUID = -6913894096452077318L;
	
	public static final String RESOURCE_KEY = "remoteUsers";
	
	Identity identity;
	public RemoteUser(Identity identity) {
		this.identity = identity;
	}

	@Override
	public String getUsername() {
		return identity.getPrincipalName();
	}

	@Override
	public String getName() {
		return identity.getFullName();
	}

	@Override
	public void setName(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean isSystem() {
		return false;
	}

	@Override
	public Boolean isHidden() {
		return false;
	}

	@Override
	public void setUuid(String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUuid() {
		String guid = identity.getGuid();
		
		return guid;
	}
	
	@Override
	public String getSystemName() {
		return identity.getAttribute(ActiveDirectoryConnector.USER_PRINCIPAL_NAME_ATTRIBUTE);
	}

	@Override
	public String getEmail() {
		return identity.getAddress(Media.email);
	}

	@Override
	public void setEmail(String email) {
		throw new UnsupportedOperationException();
	}
}
