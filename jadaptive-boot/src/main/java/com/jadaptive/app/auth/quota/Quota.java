package com.jadaptive.app.auth.quota;

import java.util.Date;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.quotas.QuotaKey;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.UniqueIndex;

@ObjectDefinition(resourceKey = Quota.RESOURCE_KEY, 
	hidden = true, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@UniqueIndex(columns = { "key.uuid", "group"})
public class Quota extends AbstractUUIDEntity {

	private static final long serialVersionUID = 2226026565046618484L;

	public static final String RESOURCE_KEY = "quota";
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = QuotaKey.RESOURCE_KEY)
	QuotaKey key;
	
	@ObjectField(type = FieldType.TEXT)
	
	String group;
	
	@ObjectField(type = FieldType.LONG)
	long quota;
	
	@ObjectField(type = FieldType.LONG)
	long period;
	
	@ObjectField(type = FieldType.LONG)
	long value;

	@ObjectField(type = FieldType.TIMESTAMP)
	Date quotaStarted;
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public void setKey(QuotaKey key) {
		this.key = key;
	}

	public QuotaKey getKey() {
		return key;
	}
	
	public long getQuota() {
		return quota;
	}

	public void setQuota(long quota) {
		this.quota = quota;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public Date getQuotaStarted() {
		return quotaStarted;
	}

	public void setQuotaStarted(Date quotaStarted) {
		this.quotaStarted = quotaStarted;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}
