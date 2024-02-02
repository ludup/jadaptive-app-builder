package com.jadaptive.api.session;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

@ObjectDefinition(resourceKey = Session.RESOURCE_KEY, scope = ObjectScope.GLOBAL, creatable = false, updatable = false)
@ObjectServiceBean(bean = SessionService.class)
@TableView(defaultColumns = { "user", "signedIn", "remoteAddress", "type", "state", "userAgent"})
public class Session extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3842259533277443038L;

	public static final String RESOURCE_KEY = "sessions";
	
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	String remoteAddress;
	
	@ObjectField(type = FieldType.ENUM, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	SessionType type;
	
	@ObjectField(type = FieldType.ENUM, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	SessionState state;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	@Validator(type = ValidationType.REQUIRED)
	Date lastUpdated;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	@Validator(type = ValidationType.REQUIRED)
	Date signedIn;
	
	@ObjectField(type = FieldType.TIMESTAMP, searchable = true)
	Date signedOut;
		
	@ObjectField(type = FieldType.TEXT, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	String userAgent;
	
	@ObjectField(type = FieldType.INTEGER)
	@Validator(type = ValidationType.REQUIRED)
	Integer sessionTimeout;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = User.RESOURCE_KEY, searchable = true)
	@Validator(type = ValidationType.REQUIRED)
	User user;
	
	Tenant tenant;
	
	User impersonatingUser;
	Tenant impersontatingTenant;
	Map<String,Object> attrs = new HashMap<>();
	
	public Session() {

	}

	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public SessionType getType() {
		return type;
	}

	public void setType(SessionType type) {
		this.type = type;
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
		if(isImpersontating()) {
			return impersontatingTenant;
		}
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
		if(isImpersontating()) {
			return impersonatingUser;
		}
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public SessionState getState() {
		return state;
	}

	public void setState(SessionState state) {
		this.state = state;
	}

	public boolean isClosed() {
		return signedOut!=null;
	}

	public User getImpersonatingUser() {
		return impersonatingUser;
	}

	public void setImpersonatingUser(User impersonatingUser) {
		this.impersonatingUser = impersonatingUser;
	}

	public Tenant getImpersontatingTenant() {
		return impersontatingTenant;
	}

	public void setImpersontatingTenant(Tenant impersontatingTenant) {
		this.impersontatingTenant = impersontatingTenant;
	}

	public boolean isImpersontating() {
		return Objects.nonNull(impersonatingUser);
	}
	
	public Object getAttribute(String name) {
		return attrs.get(name);
	}
	
	public void setAttribute(String name, Object value) {
		attrs.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Class<T> key) {
		return (T)attrs.get(key.getName());
	}
	
	public <T> void setAttribute(Class<T> key, T value) {
		attrs.put(key.getName(), value);
	}
	
	public void removeAttribute(Class<?> key) {
		attrs.remove(key.getName());
	}
	
	public void removeAttribute(String name) {
		attrs.remove(name);
	}
	
	public Map<String,Object> getAttributes() {
		return attrs;
	}
}
