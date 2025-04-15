package com.jadaptive.app.auth.oauth2;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.ui.pages.config.ConfigurationItem;

@ObjectDefinition(bundle = OAuth2Configuration.RESOURCE_KEY, resourceKey = OAuth2Configuration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ObjectViews({ 
	@ObjectViewDefinition(value = OAuth2Configuration.OPTIONS_VIEW, bundle = OAuth2Configuration.RESOURCE_KEY)})
@ConfigurationItem(system = true, icon = "fa-handshake-simple")
public class OAuth2Configuration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -7608027441233694463L;

	public static final String RESOURCE_KEY = "oauth2";
	public final static String UPDATES = "Updates";
	public static final String OPTIONS_VIEW = "options";
	public static final String DEVICE_VIEW = "device";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false", view = OPTIONS_VIEW, weight = 0)
	private boolean requireApplicationRegistration;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true", view = OPTIONS_VIEW, weight = 10)
	/* TODO only show when requireApplicationRegistration is false */
	private boolean issueRefreshTokenForUnregisteredRequests = true;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "3600", view = OPTIONS_VIEW, weight = 20)
	private int defaultExpiryTime = 3600;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "1800", view = DEVICE_VIEW, weight = 10)
	private int deviceCodeExpiryTime = 1800;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "5", view = DEVICE_VIEW, weight = 20)
	private int deviceCodeInterval = 5;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "AAAAAA", view = DEVICE_VIEW, weight = 30)
	private String deviceCodePattern = "AAAAAA";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getDeviceCodePattern() {
		return deviceCodePattern;
	}

	public void setDeviceCodePattern(String deviceCodePattern) {
		this.deviceCodePattern = deviceCodePattern;
	}

	public int getDeviceCodeInterval() {
		return deviceCodeInterval;
	}

	public void setDeviceCodeInterval(int deviceCodeInterval) {
		this.deviceCodeInterval = deviceCodeInterval;
	}

	public int getDeviceCodeExpiryTime() {
		return deviceCodeExpiryTime;
	}

	public void setDeviceCodeExpiryTime(int deviceCodeExpiryTime) {
		this.deviceCodeExpiryTime = deviceCodeExpiryTime;
	}

	public boolean isRequireApplicationRegistration() {
		return requireApplicationRegistration;
	}

	public void setRequireApplicationRegistration(boolean requireApplicationRegistration) {
		this.requireApplicationRegistration = requireApplicationRegistration;
	}

	public boolean isIssueRefreshTokenForUnregisteredRequests() {
		return issueRefreshTokenForUnregisteredRequests;
	}

	public void setIssueRefreshTokenForUnregisteredRequests(boolean issueRefreshTokenForUnregisteredRequests) {
		this.issueRefreshTokenForUnregisteredRequests = issueRefreshTokenForUnregisteredRequests;
	}

	public int getDefaultExpiryTime() {
		return defaultExpiryTime;
	}

	public void setDefaultExpiryTime(int defaultExpiryTime) {
		this.defaultExpiryTime = defaultExpiryTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}
