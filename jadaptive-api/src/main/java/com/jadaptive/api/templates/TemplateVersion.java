package com.jadaptive.api.templates;

import java.util.Date;
import java.util.Objects;

import com.jadaptive.api.entity.EntityScope;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Template Version", resourceKey = "templateVersion", scope = EntityScope.GLOBAL, type = EntityType.COLLECTION)
public class TemplateVersion extends AbstractUUIDEntity {

	@Column(name = "Version", 
			description = "The version of this template", 
			required = true,
			searchable = true,
			type = FieldType.TEXT)
	String version; 
	
	@Column(name = "Timestamp", 
			description = "The timestamp this template was installed", 
			required = true,
			type = FieldType.DATE)
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

}
