package com.jadaptive.plugins.remote;

import com.identity4j.connector.Media;
import com.identity4j.connector.principal.Identity;
import com.jadaptive.api.user.User;

public class RemoteUser extends User {

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
	public String getEmail() {
		return identity.getAddress(Media.email);
	}

	@Override
	public void setEmail(String email) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@Override
	public String getEventGroup() {
		return User.RESOURCE_KEY;
	}
}
