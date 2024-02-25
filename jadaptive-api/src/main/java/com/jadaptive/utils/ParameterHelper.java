package com.jadaptive.utils;

import java.util.Map;
import java.util.Objects;

public class ParameterHelper {

	public static void setValue(Map<String,String[]> params, String key, String value) {
		params.put(key,  new String[] { value });
	}
	
	public static String getValue(Map<String,String[]> params ,String key) {
		String[] values = params.get(key);
		if(Objects.nonNull(values)) {
			if(values.length > 0) {
				return values[0];
			}
		}
		return null;
	}
	
	public static String[] getValues(Map<String,String[]> params, String key) {
		return params.get(key);
	}
	
	public static void addValue(Map<String,String[]> params, String key, String value) {
		String[] values = getValues(params, key);
		if(Objects.isNull(values)) {
			setValue(params, key, value);
		} else {
			String[] tmp = new String[values.length+1];
			System.arraycopy(values, 0, tmp, 0, values.length);
			tmp[values.length] = value;
			params.put(key, tmp);
		}
	}
}
