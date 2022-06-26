package com.jadaptive.api.templates;

import java.util.Date;
import java.util.Objects;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = TemplateVersion.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class TemplateVersion extends AbstractUUIDEntity {

	private static final long serialVersionUID = -6308059064398663312L;

	public static final String RESOURCE_KEY = "templateVersion";

	@ObjectField(searchable = true, type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String version;

	@ObjectField(type = FieldType.TIMESTAMP)
	@Validator(type = ValidationType.REQUIRED)
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
