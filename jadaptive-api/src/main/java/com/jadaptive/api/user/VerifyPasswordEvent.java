package com.jadaptive.api.user;

public class VerifyPasswordEvent extends PasswordEvent {

	private static final long serialVersionUID = 5458426923519872724L;
	
	public static final String RESOURCE_KEY = "verifyPassword";

	User user;
	char[] password;
	
	public VerifyPasswordEvent() { 
		super(RESOURCE_KEY);
	}

	public VerifyPasswordEvent(User user, char[] password) {
		super(RESOURCE_KEY);
		this.user = user;
		this.password = password;
	}

	public User getUser() {
		return user;
	}

	public char[] getPassword() {
		return password;
	}

}
