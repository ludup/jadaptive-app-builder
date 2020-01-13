package com.jadaptive.api.db;

import java.util.Collection;

import org.springframework.util.StringUtils;

public class SearchField {

	public enum Type { EQUALS, LIKE, IN }
	
	String searchField;
	String[] searchValue;
	Type type;
	
	
	private SearchField(Type type, String searchField, String... searchValue) {
		super();
		if(StringUtils.isEmpty(searchField) || searchField.equalsIgnoreCase("UUID")) {
			searchField = "_id";
		}
		this.searchField = searchField;
		this.searchField = searchField;
		this.searchValue = searchValue;
		this.type = type;
	}

	public String getSearchField() {
		return searchField;
	}
	
	public String[] getSearchValue() {
		return searchValue;
	}
	
	public Type getSearchType() {
		return type;
	}
	
	public static SearchField eq(String searchField, String searchValue) {
		return new SearchField(Type.EQUALS, searchField, searchValue);
	}
	
	public static SearchField in(String searchField, String... searchValue) {
		return new SearchField(Type.IN, searchField, searchValue);
	}
	
	public static SearchField in(String searchField, Collection<String> searchValue) {
		return new SearchField(Type.IN, searchField, searchValue.toArray(new String[0]));
	}
	
	public static SearchField like(String searchField, String searchValue) {
		return new SearchField(Type.LIKE, searchField, searchValue);
	}
}
