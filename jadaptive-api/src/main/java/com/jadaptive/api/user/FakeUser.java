package com.jadaptive.api.user;

public class FakeUser extends User {

	private static final long serialVersionUID = 6361181523955828969L;

	public static final String FAKE_USER_UUID = "";
	
	String username;
	public FakeUser(String username) {
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
	public String getResourceKey() {
		return "user";
	}
	
	

}
