package com.jadaptive.api.session;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

@ObjectDefinition(resourceKey = Session.RESOURCE_KEY, scope = ObjectScope.GLOBAL)
public class Session extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3842259533277443038L;

	public static final String RESOURCE_KEY = "sessions";
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String remoteAddress;
	
	@ObjectField(type = FieldType.TIMESTAMP, required = true)
	Date lastUpdated;
	
	@ObjectField(type = FieldType.TIMESTAMP, required = true)
	Date signedIn;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	Date signedOut;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE)
	Tenant tenant;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String userAgent;
	
	@ObjectField(type = FieldType.INTEGER, required = true)
	Integer sessionTimeout;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, required = true)
	User user;
	
	@ObjectField(type = FieldType.HIDDEN, required = true)
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
