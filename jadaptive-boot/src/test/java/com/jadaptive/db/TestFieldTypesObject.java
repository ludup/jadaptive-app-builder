package com.jadaptive.db;

import java.util.Date;

import com.jadaptive.app.repository.AbstractUUIDEntity;

public class TestFieldTypesObject extends AbstractUUIDEntity {

	String string;
	Long longNumber;
	Integer intNumber;
	Float floatNumber;
	Double doubleNumber;
	Date date;
	TestEnum enumField;
	Boolean bool;
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

	
}
