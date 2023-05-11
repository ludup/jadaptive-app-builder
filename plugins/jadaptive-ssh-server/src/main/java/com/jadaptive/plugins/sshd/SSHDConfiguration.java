package com.jadaptive.plugins.sshd;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.sshtools.common.ssh.SecurityLevel;

@ObjectDefinition(resourceKey = SSHDConfiguration.RESOURCE_KEY, type = ObjectType.SINGLETON, system = true)
@ObjectViews({@ObjectViewDefinition(value = SSHDConfiguration.GENERAL_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY),
	@ObjectViewDefinition(value = SSHDConfiguration.AUTHENTICATION_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 50),
	@ObjectViewDefinition(value = SSHDConfiguration.IP_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 100),
	@ObjectViewDefinition(value = SSHDConfiguration.IP_BLACKLIST, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 150),
	@ObjectViewDefinition(value = SSHDConfiguration.FILESYSTEM_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 200),
	@ObjectViewDefinition(value = SSHDConfiguration.FORWARDING_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 250),
	@ObjectViewDefinition(value = SSHDConfiguration.ADVANCED_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 300),
	@ObjectViewDefinition(value = SSHDConfiguration.PROXY_VIEW, bundle = SSHDConfiguration.RESOURCE_KEY, weight = 400)})
public class SSHDConfiguration extends SingletonUUIDEntity {

	private static final long serialVersionUID = -9112641261620391141L;

	public static final String RESOURCE_KEY = "sshdConfiguration";

	public static final String AUTHENTICATION_VIEW = "authentication";
	public static final String IP_VIEW = "ipRules";
	public static final String IP_BLACKLIST = "ipBlacklist";
	public static final String GENERAL_VIEW =  "general";
	public static final String FILESYSTEM_VIEW =  "filesystem";
	public static final String FORWARDING_VIEW =  "forwarding";
	public static final String ADVANCED_VIEW =  "advanced";
	public static final String PROXY_VIEW =  "proxy";
	
	@ObjectField(type = FieldType.ENUM, defaultValue = "STRONG")
	@ObjectView(GENERAL_VIEW)
	SecurityLevel securityLevel;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "600")
	@ObjectView(GENERAL_VIEW)
	Integer idleConnectionTimeoutSecs = 600;

	@ObjectField(type = FieldType.INTEGER, defaultValue = "6144000")
	@ObjectView(ADVANCED_VIEW)
	Integer sftpMaximumWindowSpace = 6144000;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "1048576")
	@ObjectView(ADVANCED_VIEW)
	Integer sftpMinimumWindowSpace = 1048576;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "34000")
	@ObjectView(ADVANCED_VIEW)
	Integer sftpMaximumPacketSize = 34000;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	@ObjectView(FILESYSTEM_VIEW)
	Boolean enableSCP = true;

	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	@ObjectView(AUTHENTICATION_VIEW)
	Boolean enablePassword = true;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "UTF-8")
	@ObjectView(FILESYSTEM_VIEW)
	String sftpCharacterSetEncoding = "UTF-8";
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "15")
	@ObjectView(value = IP_VIEW, weight = 25)
	Integer failedAuthenticationCount = 15;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "5")
	@ObjectView(value = IP_VIEW, weight = 50)
	Integer failedAuthenticationThreshold = 5;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "300")
	@ObjectView(value = IP_VIEW, weight = 100)
	Integer temporaryBanPeriod = 300;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	@ObjectView(value = IP_VIEW, weight = 0)
	Boolean enableBanning = true;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(value = FORWARDING_VIEW, weight = 0)
	Boolean enableLocalForwarding = true;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(FORWARDING_VIEW)
	Collection<String> allowedForwarding;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(IP_BLACKLIST)
	@Validator(bundle = "default", type = ValidationType.REGEX, i18n = "blockedIP.invalid.ipAddress", value = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(3[0-2]|[1-2][0-9]|[0-9]))$|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\\/(12[0-8]|1[0-1][0-9]|[1-9][0-9]|[0-9]))$")
	Collection<String> blockedIPs;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(IP_BLACKLIST)
	@Validator(bundle = "default", type = ValidationType.REGEX, i18n = "allowedIP.invalid.ipAddress", value = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(3[0-2]|[1-2][0-9]|[0-9]))$|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\\/(12[0-8]|1[0-1][0-9]|[1-9][0-9]|[0-9]))$")
	Collection<String> allowedIPs;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(value = ADVANCED_VIEW, weight = 900)
	Boolean keepAlive = false;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(value = ADVANCED_VIEW, weight = 901)
    boolean tcpNoDelay = false;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "0")
	@ObjectView(value = ADVANCED_VIEW, weight = 200)
    int receiveBufferSize = 0;
	
	@ObjectField(type = FieldType.INTEGER, defaultValue = "0")
	@ObjectView(value = ADVANCED_VIEW, weight = 201)
    int sendBufferSize = 0;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(PROXY_VIEW)
	Boolean proxyProtocolEnabled = false;
	
	@ObjectField(type = FieldType.TEXT, defaultValue = "")
	@ObjectView(PROXY_VIEW)
	@Validator(bundle = RESOURCE_KEY, type = ValidationType.REGEX, i18n = "allowedIP.invalid.ipAddress", value = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(\\/(3[0-2]|[1-2][0-9]|[0-9]))$|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*|^s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]d|1dd|[1-9]?d)(.(25[0-5]|2[0-4]d|1dd|[1-9]?d)){3}))|:)))(%.+)?s*(\\/(12[0-8]|1[0-1][0-9]|[1-9][0-9]|[0-9]))$")
	Collection<String> loadBalancerIPs;
	
	public SecurityLevel getSecurityLevel() {
		return securityLevel;
	}

	public void setSecurityLevel(SecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
	}

	public Integer getIdleConnectionTimeoutSecs() {
		return idleConnectionTimeoutSecs;
	}

	public void setIdleConnectionTimeoutSecs(Integer idleConnectionTimeoutSecs) {
		this.idleConnectionTimeoutSecs = idleConnectionTimeoutSecs;
	}

	public Boolean getEnableSCP() {
		return enableSCP;
	}

	public void setEnableSCP(Boolean enableSCP) {
		this.enableSCP = enableSCP;
	}

	public Boolean getEnablePassword() {
		return enablePassword;
	}

	public void setEnablePassword(Boolean enablePassword) {
		this.enablePassword = enablePassword;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Integer getSftpMaximumWindowSpace() {
		return sftpMaximumWindowSpace;
	}

	public void setSftpMaximumWindowSpace(Integer sftpMaximumWindowSpace) {
		this.sftpMaximumWindowSpace = sftpMaximumWindowSpace;
	}

	public Integer getSftpMinimumWindowSpace() {
		return sftpMinimumWindowSpace;
	}

	public void setSftpMinimumWindowSpace(Integer sftpMinimumWindowSpace) {
		this.sftpMinimumWindowSpace = sftpMinimumWindowSpace;
	}

	public Integer getSftpMaximumPacketSize() {
		return sftpMaximumPacketSize;
	}

	public void setSftpMaximumPacketSize(Integer sftpMaximumPacketSize) {
		this.sftpMaximumPacketSize = sftpMaximumPacketSize;
	}

	public String getSftpCharacterSetEncoding() {
		return sftpCharacterSetEncoding;
	}

	public void setSftpCharacterSetEncoding(String sftpCharacterSetEncoding) {
		this.sftpCharacterSetEncoding = sftpCharacterSetEncoding;
	}

	public Integer getFailedAuthenticationCount() {
		return failedAuthenticationCount;
	}

	public void setFailedAuthenticationCount(Integer failedAuthenticationCount) {
		this.failedAuthenticationCount = failedAuthenticationCount;
	}

	public Integer getFailedAuthenticationThreshold() {
		return failedAuthenticationThreshold;
	}

	public void setFailedAuthenticationThreshold(Integer failedAuthenticationThreshold) {
		this.failedAuthenticationThreshold = failedAuthenticationThreshold;
	}

	public Integer getTemporaryBanPeriod() {
		return temporaryBanPeriod;
	}

	public void setTemporaryBanPeriod(Integer temporaryBanPeriod) {
		this.temporaryBanPeriod = temporaryBanPeriod;
	}

	public Collection<String> getBlockedIPs() {
		return blockedIPs;
	}

	public void setBlockedIPs(Collection<String> blockedIPs) {
		this.blockedIPs = blockedIPs;
	}

	public Collection<String> getAllowedIPs() {
		return allowedIPs;
	}

	public void setAllowedIPs(Collection<String> allowedIPs) {
		this.allowedIPs = allowedIPs;
	}

	public Boolean getKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(Boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
	}

	public Boolean getEnableBanning() {
		return enableBanning;
	}

	public void setEnableBanning(Boolean enableBanning) {
		this.enableBanning = enableBanning;
	}

	public Boolean getEnableLocalForwarding() {
		return enableLocalForwarding;
	}

	public void setEnableLocalForwarding(Boolean enableLocalForwarding) {
		this.enableLocalForwarding = enableLocalForwarding;
	}

	public Collection<String> getAllowedForwarding() {
		return allowedForwarding;
	}

	public void setAllowedForwarding(Collection<String> allowedForwarding) {
		this.allowedForwarding = allowedForwarding;
	}

	public Boolean getProxyProtocolEnabled() {
		return proxyProtocolEnabled;
	}

	public void setProxyProtocolEnabled(Boolean proxyProtocolEnabled) {
		this.proxyProtocolEnabled = proxyProtocolEnabled;
	}

	public Collection<String> getLoadBalancerIPs() {
		return loadBalancerIPs;
	}

	public void setLoadBalancerIPs(Collection<String> loadBalancerIPs) {
		this.loadBalancerIPs = loadBalancerIPs;
	}
}
