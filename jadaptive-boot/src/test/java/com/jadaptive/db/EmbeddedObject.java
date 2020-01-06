package com.jadaptive.db;

import java.util.Date;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class EmbeddedObject extends AbstractUUIDEntity {

	String embeddedString;
	Long embeddedLong;
	Integer embeddedInt;
	Float embeddedFloat;
	Double embeddedDouble;
	Date embeddedDate;
	TestEnum embeddedEnum;
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

	
}
