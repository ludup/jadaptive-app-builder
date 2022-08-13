package com.jadaptive.api.stats;

import java.util.Collection;
import java.util.Date;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = Usage.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION/*, creatable = false, updatable = false, deletable = false*/)
@TableView(defaultColumns = { "timestamp",  "keys", "value" })
public class Usage extends AbstractUUIDEntity {

	private static final long serialVersionUID = 2886275420185558997L;

	public static final String RESOURCE_KEY = "usage";

	@ObjectField(type = FieldType.TIMESTAMP)
	Date timestamp;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	Collection<String> keys;
	
	@ObjectField(type = FieldType.LONG)
	@Validator(type = ValidationType.REQUIRED)
	Long value;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Collection<String> getKeys() {
		return keys;
	}

	public void setKeys(Collection<String> keys) {
		this.keys = keys;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
