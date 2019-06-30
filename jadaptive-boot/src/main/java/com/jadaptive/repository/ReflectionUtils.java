package com.jadaptive.repository;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReflectionUtils {


	static Map<Class<?>, Set<Method>> setterCache = new HashMap<>();
	static Map<Class<?>, Set<Method>> getterCache = new HashMap<>();
	
	
	public static Set<Method> getSetters(Class<?> clz) {
		
		Set<Method> results = setterCache.get(clz);
		if(Objects.isNull(results)) {
			results = new HashSet<>();
			for(Method m : clz.getMethods()) {
				if(m.getAnnotation(JadaptiveIgnore.class) != null) {
					continue;
				}
				if(m.getName().startsWith("set") && m.getName().length() > 3 && m.getParameterCount()==1) {
					results.add(m);
				}
			}
		
			setterCache.put(clz, results);
		}
		
		return results;
	}
	
	public static Set<Method> getGetters(Class<?> clz) {
		
		Set<Method> results = getterCache.get(clz);
		if(Objects.isNull(results)) {
			results = new HashSet<>();
			for(Method m : clz.getMethods()) {
				if(m.getName().equals("getClass")) {
					continue;
				}
				
				if(m.getAnnotation(JadaptiveIgnore.class) != null) {
					continue;
				}
				if(m.getName().startsWith("get") && m.getName().length() > 3 && m.getParameterCount()==0) {
					results.add(m);
				}
				if(m.getName().startsWith("is") && m.getName().length() > 3 && m.getParameterCount()==0
						&& (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))) {
					results.add(m);
				}
			}
		
			getterCache.put(clz, results);
		}
		
		return results;
	}
	
	public static String calculateFieldName(Method m) {
		int i=0;
		do {
			if(Character.isUpperCase(m.getName().charAt(i))) {
				return m.getName().substring(i, i+1).toLowerCase() + m.getName().substring(i+1);
			}
		} while(++i < m.getName().length());
		
		throw new IllegalStateException(String.format("Cannot determine field name from method name %s", m.getName()));
	}
	
}
