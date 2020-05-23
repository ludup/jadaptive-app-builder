package com.jadaptive.db;

import java.util.Date;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Test Field Types Object", resourceKey = TestFieldTypesObject.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestFieldTypesObject extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "testFieldTypesObject";

	@Column(name = "String", description = "A string", type = FieldType.TEXT)
	String string;
	
	@Column(name = "Long", description = "A long", type = FieldType.LONG)
	Long longNumber;
	
	@Column(name = "Integer", description = "An int", type = FieldType.INTEGER)
	Integer intNumber;
	
	@Column(name = "Float", description = "A float", type = FieldType.DECIMAL)
	Float floatNumber;
	
	@Column(name = "Double", description = "A double", type = FieldType.DECIMAL)
	Double doubleNumber;
	
	@Column(name = "Date", description = "A date", type = FieldType.TIMESTAMP)
	Date date;
	
	@Column(name = "Enum", description = "An enum", type = FieldType.ENUM)
	TestEnum enumField;
	
	@Column(name = "Bool", description = "A boolean", type = FieldType.BOOL)
	Boolean bool;
	
	@Column(name = "Object", description = "An object", type = FieldType.OBJECT_EMBEDDED)
	EmbeddedObject embedded;
	
	public TestFieldTypesObject() {
	}

	public TestFieldTypesObject(String string, Long longNumber, Integer intNumber, 
			Float floatNumber, Double doubleNumber, Date date, TestEnum enumField,
			Boolean bool, EmbeddedObject embedded) {
		super();
		this.string = string;
		this.longNumber = longNumber;
		this.intNumber = intNumber;
		this.floatNumber = floatNumber;
		this.doubleNumber = doubleNumber;
		this.date = date;
		this.enumField = enumField;
		this.bool = bool;
		this.embedded = embedded;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public Long getLongNumber() {
		return longNumber;
	}

	public void setLongNumber(Long longNumber) {
		this.longNumber = longNumber;
	}

	public Integer getIntNumber() {
		return intNumber;
	}

	public void setIntNumber(Integer intNumber) {
		this.intNumber = intNumber;
	}

	public Float getFloatNumber() {
		return floatNumber;
	}

	public void setFloatNumber(Float floatNumber) {
		this.floatNumber = floatNumber;
	}

	public Double getDoubleNumber() {
		return doubleNumber;
	}

	public void setDoubleNumber(Double doubleNumber) {
		this.doubleNumber = doubleNumber;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public TestEnum getEnumField() {
		return enumField;
	}

	public void setEnumField(TestEnum enumField) {
		this.enumField = enumField;
	}

	public Boolean getBool() {
		return bool;
	}

	public void setBool(Boolean bool) {
		this.bool = bool;
	}

	public EmbeddedObject getEmbedded() {
		return embedded;
	}

	public void setEmbedded(EmbeddedObject embedded) {
		this.embedded = embedded;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	
}
