package com.jadaptive.db;

import java.util.Date;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = EmbeddedObject.RESOURCE_KEY, type = ObjectType.OBJECT)
public class EmbeddedObject extends AbstractUUIDEntity {

	private static final long serialVersionUID = 1050140978723123850L;

	public static final String RESOURCE_KEY = "embeddedObject";

	@ObjectField(type = FieldType.TEXT)
	String embeddedString;
	
	@ObjectField(type = FieldType.LONG)
	Long embeddedLong;
	
	@ObjectField(type = FieldType.INTEGER)
	Integer embeddedInt;
	
	@ObjectField(type = FieldType.DECIMAL)
	Float embeddedFloat;
	
	@ObjectField(type = FieldType.DECIMAL)
	Double embeddedDouble;
	
	@ObjectField(type = FieldType.TIMESTAMP)
	Date embeddedDate;
	
	@ObjectField(type = FieldType.ENUM)
	TestEnum embeddedEnum;
	
	@ObjectField(type = FieldType.BOOL)
	Boolean embeddedBool;
	
	public EmbeddedObject() {

	}

	public EmbeddedObject(String embeddedString, Long embeddedLong, Integer embeddedInt, Float embeddedFloat,
			Double embeddedDouble, Date embeddedDate, TestEnum embeddedEnum, Boolean embeddedBool) {
		super();
		this.embeddedString = embeddedString;
		this.embeddedLong = embeddedLong;
		this.embeddedInt = embeddedInt;
		this.embeddedFloat = embeddedFloat;
		this.embeddedDouble = embeddedDouble;
		this.embeddedDate = embeddedDate;
		this.embeddedEnum = embeddedEnum;
		this.embeddedBool = embeddedBool;
	}



	public String getEmbeddedString() {
		return embeddedString;
	}

	public void setEmbeddedString(String embeddedString) {
		this.embeddedString = embeddedString;
	}

	public Long getEmbeddedLong() {
		return embeddedLong;
	}

	public void setEmbeddedLong(Long embeddedLong) {
		this.embeddedLong = embeddedLong;
	}

	public Integer getEmbeddedInt() {
		return embeddedInt;
	}

	public void setEmbeddedInt(Integer embeddedInt) {
		this.embeddedInt = embeddedInt;
	}

	public Float getEmbeddedFloat() {
		return embeddedFloat;
	}

	public void setEmbeddedFloat(Float embeddedFloat) {
		this.embeddedFloat = embeddedFloat;
	}

	public Double getEmbeddedDouble() {
		return embeddedDouble;
	}

	public void setEmbeddedDouble(Double embeddedDouble) {
		this.embeddedDouble = embeddedDouble;
	}

	public Date getEmbeddedDate() {
		return embeddedDate;
	}

	public void setEmbeddedDate(Date embeddedDate) {
		this.embeddedDate = embeddedDate;
	}

	public TestEnum getEmbeddedEnum() {
		return embeddedEnum;
	}

	public void setEmbeddedEnum(TestEnum embeddedEnum) {
		this.embeddedEnum = embeddedEnum;
	}

	public Boolean getEmbeddedBool() {
		return embeddedBool;
	}

	public void setEmbeddedBool(Boolean embeddedBool) {
		this.embeddedBool = embeddedBool;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	
}
