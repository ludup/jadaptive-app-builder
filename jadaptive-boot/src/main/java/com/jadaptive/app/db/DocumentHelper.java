package com.jadaptive.app.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bson.Document;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.JadaptiveIgnore;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.utils.Utils;

public class DocumentHelper {

	
	public static void convertObjectToDocument(AbstractUUIDEntity obj, Document document) throws RepositoryException, EntityException {

		try {
			
			if(StringUtils.isBlank(obj.getUuid())) {
				obj.setUuid(UUID.randomUUID().toString());
			}
			
			document.put("_id", obj.getUuid());
			document.put("_clz", obj.getClass().getName());
			
			for(Method m : ReflectionUtils.getGetters(obj.getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				if(m.isAnnotationPresent(JadaptiveIgnore.class)) {
					continue;
				}
				Object value = m.invoke(obj);
				if(!Objects.isNull(value)) { 
					if(m.getReturnType().equals(Date.class)) {
						value = Utils.formatDateTime((Date)value);
						document.put(name,  String.valueOf(value));
					} else if(AbstractUUIDEntity.class.isAssignableFrom(m.getReturnType())) {
						buildDocument(name, (AbstractUUIDEntity)value, m.getReturnType(), document);
					} else if(Collection.class.isAssignableFrom(m.getReturnType())) {
						buildCollectionDocuments(name, (Collection<?>)value, m.getReturnType(), document);
					} else {
						document.put(name,  String.valueOf(value));
					}
				}
			}
			
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | ParseException e) {
			throw new RepositoryException(String.format("Unexpected error storing UUID entity %s", obj.getClass().getName()), e);		
		}
		
	}

	public static void buildCollectionDocuments(String name, Collection<?> values, Class<?> returnType, Document document) throws ParseException, EntityException {
		
		List<Object> list = new ArrayList<>();

		for(Object value : values) {
			if(Date.class.equals(value.getClass())) {
				list.add(Utils.formatDateTime((Date)value));
			} else if(AbstractUUIDEntity.class.isAssignableFrom(value.getClass())) {

				AbstractUUIDEntity e = (AbstractUUIDEntity) value;
				
				Document embeddedDocument = new Document();
				convertObjectToDocument(e, embeddedDocument);
				list.add(embeddedDocument);

			} else {
				list.add(value.toString());
			} 
		}
		
		document.put(name, list);
	}

	public static void buildDocument(String name, AbstractUUIDEntity object, Class<?> type, Document document) throws RepositoryException, ParseException, EntityException {
		
		Document embedded = new Document();
		convertObjectToDocument(object, embedded);
		document.put(name, embedded);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractUUIDEntity> T convertDocumentToObject(Class<T> baseClass, Document document) throws ParseException {
		
		try {
			
			String clz = document.getString("_clz");
			if(Objects.isNull(clz)) {
				clz = document.getString("clz"); // Compaitilbity with older version.
			}
			if(Objects.isNull(clz)) {
				throw new ParseException("Missing clz parameter from document", 0);
			}
			
			T obj = (T) baseClass.getClassLoader().loadClass(clz).newInstance();
			
			for(Method m : ReflectionUtils.getSetters(obj.getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Parameter parameter = m.getParameters()[0];

				if(parameter.getType().equals(String.class)) {
					m.invoke(obj, document.getString(name));
				} else if(parameter.getType().equals(Boolean.class) || parameter.getType().equals(boolean.class)) {
					m.invoke(obj, Boolean.parseBoolean(document.getString(name)));
				} else if(parameter.getType().equals(Integer.class) || parameter.getType().equals(int.class)) {
					m.invoke(obj, Integer.parseInt(document.getString(name)));
				} else if(parameter.getType().equals(Long.class) || parameter.getType().equals(long.class)) {
					m.invoke(obj, Long.parseLong(document.getString(name)));
				} else if(parameter.getType().equals(Float.class)  || parameter.getType().equals(float.class)) {
					m.invoke(obj, Float.parseFloat(document.getString(name)));
				} else if(parameter.getType().equals(Double.class) || parameter.getType().equals(double.class)) {
					m.invoke(obj, Double.parseDouble(document.getString(name)));
				} else if(parameter.getType().equals(Date.class)) {
					m.invoke(obj, Utils.parseDateTime(document.getString(name)));
				} else if(AbstractUUIDEntity.class.isAssignableFrom(parameter.getType())) {
					m.invoke(obj, convertDocumentToObject(AbstractUUIDEntity.class, (Document) document.get(name)));
				} else if(parameter.getType().isEnum()) { 
					String v = document.getString(name);
					Enum<?>[] enumConstants = (Enum<?>[]) parameter.getType().getEnumConstants();
					if(StringUtils.isBlank(v)) {
						m.invoke(obj, (Object)null);
						break;
					}
					if(NumberUtils.isNumber(v)) {
						Enum<?> enumConstant = enumConstants[Integer.parseInt(v)];
						m.invoke(obj, enumConstant);
						break;
					} else {//name
						for (Enum<?> enumConstant : enumConstants) {
							if(enumConstant.name().equals(v)){
								m.invoke(obj, enumConstant);
								break;
							}
						}
					}
				} else if(Collection.class.isAssignableFrom(parameter.getType())) { 
					ParameterizedType o = (ParameterizedType) parameter.getParameterizedType();
					Class<?> type = (Class<?>) o.getActualTypeArguments()[0];
					List<?> list = (List<?>) document.get(name);
					if(Objects.isNull(list)) {
						continue;
					}
					if(AbstractUUIDEntity.class.isAssignableFrom(type)) {
						Collection<AbstractUUIDEntity> elements = new ArrayList<>();	
						for(Object embedded : list) {
							Document embeddedDocument = (Document) embedded;
							elements.add(convertDocumentToObject(AbstractUUIDEntity.class, embeddedDocument));
						}

						m.invoke(obj, elements);
						
					} else {
						
						if(type.equals(String.class)) {
							m.invoke(obj, new HashSet<String>((Collection<? extends String>) list));
						} else if(type.equals(Boolean.class)) {
							m.invoke(obj, buildBooleanCollection(list));
						} else if(type.equals(Integer.class)) {
							m.invoke(obj, buildIntegerCollection(list));
						} else if(type.equals(Long.class)) {
							m.invoke(obj, buildLongCollection(list));
						} else if(type.equals(Float.class)) {
							m.invoke(obj, buildFloatCollection(list));
						} else if(type.equals(Double.class)) {
							m.invoke(obj, buildDoubleCollection(list));
						} else if(type.equals(Date.class)) {
							m.invoke(obj, buildDateCollection(list));
						} else if(type.isEnum()) {  
							m.invoke(obj, buildEnumCollection(list, type));
						} else {
							throw new IllegalStateException(
									String.format("Unexpected collection type %s in object setter %s",
									type.getName(),
									name));
						}
					}  
				} else {
					throw new IllegalStateException(String.format("Unexpected type %s in object setter %s",
							parameter.getType().getName(),
							name));
				}
				
			}
			
			return obj;
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | InstantiationException | ClassNotFoundException e) {
			throw new RepositoryException(String.format("Unexpected error loading UUID entity %s", baseClass.getName()), e);			
		}
		
	}

	private static Object buildEnumCollection(List<?> items, Class<?> type) {

		Collection<Enum<?>> v = new HashSet<>();
		for(Object item : items) {
			Enum<?>[] enumConstants = (Enum<?>[]) type.getEnumConstants();
			if(NumberUtils.isNumber(item.toString())) {
				Enum<?> enumConstant = enumConstants[Integer.parseInt(item.toString())];
				v.add(enumConstant);
				break;
			} else {
				for (Enum<?> enumConstant : enumConstants) {
					if(enumConstant.name().equals(item)){
						v.add(enumConstant);
						break;
					}
				}
			}			
		}
		return v;		
	}

	private static Collection<Date> buildDateCollection(List<?> items) throws ParseException {
		Collection<Date> v = new HashSet<>();
		for(Object item : items) {
			v.add(Utils.parseDateTime(item.toString()));
		}
		return v;
	}

	private static Collection<Double> buildDoubleCollection(List<?> items) {
		Collection<Double> v = new HashSet<>();
		for(Object item : items) {
			v.add(Double.parseDouble(item.toString()));
		}
		return v;
	}

	private static Collection<Float> buildFloatCollection(List<?> items) {
		Collection<Float> v = new HashSet<>();
		for(Object item : items) {
			v.add(Float.parseFloat(item.toString()));
		}
		return v;
	}

	private static Collection<Long> buildLongCollection(List<?> items) {
		Collection<Long> v = new HashSet<>();
		for(Object item : items) {
			v.add(Long.parseLong(item.toString()));
		}
		return v;
	}

	private static Collection<Integer> buildIntegerCollection(List<?> items) {
		Collection<Integer> v = new HashSet<>();
		for(Object item : items) {
			v.add(Integer.parseInt(item.toString()));
		}
		return v;
	}

	private static Collection<Boolean> buildBooleanCollection(List<?> items) {
		Collection<Boolean> v = new HashSet<>();
		for(Object item : items) {
			v.add(Boolean.parseBoolean(item.toString()));
		}
		return v;
	}

}
