package com.jadaptive.api.db;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.ObjectUtils;

import com.jadaptive.api.template.SearchTransformer;

public class SearchField {

	public enum Type { EQUALS, LIKE, IN, NOT, OR, AND, GT, LT, GTE, LTE, ALL }
	
	String searchField;
	Object[] searchValue;
	Type type;
	SearchField[] fields;
	private boolean marked;
	
	public SearchField(Type type, SearchField... fields) {
		switch(type) {
		case OR:
		case AND:
			break;
		default:
			throw new IllegalArgumentException("You cannot use child fields with type other than OR or AND");
		}
		this.fields = fields;
		this.type = type;
	}
	
	public SearchField(Type type, String searchField, Object... searchValue) {
		super();
		if(ObjectUtils.isEmpty(searchField) || searchField.equalsIgnoreCase("UUID")) {
			searchField = "_id";
		}
		this.searchField = searchField;
		this.searchField = searchField;
		this.searchValue = searchValue;
		this.type = type;
	}

	public SearchField[] getFields() {
		return fields;
	}
	
	public String getColumn() {
		return searchField;
	}
	
	public Object[] getValue() {
		return searchValue;
	}
	
	public Type getSearchType() {
		return type;
	}
	
	public static SearchField eq(String searchField, Object searchValue) {
		return new SearchField(Type.EQUALS, searchField, searchValue);
	}
	
	public static SearchField not(String searchField, Object searchValue) {
		return new SearchField(Type.NOT, searchField, searchValue);
	}
	
	public static SearchField in(String searchField, Object... searchValue) {
		return new SearchField(Type.IN, searchField, searchValue);
	}
	
	public static SearchField in(String searchField, Collection<String> searchValue) {
		return new SearchField(Type.IN, searchField, searchValue.toArray(new Object[0]));
	}
	
	public static SearchField all(String searchField, Object... searchValue) {
		return new SearchField(Type.ALL, searchField, searchValue);
	}
	
	public static SearchField all(String searchField, Collection<String> searchValue) {
		return new SearchField(Type.ALL, searchField, searchValue.toArray(new Object[0]));
	}
	
	public static SearchField like(String searchField, Object searchValue) {
		return new SearchField(Type.LIKE, searchField, searchValue);
	}
	
	public static SearchField or(SearchField x, SearchField y) {
		return new SearchField(Type.OR, x, y);
	}
	
	public static SearchField or(SearchField...fields) {
		return new SearchField(Type.OR, fields);
	}
	
	public static SearchField and(SearchField x, SearchField y) {
		return new SearchField(Type.AND, x, y);
	}
	
	public static SearchField and(SearchField...fields) {
		return new SearchField(Type.AND, fields);
	}
	
	public static SearchField gt(String searchField, Object searchValue) {
		return new SearchField(Type.GT, searchField, searchValue);
	}
	
	public static SearchField gte(String searchField, Object searchValue) {
		return new SearchField(Type.GTE, searchField, searchValue);
	}
	
	public static SearchField lt(String searchField, Object searchValue) {
		return new SearchField(Type.LT, searchField, searchValue);
	}
	
	public static SearchField lte(String searchField, Object searchValue) {
		return new SearchField(Type.LTE, searchField, searchValue);
	}

	public static SearchField[] add(SearchField[] fields, SearchField... field) {
		List<SearchField> tmp = new ArrayList<>();
		tmp.addAll(Arrays.asList(fields));
		tmp.addAll(Arrays.asList(field));
		return tmp.toArray(new SearchField[0]);
		
	}

	public void process(SearchTransformer transform) {
		
		if(Objects.nonNull(fields)) {
			for(SearchField field : fields) {
				field.process(transform);
			}
		}
		
		if(Objects.nonNull(searchValue)) {
			for(int i = 0;i<searchValue.length;i++) {
				searchValue[i] = transform.transform(searchField, searchValue[i]);
			}
		}
	}

	public void mark() {
		this.marked = true;
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	private boolean matches(Object bean) {
		switch (type) {
		case EQUALS:
			try {
				if (Objects.equals(searchValue[0], PropertyUtils.getNestedProperty(bean, searchField))) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case NOT:
			try {
				if (!Objects.equals(searchValue[0], PropertyUtils.getNestedProperty(bean, searchField))) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case LT:
			try {
				if (((Number)PropertyUtils.getNestedProperty(bean, searchField)).doubleValue() < ((Number)searchValue[0]).doubleValue()) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case GT:
			try {
				if (((Number)PropertyUtils.getNestedProperty(bean, searchField)).doubleValue() > ((Number)searchValue[0]).doubleValue()) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case LTE:
			try {
				if (((Number)PropertyUtils.getNestedProperty(bean, searchField)).doubleValue() <= ((Number)searchValue[0]).doubleValue()) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case GTE:
			try {
				if (((Number)PropertyUtils.getNestedProperty(bean, searchField)).doubleValue() >= ((Number)searchValue[0]).doubleValue()) {
					return true;
				}
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			}
			break;
		case IN:
			for(var val : searchValue) {
				try {
					if (Objects.equals(val, PropertyUtils.getNestedProperty(bean, searchField))) {
						return true;
					}
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				}
				break;
			}
			break;
		case OR:
			for(var fld : fields) {
				if(fld.matches(bean)) {
					return true;
				}
			}
			break;
		case AND:
			for(var fld : fields) {
				if(!fld.matches(bean)) {
					return false;
				}
			}
			break;
		default:
			throw new UnsupportedOperationException(type + " is not currently supported by non-database filtering.");
		}
		return false;
	}
	
	public static <T> Stream<T> filter(Stream<T> input, SearchField... filter) {
		if(filter.length == 0)
			return input;
		else
			return input.filter(p -> {
				for(var f : filter) {
					if(!f.matches(p)) {
						return false;
					}
				}
				return true;
			});
	}
}
