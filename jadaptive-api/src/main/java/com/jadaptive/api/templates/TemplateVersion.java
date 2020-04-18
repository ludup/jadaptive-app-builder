package com.jadaptive.api.templates;

import java.util.Date;
import java.util.Objects;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Template Version", resourceKey = TemplateVersion.RESOURCE_KEY, scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
public class TemplateVersion extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "templateVersion";

	@Column(name = "Version", 
			description = "The version of this template", 
			required = true,
			searchable = true,
			type = FieldType.TEXT)
	String version; 
	
	@Column(name = "Timestamp", 
			description = "The timestamp this template was installed", 
			required = true,
			type = FieldType.TIMESTAMP)
	Date timestamp;
	
	public Date getTimestamp() {
		return Objects.isNull(timestamp) ? new Date() : timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version.replace(".json", "");
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}
