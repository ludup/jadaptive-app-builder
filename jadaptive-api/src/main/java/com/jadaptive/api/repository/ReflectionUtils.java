package com.jadaptive.api.repository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.WordUtils;



public class ReflectionUtils {


	static Map<Class<?>, Set<Method>> setterCache = new HashMap<>();
	static Map<Class<?>, Map<String,Method>> getterCache = new HashMap<>();
	static Map<Class<?>, Map<String,Field>> fieldCache = new HashMap<>();
	
	
	public static <T extends Annotation> Set<T> getFieldAnnotations(Class<?> clz, Class<T> annotation) {
		
		Set<T> annotations = new HashSet<>();
		iterateAnnotatedFields(annotations, clz, annotation);
		return annotations;
	}
	
	
	private static <T extends Annotation> void iterateAnnotatedFields(Set<T> results, Class<?> clz, Class<T> annotation) {
		
		for(Field field : clz.getDeclaredFields()) {
			T column = field.getAnnotation(annotation);
			if(Objects.nonNull(column)) {
				results.add(column);
			}
		}
		
		Class<?> superClass = clz.getSuperclass();
		if(Objects.nonNull(superClass)) {
			iterateAnnotatedFields(results, superClass, annotation);
		}
	}

	public static Map<String, Field> getFields(Class<?> clz) {
		
		Map<String, Field> results = fieldCache.get(clz);
		if(Objects.isNull(results)) {
			results = new HashMap<>();
			iterateFields(clz, results);
			fieldCache.put(clz, results);
		}
		
		return results;
	}
	
	public static Class<?> getObjectType(Field field) {
		Class<?> clz = field.getType();
		if(Collection.class.isAssignableFrom(clz)) {
			clz = (Class<?>)((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
		}
		return clz;
	}
	
	private static void iterateFields(Class<?> clz, Map<String, Field> results) {
		
		for(Field field : clz.getDeclaredFields()) {
			results.put(field.getName(), field);
		}
		
		Class<?> superClass = clz.getSuperclass();
		if(Objects.nonNull(superClass)) {
			iterateFields(superClass, results);
		}
		
	}

	public static Method getMethod(Class<?> clz, String name, Class<?>... args) throws NoSuchMethodException, SecurityException {
		
		try {
			return clz.getMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
			clz = clz.getSuperclass();
			if(Objects.nonNull(clz)) {
				return getMethod(clz, name, args);
			}
			throw e;
		}
	}

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
	
	public static Method getGetter(Class<?> clz, Field field) {
		
		String name = "get" + WordUtils.capitalize(field.getName());
		Map<String,Method> results = getGettersMap(clz);
		return results.get(name);
	}
	
	public static Map<String,Method> getGettersMap(Class<?> clz) {
		
		Map<String,Method> results = getterCache.get(clz);
		if(Objects.isNull(results)) {
			results = new HashMap<>();
			for(Method m : clz.getMethods()) {
				if(m.getName().equals("getClass")) {
					continue;
				}
				
				if(m.getAnnotation(JadaptiveIgnore.class) != null) {
					continue;
				}
				if(clz.isRecord()) {
//					if(m.isSynthetic()) 
						results.put(m.getName(), m);
				}
				if(m.getName().startsWith("get") && m.getName().length() > 3 && m.getParameterCount()==0) {
					results.put(m.getName(), m);
				}
				if(m.getName().startsWith("is") && m.getName().length() > 3 && m.getParameterCount()==0
						&& (m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))) {
					results.put(m.getName(), m);
				}
			}
		
			getterCache.put(clz, results);
		}
		
		return results;
		
	}
	
	
	public static Set<Method> getGetters(Class<?> clz) {
		return new HashSet<>(getGettersMap(clz).values());
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


	public static Field getField(Class<?> clz, String resourceKey) throws NoSuchFieldException {
		do {
			try {
				return clz.getDeclaredField(resourceKey);
			} catch(NoSuchFieldException e) {
				clz = clz.getSuperclass();
			}
		} while(!clz.equals(Object.class));
		
		throw new NoSuchFieldException(resourceKey);
	}


	public static boolean hasAnnotation(Class<?> templateClazz, Class<? extends Annotation> annotationClazz) {
		return templateClazz.getAnnotation(annotationClazz)!=null;
	}
	
	public static boolean hasAnnotationRecursive(Class<?> templateClazz, Class<? extends Annotation> annotationClazz) {
		return getAnnotation(templateClazz, annotationClazz)!=null;
	}
	
	public static <T extends Annotation> T getAnnotation(Class<?> clz, Class<T> annotation) {
		
		do {
			T res = clz.getAnnotation(annotation);
			if(Objects.nonNull(res)) {
				return res;
			}
			clz = clz.getSuperclass();
		} while(!clz.equals(Object.class));
		
		return null;
	}


	public static void executeAnnotatedMethods(Object obj, Class<PostConstruct> class1) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Class<?> parent = obj.getClass();
		do {
			for(Method m : obj.getClass().getDeclaredMethods()) {
				if(m.isAnnotationPresent(PostConstruct.class)) {
					m.setAccessible(true);
					m.invoke(obj);
				}
			}
			parent = parent.getSuperclass();
		} while(parent!=null);
		
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAnnotatedFieldValue(Object obj,
			Class<T> fieldType,
			Class<? extends Annotation> annotationClz) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Field field = getFieldWithAnnotation(obj.getClass(), fieldType, annotationClz);
		if(Objects.nonNull(field)) {
			Method get = ReflectionUtils.getGetter(obj.getClass(), field);
			if(Objects.nonNull(get)) {
				return (T) get.invoke(obj);
			}
		}
		return null;
	}
	
	public static Field getFieldWithAnnotation(Class<?> clz, 
			Class<?> fieldType,
			Class<? extends Annotation> annotationClz) {
		
		Map<String,Field> fields = getFields(clz);
		for(Field field : fields.values()) {
			if(field.getType().isAssignableFrom(fieldType)
					&& field.isAnnotationPresent(annotationClz)) {
				return field;
			}
		}
		return null;
	}
}
