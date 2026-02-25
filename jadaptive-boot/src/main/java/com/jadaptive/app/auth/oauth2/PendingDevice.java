package com.jadaptive.app.auth.oauth2;

import java.time.Instant;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.tenant.Tenant;

public class PendingDevice implements java.io.Serializable {
	
	private static final long serialVersionUID = -4055159033417158051L;

	public enum Status {
		PENDING, APPROVED, REJECTED
	}
	
	private String deviceCode;
	private String userCode;
	private Instant created;
	private OAuth2Request request;
	private String tenant;
	
	private Instant lastPoll;
	private Status status = Status.PENDING;
	private String user;
	
	public PendingDevice() {
		super();
	}

	PendingDevice(String deviceCode, String userCode, OAuth2Request request, Tenant tenant) {
		super();
		this.deviceCode = deviceCode;
		this.userCode = userCode;
		this.created = Instant.now();
		this.request = request;
		this.tenant = tenant.getUuid();
	}
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Instant getLastPoll() {
		return lastPoll;
	}

	public void setLastPoll(Instant lastPoll) {
		this.lastPoll = lastPoll;
	}

	public OAuth2Request getRequest() {
		return request;
	}

	public Instant getCreated() {
		return created;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public String getUserCode() {
		return userCode;
	}

	public String getTenant() {
		return tenant;
	}

	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

	public void setRequest(OAuth2Request request) {
		this.request = request;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
}