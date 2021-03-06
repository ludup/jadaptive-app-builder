package com.jadaptive.app.db;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.app.ClassLoaderServiceImpl;
import com.jadaptive.app.encrypt.EncryptionServiceImpl;
import com.jadaptive.utils.Utils;

public class DocumentHelper {

	static Logger log = LoggerFactory.getLogger(DocumentHelper.class);
	
	static Set<String> builtInNames = new HashSet<>(Arrays.asList("uuid", "system", "hidden"));
	
	static Map<String,String> classNameChanges = new HashMap<>();
	
	public static String getTemplateResourceKey(Class<?> clz) {
		ObjectDefinition template = (ObjectDefinition) clz.getAnnotation(ObjectDefinition.class);
		if(Objects.nonNull(template)) {
			return template.resourceKey();
		}
		return clz.getSimpleName();
	}
	
	
	public static void convertObjectToDocument(UUIDDocument obj, Document document) throws RepositoryException, ObjectException {

		try {
			
			if(StringUtils.isNotBlank(obj.getUuid())) {
				document.put("_id", obj.getUuid());
			}
			
			
			document.put("_clz", obj.getClass().getName());
			
			Map<String,Field> fields = ReflectionUtils.getFields(obj.getClass());
			
			for(Method m : ReflectionUtils.getGetters(obj.getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Field field = fields.get(name);
				if(Objects.isNull(field)) {
					continue;
				}
				ObjectField columnDefinition = field.getAnnotation(ObjectField.class);
				Object value = m.invoke(obj);
				if(!Objects.isNull(value)) { 
					if(m.getReturnType().equals(Date.class)) {
						document.put(name,  value);
					} else if(isSupportedPrimative(m.getReturnType())) {
						document.put(name,  value);
					} else if(m.getReturnType().isEnum()) {
						document.put(name, ((Enum<?>)value).name());
					} else if(UUIDDocument.class.isAssignableFrom(m.getReturnType())) {
						if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
							buildDocument(name, (UUIDDocument)value, m.getReturnType(), document);
						} else if(Objects.nonNull(value)) {
							document.put(name, ((UUIDDocument)value).getUuid());
						}
					} else if(Collection.class.isAssignableFrom(m.getReturnType())) {
						buildCollectionDocuments(name, columnDefinition, (Collection<?>)value, m.getReturnType(), document);
					} else {
						document.put(name,  checkForAndPerformEncryption(columnDefinition, String.valueOf(value)));
					}
				}
			}
			
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | ParseException e) {
			throw new RepositoryException(String.format("Unexpected error storing UUID entity %s", obj.getClass().getName()), e);		
		}
		
	}

	private static boolean isSupportedPrimative(Class<?> returnType) {
		if(returnType.equals(Integer.class)) {
			return true;
		} else if(returnType.equals(int.class)) {
			return true;
		} else if(returnType.equals(Long.class)) {
			return true;
		} else if(returnType.equals(long.class)) {
			return true;
		} else if(returnType.equals(Float.class)) {
			return true;
		} else if(returnType.equals(float.class)) {
			return true;
		} else if(returnType.equals(Double.class)) {
			return true;
		} else if(returnType.equals(double.class)) {
			return true;
		} else if(returnType.equals(Boolean.class)) {
			return true;
		} else if(returnType.equals(boolean.class)) {
			return true;
		} 
		return false;
	}


	private static String checkForAndPerformEncryption(ObjectField columnDefinition, String value) {
		if(Objects.nonNull(columnDefinition) && (columnDefinition.manualEncryption() || columnDefinition.automaticEncryption())) {
			if(Objects.nonNull(value) && !EncryptionServiceImpl.getInstance().isEncrypted(value)) {
				return EncryptionServiceImpl.getInstance().encrypt(value);
			}
		}
		return value;
	}
	
	private static String checkForAndPerformDecryption(ObjectField columnDefinition, String value) {
		if(Objects.nonNull(columnDefinition) && columnDefinition.automaticEncryption()) {
			if(Objects.nonNull(value) && EncryptionServiceImpl.getInstance().isEncrypted(value)) {
				return EncryptionServiceImpl.getInstance().decrypt(value);
			}
		}
		return value;
	}

	public static void buildCollectionDocuments(String name, ObjectField columnDefinition, Collection<?> values, Class<?> returnType, Map<String,Object> document) throws ParseException, ObjectException {
		
		List<Object> list = new ArrayList<>();

		for(Object value : values) {
			if(Date.class.equals(value.getClass())) {
				list.add(value);
			} else if(isSupportedPrimative(value.getClass())) {
				list.add(value);
			} else if(value.getClass().isEnum()) {
				list.add(((Enum<?>)value).name());
//			} else if(value instanceof ObjectReference2) {
//				list.add(((ObjectReference2)value).toMap());
			} else if(UUIDEntity.class.isAssignableFrom(value.getClass())) {

				AbstractUUIDEntity e = (AbstractUUIDEntity) value;
				if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
					Document embeddedDocument = new Document();
					convertObjectToDocument(e, embeddedDocument);
					list.add(embeddedDocument);
				} else {
					list.add(e.getUuid());
				}
			} else {
				list.add(checkForAndPerformEncryption(columnDefinition,value.toString()));
			} 
		}
		
		document.put(name, list);
	}

	public static void buildDocument(String name, UUIDDocument object, Class<?> type, Document document) throws RepositoryException, ParseException, ObjectException {
		
		Document embedded = new Document();
		convertObjectToDocument(object, embedded);
		document.put(name, embedded);
	}

	public static <T extends UUIDDocument> T convertDocumentToObject(Class<?> baseClass, Document document) {
		return convertDocumentToObject(baseClass, document, baseClass.getClassLoader());
	}

	@SuppressWarnings("unchecked")
	public static <T extends UUIDDocument> T convertDocumentToObject(Class<?> baseClass, Document document, ClassLoader classLoader) {
		
		try {
			
			String clz = (String) document.get("_clz");
			if(Objects.isNull(clz)) {
				clz = (String) document.get("clz"); // Compatibility with older version.
			}
			if(Objects.isNull(clz)) {
				clz = baseClass.getName();
			}
			
			clz = clz.replace("FieldDefinition", "FieldTemplate");
			T obj;
			
			try {
				clz = processClassNameChanges(clz, classLoader);
				obj = (T) classLoader.loadClass(clz).newInstance();
			} catch(ClassNotFoundException e) {
				obj = (T) ClassLoaderServiceImpl.getInstance().findClass(clz).newInstance();
			}

			String uuid = (String) document.get("_id");
			obj.setUuid(uuid);
			
			Map<String,Field> fields = ReflectionUtils.getFields(obj.getClass());
			
			for(Method m : ReflectionUtils.getSetters(obj.getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				if(name.equals("uuid")) {
					continue;
				}
				Field field = fields.get(name);
				if(Objects.isNull(field)) {
					continue;
				}
				ObjectField columnDefinition = field.getAnnotation(ObjectField.class);
//				if(!builtInNames.contains(name) && Objects.isNull(columnDefinition)) {
//					continue;
//				}
				
				Parameter parameter = m.getParameters()[0];
				Object value = document.get(name);
				if(Objects.isNull(value) && Objects.nonNull(columnDefinition)) {
					value = fromString(parameter.getType(), columnDefinition.defaultValue());
				}
				if(Objects.isNull(value) && parameter.getType().isPrimitive()) {
					continue;
				}
				if(parameter.getType().equals(String.class)) {
					m.invoke(obj, checkForAndPerformDecryption(columnDefinition, (String) value));
				} else if(isSupportedPrimative(parameter.getType())) {
					if(!parameter.getType().equals(String.class) && value instanceof String) {
						m.invoke(obj, fromString(parameter.getType(), (String) value));
					} else {
						m.invoke(obj, value);
					}
				} else if(parameter.getType().equals(Date.class)) {
					m.invoke(obj, document.getDate(name));
				} else if(UUIDDocument.class.isAssignableFrom(parameter.getType())) {
					if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
						Object doc = document.get(name);
						if(Objects.isNull(doc)) {
							continue;
						}
						if(!(doc instanceof Document) && doc instanceof Map) {
							doc = new Document((Map<String,Object>)doc);
						} 
						
						m.invoke(obj, convertDocumentToObject(UUIDEntity.class, (Document) doc, classLoader));
					} else {
						String objectUUID =  document.getString(name);
						ObjectServiceBean service = parameter.getType().getAnnotation(ObjectServiceBean.class);
						if(Objects.nonNull(service)) {
							UUIDObjectService<?> bean = (UUIDObjectService<?>) ApplicationServiceImpl.getInstance().getBean(service.bean());
							Object ref = bean.getObjectByUUID(objectUUID);
							m.invoke(obj, ref);
						} else {
							String resourceKey = getTemplateResourceKey(parameter.getType());
							
							if(StringUtils.isNotBlank(objectUUID)) {
								AbstractObject e = (AbstractObject) ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(resourceKey, objectUUID);
								Object ref = convertDocumentToObject(parameter.getType(), new Document(e.getDocument())); 
								m.invoke(obj, ref);
							}
						}
					}
				} else if(parameter.getType().isEnum()) { 
					String v = (String) value;
					Enum<?>[] enumConstants = (Enum<?>[]) parameter.getType().getEnumConstants();
					if(StringUtils.isBlank(v)) {
						m.invoke(obj, (Object)null);
						continue;
					}
					if(NumberUtils.isNumber(v)) {
						Enum<?> enumConstant = enumConstants[Integer.parseInt(v)];
						m.invoke(obj, enumConstant);
						continue;
					} else {//name
						for (Enum<?> enumConstant : enumConstants) {
							if(enumConstant.name().equalsIgnoreCase(v)){
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
					if(UUIDEntity.class.isAssignableFrom(type)) {
						Collection<AbstractUUIDEntity> elements = new ArrayList<>();	
						for(Object embedded : list) {
							if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
								Document embeddedDocument = (Document) embedded;
								elements.add(convertDocumentToObject(UUIDEntity.class, embeddedDocument, classLoader));
							} else {
								String resourceKey = getTemplateResourceKey(parameter.getType());
								String objectUUID =  document.getString(name);
								AbstractObject e = (AbstractObject) ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(resourceKey, objectUUID);
								
								AbstractUUIDEntity ref = convertDocumentToObject(parameter.getType(), new Document(e.getDocument())); 
								elements.add(ref);
							}
						}

						m.invoke(obj, elements);
						
					} else {
						
						if(type.equals(String.class)) {
							m.invoke(obj, buildStringCollection(columnDefinition, list));
						} else if(type.equals(Boolean.class)) {
							m.invoke(obj, buildBooleanCollection(columnDefinition, list));
						} else if(type.equals(Integer.class)) {
							m.invoke(obj, buildIntegerCollection(columnDefinition, list));
						} else if(type.equals(Long.class)) {
							m.invoke(obj, buildLongCollection(columnDefinition, list));
						} else if(type.equals(Float.class)) {
							m.invoke(obj, buildFloatCollection(columnDefinition, list));
						} else if(type.equals(Double.class)) {
							m.invoke(obj, buildDoubleCollection(columnDefinition, list));
						} else if(type.equals(Date.class)) {
							m.invoke(obj, buildDateCollection(columnDefinition, list));
						} else if(type.isEnum()) {  
							m.invoke(obj, buildEnumCollection(list, type));
//						} else if(type.equals(ObjectReference2.class)) {  
//							m.invoke(obj, buildReferenceCollection(list, type));
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
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | InstantiationException | ClassNotFoundException | ParseException e) {
			throw new RepositoryException(String.format("Unexpected error loading UUID entity %s", baseClass.getName()), e);			
		}
		
	}

	public static String processClassNameChanges(String clz, ClassLoader classLoader) {
		
		String change = classNameChanges.get(clz);
		if(Objects.nonNull(change)) {
			return StringUtils.isNotBlank(change) ? change : clz;
		}
		
		String value = "";
		URL url = classLoader.getResource("classpath.properties");
		if(Objects.isNull(url)) {
			url = DocumentHelper.class.getResource("classpath.properties");
		}
		if(Objects.nonNull(url)) {
			Properties declaredChanges = new Properties();
			try(InputStream in = url.openStream()) {
				declaredChanges.load(in);
			} catch(IOException e) { 
				log.warn("Expected to load properties file for classpath.changes", e);
			}
			
			value = declaredChanges.getProperty(clz, "");
		}
		
		classNameChanges.put(clz, value);
		change = classNameChanges.get(clz);
		return StringUtils.isNotBlank(change) ? change : clz;
	}


	public static Object fromString(Class<?> type, String value) {
		if(type.equals(boolean.class)) {
			return Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false"));
		} else if(type.equals(Boolean.class)) {
			return new Boolean(Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false")));
		} else if(type.equals(int.class)) {
			return Integer.parseInt(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Integer.class)) {
			return new Integer(Integer.parseInt(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(long.class)) {
			return Long.parseLong(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Long.class)) {
			return new Long(Long.parseLong(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(float.class)) {
			return Float.parseFloat(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Float.class)) {
			return new Float(Float.parseFloat(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(double.class)) {
			return Double.parseDouble(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Double.class)) {
			return new Double(Double.parseDouble(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(Date.class)) {
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseDate(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			} else {
				return null;
			}
		} else {
			return value;
		}
	}
	
	public static Object fromString(FieldTemplate def, String value) {
		switch(def.getFieldType()) {
		case BOOL:
			if(StringUtils.isNotBlank(value)) {
				return Boolean.parseBoolean(value);
			} else {
				return false;
			}
		case DECIMAL:
			if(StringUtils.isNotBlank(value)) {
				return Double.parseDouble(value);
			} else {
				return null;
			}
		case INTEGER:
			if(StringUtils.isNotBlank(value)) {
				return Integer.parseInt(value);
			} else {
				return null;
			}
		case LONG:
			if(StringUtils.isNotBlank(value)) {
				return Long.parseLong(value);
			} else {
				return null;
			}
		case PASSWORD:
		case TEXT:
		case TEXT_AREA:
		case ENUM:
			return value;
		case TIMESTAMP:
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseDate(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			} else {
				return null;
			}
		default:
			throw new IllegalStateException("Unhandled field type " + def.getFieldType() + " in DocumentHelper.fromString");
		}
	}

	private static Object buildStringCollection(ObjectField columnDefinition, List<?> list) {
		Collection<String> v = new HashSet<>();
		for(Object item : list) {
			v.add(checkForAndPerformDecryption(columnDefinition, item.toString()));
		}
		return v;
	}


//	private static Object buildReferenceCollection(List<?> list, Class<?> type) {
//		Collection<ObjectReference2> v = new HashSet<>();
//		for(Object item : list) {
//			v.add(new ObjectReference2((Map<String,String>) item));
//		}
//		return v;
//	}
	
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

	private static Collection<Date> buildDateCollection(ObjectField columnDefinition, List<?> items) throws ParseException {
		Collection<Date> v = new HashSet<>();
		for(Object item : items) {
			v.add(Utils.parseDateTime(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Double> buildDoubleCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Double> v = new HashSet<>();
		for(Object item : items) {
			v.add(Double.parseDouble(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Float> buildFloatCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Float> v = new HashSet<>();
		for(Object item : items) {
			v.add(Float.parseFloat(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Long> buildLongCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Long> v = new HashSet<>();
		for(Object item : items) {
			v.add(Long.parseLong(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Integer> buildIntegerCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Integer> v = new HashSet<>();
		for(Object item : items) {
			v.add(Integer.parseInt(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Boolean> buildBooleanCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Boolean> v = new HashSet<>();
		for(Object item : items) {
			v.add(Boolean.parseBoolean(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

}
