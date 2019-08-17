package com.jadaptive.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.JadaptiveIgnore;
import com.jadaptive.repository.ReflectionUtils;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.utils.Utils;
import com.mongodb.BasicDBList;

public class DocumentHelper {

	
	public static void convertObjectToDocument(AbstractUUIDEntity obj, Document document) throws RepositoryException, EntityException {

		try {
			
			if(StringUtils.isNotBlank(obj.getUuid())) {
				document.put("_id", obj.getUuid());
			}
			
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
		
		BasicDBList list = new BasicDBList();
		
		for(Object value : values) {
			
			if(String.class.equals(value.getClass())) {
				
			} else if(Integer.class.equals(value.getClass())) {
				
			} else if(Long.class.equals(value.getClass())) {
				
			} else if(Boolean.class.equals(value.getClass())) {
				
			} else if(Float.class.equals(value.getClass())) {
				
			} else if(Double.class.equals(value.getClass())) {
				
			} else if(Date.class.equals(value.getClass())) {
				
			} else if(AbstractUUIDEntity.class.isAssignableFrom(value.getClass())) {

				AbstractUUIDEntity e = (AbstractUUIDEntity) value;
				
				Document embeddedDocument = new Document();
				convertObjectToDocument(e, embeddedDocument);
				list.add(embeddedDocument);

			} else if(value.getClass().isEnum()) {
				
			} 
		}
		
		document.put(name, list);
	}

	public static void buildDocument(String name, AbstractUUIDEntity object, Class<?> type, Document document) throws RepositoryException, ParseException, EntityException {
		
		Document embedded = new Document();
		convertObjectToDocument(object, embedded);
		document.put(name, embedded);
	}

	public static <T extends AbstractUUIDEntity> T convertDocumentToObject(T obj, Document document) throws ParseException {
		
		try {
			for(Method m : ReflectionUtils.getSetters(obj.getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Parameter parameter = m.getParameters()[0];

				if(parameter.getType().equals(String.class)) {
					m.invoke(obj, document.getString(name));
				} else if(parameter.getType().equals(Boolean.class)) {
					m.invoke(obj, Boolean.parseBoolean(document.getString(name)));
				} else if(parameter.getType().equals(Integer.class)) {
					m.invoke(obj, Integer.parseInt(document.getString(name)));
				} else if(parameter.getType().equals(Long.class)) {
					m.invoke(obj, Long.parseLong(document.getString(name)));
				} else if(parameter.getType().equals(Float.class)) {
					m.invoke(obj, Float.parseFloat(document.getString(name)));
				} else if(parameter.getType().equals(Double.class)) {
					m.invoke(obj, Double.parseDouble(document.getString(name)));
				} else if(parameter.getType().equals(Date.class)) {
					m.invoke(obj, Utils.parseDateTime(document.getString(name)));
				} else if(Collection.class.isAssignableFrom(parameter.getType())) { 
					ParameterizedType o = (ParameterizedType) parameter.getParameterizedType();
					Class<?> type = (Class<?>) o.getActualTypeArguments()[0];
					if(type.isEnum()) {
						 
					} else if(AbstractUUIDEntity.class.isAssignableFrom(type)) {
						Set<AbstractUUIDEntity> elements = new HashSet<>();	
						List<?> list = (List<?>) document.get(name);
						if(Objects.isNull(list)) {
							continue;
						}
						for(Object embedded : list) {
							Document embeddedDocument = (Document) embedded;
							elements.add(convertDocumentToObject((AbstractUUIDEntity)type.newInstance(), embeddedDocument));
						}

						m.invoke(obj, elements);
						
					} else if(type.equals(String.class)) {
						
					} else if(type.equals(Boolean.class)) {
						
					} else if(type.equals(Integer.class)) {
						
					} else if(type.equals(Long.class)) {
						
					} else if(type.equals(Float.class)) {
						
					} else if(type.equals(Double.class)) {
						
					} else if(type.equals(Date.class)) {
					
					} else {
						throw new IllegalStateException(
								String.format("Unexpected collection type %s in object setter %s",
								type.getName(),
								name));
					}
					
					

				} else if(AbstractUUIDEntity.class.isAssignableFrom(parameter.getType())) {
					m.invoke(obj, convertDocumentToObject((AbstractUUIDEntity) parameter.getType().newInstance(), (Document) document.get(name)));
				} else if(parameter.getType().isEnum()) { 
					String v = document.getString(name);
					Enum<?>[] enumConstants = (Enum<?>[]) parameter.getType().getEnumConstants();
					if(StringUtils.isBlank(v)) {
						m.invoke(obj, (Object)null);
						break;
					}
					if(NumberUtils.isCreatable(v)) {
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
				} else {
					throw new IllegalStateException(String.format("Unexpected type %s in object setter %s",
							parameter.getType().getName(),
							name));
				}
				
			}
			
			return obj;
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | InstantiationException e) {
			throw new RepositoryException(String.format("Unexpected error loading UUID entity %s", obj.getClass().getName()), e);			
		}
		
	}
}
