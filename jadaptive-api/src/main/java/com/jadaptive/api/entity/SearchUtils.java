package com.jadaptive.api.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.utils.Utils;

public class SearchUtils {

	public static SearchField[] generateSearch(String searchField, String searchValue, 
			ObjectTemplate template, SearchField... additional) {
		return generateSearch(searchField, searchValue, template.toMap(), additional);
	}
	
	public static SearchField[] generateSearch(String searchField, String searchValue,
			Map<String,FieldTemplate> searchFieldTemplates, SearchField... additional) {
		List<SearchField> fields = new ArrayList<>();
		if(StringUtils.isNotBlank(searchValue)) {
			FieldTemplate f = searchFieldTemplates.get(searchField);
			if(Objects.nonNull(f)) {
				switch(f.getFieldType()) {
				case OBJECT_REFERENCE:
					if(Utils.isUUID(searchValue)) {
						if(f.getCollection()) {
							fields.add(SearchField.in(searchField + ".uuid", searchValue));
						} else {
							fields.add(SearchField.eq(searchField + ".uuid", searchValue));
						}
					} else {
						if(f.getCollection()) {
							fields.add(SearchField.like(searchField + ".name", searchValue));
						} else {
							fields.add(SearchField.like(searchField + ".name", searchValue));
						}
					}
					
					break;
				
				case DECIMAL:
				{
					if(searchValue.startsWith(">=")) {
						fields.add(SearchField.gte(searchField, Double.parseDouble(searchValue.substring(2))));
					} else if(searchValue.startsWith("<=")) {
						fields.add(SearchField.lte(searchField, Double.parseDouble(searchValue.substring(2))));
					} else if(searchValue.startsWith(">")) {
						fields.add(SearchField.gt(searchField, Double.parseDouble(searchValue.substring(1))));
					} else if(searchValue.startsWith("<")) {
						fields.add(SearchField.lt(searchField, Double.parseDouble(searchValue.substring(1))));
					} else {
						fields.add(SearchField.eq(searchField, Double.parseDouble(searchValue)));
					}
					break;
				}
				case INTEGER:
				{
					if(searchValue.startsWith(">=")) {
						fields.add(SearchField.gte(searchField, Integer.parseInt(searchValue.substring(2))));
					} else if(searchValue.startsWith("<=")) {
						fields.add(SearchField.lte(searchField, Integer.parseInt(searchValue.substring(2))));
					} else if(searchValue.startsWith(">")) {
						fields.add(SearchField.gt(searchField, Integer.parseInt(searchValue.substring(1))));
					} else if(searchValue.startsWith("<")) {
						fields.add(SearchField.lt(searchField, Integer.parseInt(searchValue.substring(1))));
					} else {
						fields.add(SearchField.eq(searchField, Integer.parseInt(searchValue)));
					}
					break;
				}
				case LONG:
				{
					if(searchValue.startsWith(">=")) {
						fields.add(SearchField.gte(searchField, Long.parseLong(searchValue.substring(2))));
					} else if(searchValue.startsWith("<=")) {
						fields.add(SearchField.lte(searchField, Long.parseLong(searchValue.substring(2))));
					} else if(searchValue.startsWith(">")) {
						fields.add(SearchField.gt(searchField, Long.parseLong(searchValue.substring(1))));
					} else if(searchValue.startsWith("<")) {
						fields.add(SearchField.lt(searchField, Long.parseLong(searchValue.substring(1))));
					} else {
						fields.add(SearchField.eq(searchField, Long.parseLong(searchValue)));
					}
					break;
				}
				case TIMESTAMP:
				case DATE:
					if(searchValue.startsWith(">=")) {
						fields.add(SearchField.gte(searchField, Utils.parseDate(searchValue.substring(2), "yyyy-MM-dd")));
					} else if(searchValue.startsWith("<=")) {
						fields.add(SearchField.lte(searchField, Utils.parseDate(searchValue.substring(2), "yyyy-MM-dd")));
					} else if(searchValue.startsWith(">")) {
						fields.add(SearchField.gt(searchField, Utils.parseDate(searchValue.substring(1), "yyyy-MM-dd")));
					} else if(searchValue.startsWith("<")) {
						fields.add(SearchField.lt(searchField, Utils.parseDate(searchValue.substring(1), "yyyy-MM-dd")));
					} else {
						fields.add(SearchField.eq(searchField, Utils.parseDate(searchValue, "yyyy-MM-dd")));
					}
					break;
				case BOOL:
					fields.add(SearchField.eq(searchField, Boolean.parseBoolean(searchValue)));
					break;
				default:
					fields.add(SearchField.like(searchField, searchValue));
					break;
				}
			}
		}
		fields.addAll(Arrays.asList(additional));
		return fields.toArray(new SearchField[0]);
	}

	public static SearchField[] combine(SearchField[] fields, SearchField field) {
		
		List<SearchField> tmp = new ArrayList<>();
		tmp.addAll(Arrays.asList(fields));
		tmp.add(field);
		return tmp.toArray(new SearchField[0]);
	}
}
