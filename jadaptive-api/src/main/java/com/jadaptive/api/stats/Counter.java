package com.jadaptive.api.stats;

import java.util.Date;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = Counter.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@TableView(defaultColumns = { "date",  "key", "value" })
public class Counter extends AbstractUUIDEntity {

	private static final long serialVersionUID = -8552673621693434319L;
	
	@ObjectField(type = FieldType.DATE)
	Date date;
	
	@ObjectField(type = FieldType.TEXT)
	String key;
	
	@ObjectField(type = FieldType.LONG)
	long value;
	
	public static final String RESOURCE_KEY = "counter";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	
	
}
