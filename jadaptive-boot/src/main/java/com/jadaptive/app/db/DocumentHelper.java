package com.jadaptive.app.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
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
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.app.ApplicationServiceImpl;
import com.jadaptive.app.ClassLoaderServiceImpl;
import com.jadaptive.app.encrypt.EncryptionServiceImpl;
import com.jadaptive.app.entity.MongoEntity;
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
			log.error("Error converting document", e);
			throw new RepositoryException(String.format("Unexpected error storing U%s", obj.getClass().getName()), e);		
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

	public static AbstractObject buildObject(HttpServletRequest request, String fieldName, ObjectTemplate template) {

		if(log.isDebugEnabled()) {
			log.debug("Building object {} using template {}", fieldName, template.getResourceKey());
		}
		
		MongoEntity obj = new MongoEntity(fieldName);
		String uuid = request.getParameter("uuid");
		if(StringUtils.isNotBlank(uuid)) {
			obj.setUuid(uuid);
		}
		String system = request.getParameter("system");
		if(Objects.nonNull(system)) {
			obj.setSystem(Boolean.valueOf(system));
		}
		String hidden = request.getParameter("hidden");
		if(Objects.nonNull(hidden)) {
			obj.setHidden(Boolean.valueOf(hidden));
		}

		if(StringUtils.isNotBlank(template.getTemplateClass())) {
			obj.setValue("_clz", template.getTemplateClass());
		}
		for(FieldTemplate field : template.getFields()) {
			
			if(log.isDebugEnabled()) {
				log.debug("Processing field {} using form variable {}", field.getResourceKey(), field.getFormVariable());
			}
			
			if(field.getCollection()) {
				obj.setValue(field, convertValues(field, request));
			} else {
				obj.setValue(field, convertValue(field, request));
			}

		}
		
		return obj;
	}
	
	private static Object convertValue(FieldTemplate field, HttpServletRequest request) {
		
		String fieldName = field.getFormVariable();
		
		String value = request.getParameter(fieldName);

		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
			ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(TemplateService.class)
					.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			return buildObject(request, 
					template.getResourceKey(),
					template).getDocument();
		case OBJECT_REFERENCE:
			if(log.isDebugEnabled()) {
				log.debug("Returning {} as reference {}", field.getResourceKey(), value);
			}
			return value;
		default:
			if(Objects.isNull(value)) {
				
				if(log.isDebugEnabled()) {
					log.debug("Returning {} value NULL", field.getResourceKey());
				}
				return null;
			}
			if(log.isDebugEnabled()) {
				log.debug("Returning {} value {}", field.getResourceKey(), value);
			}
			return fromString(field, value);
		}
	}
	
	private static List<Object> convertValues(FieldTemplate field, HttpServletRequest request) {
		
		String fieldName = field.getFormVariable();
		
		List<Object> result = new ArrayList<>();
		
		String[] values = request.getParameterValues(fieldName);
		if(Objects.isNull(values) || values.length == 0) {
			return result;
		}
		
		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
//			TODO build a collection of objects
//			result.add(buildObject(request, 
//				ApplicationServiceImpl.getInstance().getBean(TemplateService.class)
//					.get(field.getValidationValue(ValidationType.RESOURCE_KEY))));
		case OBJECT_REFERENCE:
			for(String value : values) {
				result.add(value);
			}
			break;
		default:
			for(String value : values) {
				result.add(fromString(field, value));
			}
			break;
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Extracted {} values", field.getResourceKey(), Utils.csv(values));
		}
		
		return result;
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
				obj = (T) classLoader.loadClass(clz).getConstructor().newInstance();
			} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
				obj = (T) ClassLoaderServiceImpl.getInstance().findClass(clz).getConstructor().newInstance();
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
					value = fromString(parameter.getType(), columnDefinition.type(), columnDefinition.defaultValue());
				}
				if(Objects.isNull(value) && parameter.getType().isPrimitive()) {
					continue;
				}
				if(parameter.getType().equals(String.class)) {
					m.invoke(obj, checkForAndPerformDecryption(columnDefinition, (String) value));
				} else if(isSupportedPrimative(parameter.getType())) {
					if(!parameter.getType().equals(String.class) && value instanceof String) {
						m.invoke(obj, fromString(parameter.getType(), columnDefinition.type(), (String) value));
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
								AbstractObject e = (AbstractObject) ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(columnDefinition.references(), (String)embedded);
								
								AbstractUUIDEntity ref = convertDocumentToObject(null, new Document(e.getDocument()), classLoader); 
								elements.add(ref);
							}
						}

						m.invoke(obj, elements);
						
					} else {
						
						if(list.isEmpty()) {
							m.invoke(obj, new HashSet<>());
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
					}  
				} else {
					throw new IllegalStateException(String.format("Unexpected type %s in object setter %s",
							parameter.getType().getName(),
							name));
				}
				
			}
			
			return obj;
		} catch (SecurityException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | RepositoryException | InstantiationException | ClassNotFoundException | ParseException e) {
			log.error("Error converting document", e);
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


	public static Object fromString(Class<?> type, FieldType t, String value) {
		if(type.equals(boolean.class)) {
			assertType(t, FieldType.BOOL, FieldType.HIDDEN);
			return Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false"));
		} else if(type.equals(Boolean.class)) {
			assertType(t, FieldType.BOOL, FieldType.HIDDEN);
			return Boolean.valueOf(Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false")));
		} else if(type.equals(int.class)) {
			assertType(t, FieldType.INTEGER, FieldType.HIDDEN);
			return Integer.parseInt(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Integer.class)) {
			assertType(t, FieldType.INTEGER, FieldType.HIDDEN);
			return Integer.valueOf(Integer.parseInt(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(long.class)) {
			assertType(t, FieldType.LONG, FieldType.HIDDEN);
			return Long.parseLong(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Long.class)) {
			assertType(t, FieldType.LONG, FieldType.HIDDEN);
			return Long.valueOf(Long.parseLong(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(float.class)) {
			assertType(t, FieldType.DECIMAL, FieldType.HIDDEN);
			return Float.parseFloat(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Float.class)) {
			assertType(t, FieldType.DECIMAL, FieldType.HIDDEN);
			return Float.valueOf(Float.parseFloat(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(double.class)) {
			assertType(t, FieldType.DECIMAL, FieldType.HIDDEN);
			return Double.parseDouble(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Double.class)) {
			assertType(t, FieldType.DECIMAL, FieldType.HIDDEN);
			return Double.valueOf(Double.parseDouble(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(Date.class)) {
			if(t==FieldType.TIMESTAMP) {
				if(StringUtils.isNotBlank(value)) {
					return Utils.parseDate(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				} else {
					return null;
				}
			} else {
				if(StringUtils.isNotBlank(value)) {
					return Utils.parseDate(value, "yyyy-MM-dd");
				} else {
					return null;
				}
			}
		} else {
			return value;
		}
	}
	
	private static void assertType(FieldType type, FieldType... types) {
		for(FieldType t : types) {
			if(type == t) {
				return;
			}
		}
		throw new IllegalStateException(String.format("Invalid object for field %s", type));
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
		case PERMISSION:
		case HIDDEN:
		case ENUM:
			return value;
		case TIMESTAMP:
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseDate(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			} else {
				return null;
			}
		case DATE:
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseDate(value, "yyyy-MM-dd");
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
	
	public static String generateContentHash(Document entity) {
		
		try {
			SHA256Digest sha2 = new SHA256Digest();
			generateObjectHash(entity, sha2);
			byte[] tmp = new byte[16];
			new Random().nextBytes(tmp);
			sha2.update(tmp, 0, tmp.length);
			sha2.finish();
			return new String(Hex.encode(sha2.getEncodedState()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static void generateObjectHash(Document entity, SHA256Digest sha2) throws UnsupportedEncodingException {

//		for(Map.Entry<String, Object> entry : entity.entrySet()) {
//			
//		}
	}

}
