package com.jadaptive.api.session;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.tenant.Tenant;

public class Session extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "session";
	
	String remoteAddress;
	Date lastUpdated;
	Date signedIn;
	Date signedOut;
	Tenant tenant;
	String userAgent;
	Integer sessionTimeout;
	String username;
	String csrfToken; 
	
	public Session() {

	}

	public String getId() {
		/**
		 * For compatibility with Hypersocket
		 */
		return getUuid();
	}
	
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getSignedIn() {
		return signedIn;
	}

	public void setSignedIn(Date signedIn) {
		this.signedIn = signedIn;
	}

	public Date getSignedOut() {
		return signedOut;
	}

	public void setSignedOut(Date signedOut) {
		this.signedOut = signedOut;
	}

	@JsonIgnore
	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Integer getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCsrfToken() {
		return csrfToken;
	}

	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}
	
	@JsonIgnore
	public boolean isReadyForUpdate() {
		// We save our state every minute
		if(lastUpdated==null) {
			return true;
		}
		return System.currentTimeMillis() - lastUpdated.getTime() > 60000L;
	}

	public boolean isClosed() {
		return signedOut!=null;
	}
}
