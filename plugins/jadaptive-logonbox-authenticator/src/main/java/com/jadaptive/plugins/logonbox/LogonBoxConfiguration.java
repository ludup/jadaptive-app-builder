package com.jadaptive.plugins.logonbox;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = LogonBoxConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON)
@ObjectViews({@ObjectViewDefinition(value = LogonBoxConfiguration.GENERAL_VIEW, bundle = "authenticator")})
public class LogonBoxConfiguration  extends SingletonUUIDEntity {

	private static final long serialVersionUID = -8399924920280451466L;
	
	public static final String RESOURCE_KEY = "authenticator";
	
	public static final String GENERAL_VIEW = "general";
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(value = GENERAL_VIEW)
	String directoryHostname;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "443")
	@ObjectView(value = GENERAL_VIEW)
	int directoryPort;
	
	@ObjectField(type = FieldType.TEXT_AREA, defaultValue = "")
	@ObjectView(value = GENERAL_VIEW)
	String authorizePrompt;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "Login")
	@ObjectView(value = GENERAL_VIEW)
	String authorizeAction;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "JADAPTIVE Server")
	@ObjectView(value = GENERAL_VIEW)
	String applicationName;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(value = GENERAL_VIEW)
	Boolean debug;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}

	public void setDirectoryHostname(String directoryHostname) {
		this.directoryHostname = directoryHostname;
	}

	public int getDirectoryPort() {
		return directoryPort;
	}

	public void setDirectoryPort(int directoryPort) {
		this.directoryPort = directoryPort;
	}

	public String getAuthorizePrompt() {
		return authorizePrompt;
	}

	public void setAuthorizePrompt(String authorizePrompt) {
		this.authorizePrompt = authorizePrompt;
	}

	public String getAuthorizeAction() {
		return authorizeAction;
	}

	public void setAuthorizeAction(String authorizeAction) {
		this.authorizeAction = authorizeAction;
	}

	public Boolean getDebug() {
		return debug;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
