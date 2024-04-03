package com.jadaptive.api.quotas;

import java.util.Collection;

import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = IPQuota.RESOURCE_KEY, bundle = QuotaThreshold.RESOURCE_KEY)
@ObjectViewDefinition(value = IPQuota.IP_VIEW, weight = 99999)
@GenerateEventTemplates
public class IPQuota extends QuotaThreshold {

	private static final long serialVersionUID = 6344677508192690706L;
	public static final String RESOURCE_KEY = "ipQuotas";
	public static final String IP_VIEW = "ipQuotaView";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(IP_VIEW)
	Boolean allAddresses;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = IP_VIEW, dependsOn = "allAddresses", dependsValue = "false")
	@Validator(type = ValidationType.IPV4)
	@Validator(type = ValidationType.IPV6)
	@Validator(type = ValidationType.CIDR_V4)
	@Validator(type = ValidationType.CIDR_V6)
	Collection<String> ipAddresses;

	public Collection<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(Collection<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Boolean getAllAddresses() {
		return allAddresses;
	}

	public void setAllAddresses(Boolean allAddresses) {
		this.allAddresses = allAddresses;
	}
	
}
