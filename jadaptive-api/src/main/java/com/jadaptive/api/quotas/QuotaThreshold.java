package com.jadaptive.api.quotas;

import java.util.concurrent.TimeUnit;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.permissions.FeatureGroup;
import com.jadaptive.api.permissions.LicensedFeature;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.ui.pages.config.ConfigurationItem;

@ObjectDefinition(resourceKey = QuotaThreshold.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "key")
@ConfigurationItem(bundle = QuotaThreshold.RESOURCE_KEY, icon = "fa-traffic-light-stop", resourceKey = QuotaThreshold.RESOURCE_KEY, path = "/app/ui/search/quotaThresholds")
@TableView(defaultColumns = {"key", "periodValue", "periodUnit", "value"})
@ObjectViewDefinition(value = QuotaThreshold.QUOTA_VIEW, weight = 0)
@ObjectViewDefinition(value = QuotaThreshold.OPTIONS_VIEW, weight = 1000)
@LicensedFeature(group = FeatureGroup.PROFESSIONAL, value = "Quotas", includedWithPAYG = true)
public abstract class QuotaThreshold extends AbstractUUIDEntity {

	private static final long serialVersionUID = 1389709000295821056L;
	public static final String RESOURCE_KEY = "quotaThresholds";
	
	public static final String QUOTA_VIEW = "quotaView";
	public static final String OPTIONS_VIEW = "quotaOptions";
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = QuotaKey.RESOURCE_KEY, searchable = true, view = QUOTA_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	QuotaKey key;
		
	@ObjectField(type = FieldType.TEXT, view = QUOTA_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	@Validator(type = ValidationType.BYTESIZE, i18n = "bytesize.info")
	String value;
	
	@ObjectField(type = FieldType.LONG, view = QUOTA_VIEW)
	@Validator(type = ValidationType.RANGE, value = "1-" + Long.MAX_VALUE)
	@Validator(type = ValidationType.REQUIRED)
	long periodValue;
	
	@ObjectField(type = FieldType.ENUM, view = QUOTA_VIEW)
	@Validator(type = ValidationType.REQUIRED)
	TimeUnit periodUnit; 
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public QuotaKey getKey() {
		return key;
	}

	public void setKey(QuotaKey key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getPeriodValue() {
		return periodValue;
	}

	public void setPeriodValue(long periodValue) {
		this.periodValue = periodValue;
	}

	public TimeUnit getPeriodUnit() {
		return periodUnit;
	}

	public void setPeriodUnit(TimeUnit periodUnit) {
		this.periodUnit = periodUnit;
	}

}
