package com.jadaptive.api.tasks;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Trigger Mapping", resourceKey = TriggerMapping.RESOURCE_KEY, type = EntityType.OBJECT)
public class TriggerMapping extends UUIDEntity {

	public static final String RESOURCE_KEY = "triggerMapping";
	
	public TriggerMapping() {	
	}
	
	public TriggerMapping(String fieldName, String variableExpression) {
		this.fieldName = fieldName;
		this.variableExpression = variableExpression;
	}
	
	@Column(name = "Field Name", 
			description = "The name of the field that will accept this value", 
			type = FieldType.TEXT)
	String fieldName;
	
	@Column(name = "Expression", 
			description = "The value of this variable, or expression used to calculate it", 
			type = FieldType.TEXT_AREA)
	String variableExpression;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getVariableExpression() {
		return variableExpression;
	}

	public void setVariableExpression(String variableExpression) {
		this.variableExpression = variableExpression;
	}

	
}
