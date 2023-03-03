package com.jadaptive.api.ui;

public class NamePairValue implements Comparable<NamePairValue> {

	String name;
	String value;
	
	public NamePairValue(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int compareTo(NamePairValue o) {
		return name.compareTo(o.getName());
	}
	
	
}
