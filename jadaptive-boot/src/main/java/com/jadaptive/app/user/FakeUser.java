package com.jadaptive.app.user;

import com.jadaptive.api.user.User;

public class FakeUser implements User {

	private static final long serialVersionUID = 6361181523955828969L;

	public static final String FAKE_USER_UUID = "";
	
	String username;
	FakeUser(String username) {
		this.username = username;
	}
	
	@Override
	public Boolean isSystem() {
		return true;
	}

	@Override
	public Boolean isHidden() {
		return true;
	}

	@Override
	public void setUuid(String string) {
		
	}

	@Override
	public String getUuid() {
		return FAKE_USER_UUID;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void setName(String value) {
		
	}

	@Override
	public String getSystemName() {
		return getUsername();
	}
	
	

}
