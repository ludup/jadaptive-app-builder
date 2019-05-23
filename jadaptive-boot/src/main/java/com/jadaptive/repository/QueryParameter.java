package com.jadaptive.repository;

public class QueryParameter {

	String column;
	String value;
	
	public QueryParameter(String column, String value) {
		this.column = column;
		this.value = value;
	}

	public String getColumn() {
		return column;
	}

	public String getValue() {
		return value;
	}
}
