package com.jadaptive.api.countries;

public class Country {

	String name;
	String code;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getResourceKey() {
		return getCode();
	}
	
	

}
