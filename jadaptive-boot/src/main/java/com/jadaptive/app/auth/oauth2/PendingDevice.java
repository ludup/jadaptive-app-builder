package com.jadaptive.app.auth.oauth2;

import java.time.Instant;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public final class PendingDevice {
	
	public enum Status {
		PENDING, APPROVED, REJECTED
	}
	
	private final String deviceCode;
	private final String userCode;
	private final Instant created;
	private final OAuth2Request request;
	private final Tenant tenant;
	
	private Instant lastPoll;
	private Status status = Status.PENDING;
	private User user;

	PendingDevice(String deviceCode, String userCode, OAuth2Request request, Tenant tenant) {
		super();
		this.deviceCode = deviceCode;
		this.userCode = userCode;
		this.created = Instant.now();
		this.request = request;
		this.tenant = tenant;
	}
	
	public User user() {
		return user;
	}

	public void user(User user) {
		this.user = user;
	}

	public Status status() {
		return status;
	}
	
	public void status(Status status) {
		this.status = status;
	}
	
	public Instant lastPoll() {
		return lastPoll;
	}

	public void lastPoll(Instant lastPoll) {
		this.lastPoll = lastPoll;
	}

	public OAuth2Request request() {
		return request;
	}

	public Instant created() {
		return created;
	}

	public String deviceCode() {
		return deviceCode;
	}

	public String userCode() {
		return userCode;
	}

	public Tenant tenant() {
		return tenant;
	}
}