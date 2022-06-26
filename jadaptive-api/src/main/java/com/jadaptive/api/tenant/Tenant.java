package com.jadaptive.api.tenant;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.utils.Utils;

@ObjectDefinition(resourceKey = Tenant.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, system = true)
@ObjectServiceBean(bean = TenantService.class)
@ObjectViews({ 
	@ObjectViewDefinition(value = Tenant.DOMAINS_VIEW, bundle = Tenant.RESOURCE_KEY)})
@TableView(defaultColumns = { "name", "hostname" })
public class Tenant extends AbstractUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 1567817173441528990L;

	public static final String RESOURCE_KEY = "tenant";
	
	public static final String DOMAINS_VIEW = "domains";
	
	@ObjectField(type = FieldType.TEXT)
	String name;
	
	@ObjectField(type = FieldType.TEXT)
	String hostname;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String ownerName;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REGEX, value = Utils.EMAIL_PATTERN)
	String ownerEmail;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = DOMAINS_VIEW)
	Collection<String> alternativeDomains = new HashSet<>();
	
	public Tenant() {
		
	}
	
	public Tenant(String uuid, String name, String hostname, boolean system) {
		this(uuid, name, hostname);
		setSystem(system);
	}

	public Tenant(String uuid, String name, String hostname) {
		this.setUuid(uuid);
		this.name = name;
		this.hostname = hostname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDomain() {
		return hostname;
	}

	public void setDomain(String domain) {
		this.hostname = domain;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String domain) {
		this.hostname = domain;
	}

	public Collection<String> getAlternativeDomains() {
		return alternativeDomains;
	}

	public void setAlternativeDomains(Collection<String> alternativeDomains) {
		this.alternativeDomains = alternativeDomains;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
