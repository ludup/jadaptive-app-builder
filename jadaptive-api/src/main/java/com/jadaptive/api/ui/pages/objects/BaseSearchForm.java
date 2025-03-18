package com.jadaptive.api.ui.pages.objects;

public interface BaseSearchForm {

	String getSortOrder();
	String getSortColumn();
	String getSearchValue();
	int getStart();
	int getLength();
}
