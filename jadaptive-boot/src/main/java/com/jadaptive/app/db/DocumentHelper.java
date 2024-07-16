package com.jadaptive.app.db;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
import java.util.Base64;
import java.util.Collection;
//import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.repository.UUIDReference;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.template.ValidationType;
//import com.jadaptive.app.ClassLoaderServiceImpl;
//import com.jadaptive.app.encrypt.EncryptionServiceImpl;
//import com.jadaptive.app.entity.MongoEntity;
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
			document.put("resourceKey", obj.getResourceKey());
			 
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
							buildDocument(name, (UUIDDocument)value, document);
						} else if(Objects.nonNull(value)) {
							if(value instanceof UUIDReference) {
								buildDocument(name, (UUIDReference) value, document);
							} else {
								buildDocument(name, buildReference((UUIDEntity)value), document);
							}
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

	private static UUIDReference buildReference(UUIDEntity e) {
		
		if(e instanceof NamedDocument) {
			return generateReference(e.getUuid(), ((NamedDocument)e).getName());
		} else {
			return generateReference(e.getUuid(), e.getUuid());
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
			if(Objects.nonNull(value) && !ApplicationServiceImpl.getInstance().getBean(EncryptionService.class).isEncrypted(value)) {
				return ApplicationServiceImpl.getInstance().getBean(EncryptionService.class).encrypt(value);
			}
		}
		return value;
	}
	
	private static String checkForAndPerformDecryption(ObjectField columnDefinition, String value) {
		if(Objects.nonNull(columnDefinition) && columnDefinition.automaticEncryption()) {
			if(Objects.nonNull(value) && ApplicationServiceImpl.getInstance().getBean(EncryptionService.class).isEncrypted(value)) {
				return ApplicationServiceImpl.getInstance().getBean(EncryptionService.class).decrypt(value);
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
			} else if(UUIDEntity.class.isAssignableFrom(value.getClass())) {

				UUIDEntity e = (UUIDEntity) value;
				if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
					Document embeddedDocument = new Document();
					convertObjectToDocument(e, embeddedDocument);
					list.add(embeddedDocument);
				} else if(e instanceof NamedDocument) {
					Document embeddedDocument = new Document();
					convertObjectToDocument(generateReference(e.getUuid(), ((NamedDocument)e).getName()), embeddedDocument);
					list.add(embeddedDocument);
				} else {
					throw new IllegalStateException(
							String.format("Referenced UUIDEntity %s MUST implement NamedDocument or extend NamedUUIDEntity!",
									e.getClass().getSimpleName()));
				}
			} else {
				list.add(checkForAndPerformEncryption(columnDefinition,value.toString()));
			} 
		}
		
		document.put(name, list);
	}

	public static void buildDocument(String name, UUIDDocument object, Document document) throws RepositoryException, ParseException, ObjectException {
		
		Document embedded = new Document();
		convertObjectToDocument(object, embedded);
		document.put(name, embedded);
	}

	public static <T extends UUIDDocument> T convertDocumentToObject(Class<?> baseClass, Document document) throws ObjectException, ValidationException {
		return convertDocumentToObject(baseClass, document, baseClass.getClassLoader());
	}
	
	private static String getParameter(Map<String,String[]> parameters, FieldTemplate field, String formVariablePrefix) {
		return getParameter(parameters, formVariablePrefix + field.getFormVariable());
	}
	
	private static String getTextParameter(Map<String,String[]> parameters, FieldTemplate field, String formVariablePrefix) {
		return getParameter(parameters, String.format("%sText", formVariablePrefix + field.getFormVariable()));
	}
	
	private static String getParameter(Map<String,String[]> parameters, String formVariable) {
		String[] val = parameters.get(formVariable);
		if(Objects.isNull(val) || val.length == 0) {
			return null;
		}
		return val[0];
	}

	public static AbstractObject buildRootObject(HttpServletRequest request, String resourceKey, ObjectTemplate template) throws IOException, ValidationException {
		return buildObject(request, resourceKey, "", template);
	}
	
	public static AbstractObject buildRootObject(Map<String, String[]> parameters, String resourceKey, ObjectTemplate template) throws IOException, ValidationException {
		return buildObject(parameters, resourceKey, "", template);
	}

	public static AbstractObject buildObject(HttpServletRequest request, String resourceKey, String formVariablePrefix, ObjectTemplate template) throws IOException, ValidationException {
		return buildObject(request.getParameterMap(), resourceKey, formVariablePrefix, template);
	}
	
	public static AbstractObject buildObject(Map<String, String[]> parameters, String resourceKey, String formVariablePrefix, ObjectTemplate template) throws IOException, ValidationException {

		if(log.isDebugEnabled()) {
			log.debug("Building object {} using template {}", resourceKey, template.getResourceKey());
		}
		
		AbstractObject obj = ApplicationServiceImpl.getInstance().getBean(ObjectService.class).createNew(resourceKey);
		String uuid = getParameter(parameters, formVariablePrefix + "uuid");
		if(StringUtils.isNotBlank(uuid)) {
			obj.setUuid(uuid);
		}
		String system = getParameter(parameters, formVariablePrefix + "system");
		if(Objects.nonNull(system)) {
			obj.setSystem(Boolean.valueOf(system));
		}
//		String hidden = getParameter(parameters, formVariablePrefix + "hidden");
//		if(Objects.nonNull(hidden)) {
//			obj.setHidden(Boolean.valueOf(hidden));
//		}

		if(StringUtils.isNotBlank(template.getTemplateClass())) {
			obj.setValue("_clz", template.getTemplateClass());
		}
		for(FieldTemplate field : template.getFields()) {
			
			if(log.isDebugEnabled()) {
				log.debug("Processing field {} using form variable {}", field.getResourceKey());
			}
			
			if(field.getCollection()) {
				obj.setValue(field, convertValues(field, parameters));
			} else {
				obj.setValue(field, convertValue(field, parameters, formVariablePrefix));
			}

		}
		
		return obj;
	}
	
	private static Object convertValue(FieldTemplate field, Map<String,String[]> parameters, String formVariablePrefix) throws IOException, ValidationException {
		
		String value = getParameter(parameters, field, formVariablePrefix);

		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
		{
			ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(TemplateService.class)
					.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			
			StringBuffer tmp = new StringBuffer();
			tmp.append(formVariablePrefix);
			tmp.append(field.getFormVariable());
			tmp.append(".");

			return buildObject(parameters,
					template.getResourceKey(),
					tmp.toString(),
					template).getDocument();
		}
		case OBJECT_REFERENCE:
		{	
			if(log.isDebugEnabled()) {
				log.debug("Returning {} as reference {}", field.getResourceKey(), value);
			}

			String name = getTextParameter(parameters, field, formVariablePrefix);
			
			Document doc = new Document();
			convertObjectToDocument(generateReference(getParameter(parameters, field, formVariablePrefix), name), doc);
			return doc;
		}
		case BOOL:
			if(Objects.isNull(value)) {
				return false;
			} else {
				return Boolean.valueOf(value);
			}
		case IMAGE:
			try {
				if(!Request.get().getParts().isEmpty()) {
					Part part = Request.get().getPart(field.getFormVariable());
					if(Objects.isNull(part)) {
						return null;
					}
					
					if(part.getSize() == 0) {
						return getParameter(parameters, formVariablePrefix + field.getFormVariable() + "_previous");
					}
					
					byte[] data = part.getInputStream().readAllBytes();
					try(ByteArrayInputStream in = new ByteArrayInputStream(data)) {
						BufferedImage bimg = ImageIO.read(in);
						if(Objects.isNull(bimg)) {
							throw new ValidationException(String.format("The file %s does not appear to contain an image!", part.getSubmittedFileName()));
						}
						
						int width          = bimg.getWidth();
						int height         = bimg.getHeight();
						
						int maxHeight = field.getValidationValueInt(ValidationType.IMAGE_HEIGHT, -1);
						int maxWidth = field.getValidationValueInt(ValidationType.IMAGE_WIDTH, -1);
						
						if(maxWidth > -1 && maxWidth < width) {
							throw new ValidationException(String.format("Image dimensions are %dx%d but must not exceed %dx%d", width, height, maxWidth, maxHeight));
						}
						
						if(maxHeight > -1 && maxHeight < height) {
							throw new ValidationException(String.format("Image dimensions are %dx%d but must not exceed %dx%d", width, height, maxWidth, maxHeight));
						}
						
						if(StringUtils.isNotBlank(part.getSubmittedFileName()) && part.getSize() > 0) {
							return String.format("data:%s;base64, %s", part.getContentType(), Base64.getEncoder().encodeToString(data));
						}
					}
					
				}
			} catch (IOException | ServletException e) {
				log.error("Failed to parse IMAGE part for {}", field.getResourceKey());
			}
			return getParameter(parameters, formVariablePrefix + field.getFormVariable() + "_previous");
			
		case FILE:
			try {
				if(!Request.get().getParts().isEmpty()) {
					Part part = Request.get().getPart(field.getFormVariable());
					if(Objects.isNull(part)) {
						return null;
					}
					if(part.getSize() == 0) {
						return null;
					}
					if(StringUtils.isNotBlank(part.getSubmittedFileName())) {
						return String.format("%s;%s;%s", part.getSubmittedFileName(), part.getContentType(),  Base64.getEncoder().encodeToString(IOUtils.toByteArray(part.getInputStream())));
					}
				}
			} catch (IOException | ServletException e) {
				log.error("Failed to parse FILE part for {}", field.getResourceKey());
			}
			return getParameter(parameters, formVariablePrefix + field.getFormVariable() + "_previous");
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

	private static List<Object> convertValues(FieldTemplate field, Map<String,String[]> parameters) throws IOException, ValidationException {
		
		String fieldName = field.getFormVariable();
		
		List<Object> result = new ArrayList<>();
		String[] values = parameters.get(fieldName);
		if(Objects.isNull(values) || values.length == 0) {
			return result;
		}
		
		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
			
			ObjectMapper mapper = new ObjectMapper();

			for(String value : values) {
				if(StringUtils.isNotBlank(value)) {
					String json = new String(Base64.getUrlDecoder().decode(value), "UTF-8");
					result.add(mapper.readValue(json, MongoEntity.class).getDocument());
				}
			}

			break;
		case OBJECT_REFERENCE:
		{
			String[] names = parameters.get(String.format("%sText", field.getFormVariable()));
			for(int i=0;i<values.length;i++) {
				Document doc = new Document();
				convertObjectToDocument(generateReference(values[i], names[i]), doc);
				result.add(doc);
			}
			break;
		}
		case OPTIONS:
		{
			String nameField = field.getMetaValue("nameField", "name");
			ObjectTemplate template = ApplicationServiceImpl.getInstance().getBean(TemplateService.class).get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			ObjectService service = ApplicationServiceImpl.getInstance().getBean(ObjectService.class);
			for(int i=0;i<values.length;i++) {
				AbstractObject obj = service.get(template, values[i]);
				Document doc = new Document();
				convertObjectToDocument(generateReference(values[i], (String) obj.getValue(nameField)), doc);
				result.add(doc);
			}
			break;
		}
		default:
		{
			for(String value : values) {
				result.add(fromString(field, value));
			}
			break;
		}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Extracted {} values", field.getResourceKey(), Utils.csv(values));
		}
		
		return result;
	}

	private static UUIDReference generateReference(String uuid, String name) {
		return new UUIDReference(uuid, name);
	}

	@SuppressWarnings("unchecked")
	public static <T extends UUIDDocument> T convertDocumentToObject(Class<?> baseClass, Document document, ClassLoader classLoader) throws ObjectException, ValidationException {
		
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
			
			String resourceKey = document.getString("resourceKey");
			
			try {
				clz = processClassNameChanges(clz, classLoader);
				obj = (T) classLoader.loadClass(clz).getConstructor().newInstance();
			} catch(ClassNotFoundException | NoSuchMethodException | InstantiationException e) {
				if(Objects.nonNull(resourceKey)) {
					obj = (T) ApplicationServiceImpl.getInstance().getBean(TemplateService.class).getTemplateClass(resourceKey).getConstructor().newInstance();
				} else {		
					obj = (T) ApplicationServiceImpl.getInstance().getBean(ClassLoaderService.class).findClass(clz).getConstructor().newInstance();
				}
			}
			
			String uuid = (String) document.get("_id");
			if(Objects.isNull(uuid)) {
				uuid = document.getString("uuid");
			}
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
						try {
							m.invoke(obj, value);
						} catch(Throwable e) {
							log.error("Failed to process field {} value {} as parameter {} for {} uuid {}", name, value, parameter.getType().getSimpleName(), resourceKey, uuid);
							throw e;
						}
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
						Object objectUUID =  document.get(name);
						if(objectUUID instanceof Document) {
							objectUUID = document.get(name, Document.class).get("uuid");
						} 
						
						if(StringUtils.isNotBlank((String)objectUUID)) {
							ObjectServiceBean service = parameter.getType().getAnnotation(ObjectServiceBean.class);
							if(Objects.nonNull(service)) {
								
								UUIDObjectService<?> bean = (UUIDObjectService<?>) ApplicationServiceImpl.getInstance().getBean(service.bean());
								Object ref = bean.getObjectByUUID((String)objectUUID);
								m.invoke(obj, ref);
								
							} else {
								resourceKey = getTemplateResourceKey(parameter.getType());

								AbstractObject e = (AbstractObject) ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(resourceKey, (String)objectUUID);
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
						m.invoke(obj, new HashSet<>());
						continue;
					}
					if(UUIDEntity.class.isAssignableFrom(type)) {
						Collection<UUIDEntity> elements = new ArrayList<>();	
						for(Object embedded : list) {
							if(Objects.isNull(columnDefinition) || columnDefinition.type() == FieldType.OBJECT_EMBEDDED) {
								Document embeddedDocument = (Document) embedded;
								elements.add(convertDocumentToObject(type, embeddedDocument, classLoader));
							} else if(embedded instanceof Document) {
								UUIDReference ref = DocumentHelper.convertDocumentToObject(UUIDReference.class, (Document) embedded);
								AbstractObject e = (AbstractObject) 
										ApplicationServiceImpl.getInstance().getBean(ObjectService.class).get(
												columnDefinition.references(), ref.getUuid());
								UUIDEntity ue = convertDocumentToObject(
										ApplicationServiceImpl.getInstance().getBean(TemplateService.class).getTemplateClass(columnDefinition.references()),
										new Document(e.getDocument()), 
										classLoader); 
								elements.add(ue);
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
		} catch (SecurityException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | RepositoryException | InstantiationException | ParseException | ClassNotFoundException e) {
			log.error("Error converting document", e);
			throw new RepositoryException(String.format("Unexpected error loading UUID entity %s", baseClass.getName()), e);			
		}
		
	}

	public static void registerClassNameChange(String from, String to) {
		classNameChanges.put(from, to);
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
			assertType(t, FieldType.BOOL);
			return Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false"));
		} else if(type.equals(Boolean.class)) {
			assertType(t, FieldType.BOOL);
			return Boolean.valueOf(Boolean.parseBoolean(StringUtils.defaultIfEmpty(value, "false")));
		} else if(type.equals(int.class)) {
			assertType(t, FieldType.INTEGER);
			return Integer.parseInt(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Integer.class)) {
			assertType(t, FieldType.INTEGER);
			return Integer.valueOf(Integer.parseInt(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(long.class)) {
			assertType(t, FieldType.LONG);
			return Long.parseLong(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Long.class)) {
			assertType(t, FieldType.LONG);
			return Long.valueOf(Long.parseLong(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(float.class)) {
			assertType(t, FieldType.DECIMAL);
			return Float.parseFloat(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Float.class)) {
			assertType(t, FieldType.DECIMAL);
			return Float.valueOf(Float.parseFloat(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(double.class)) {
			assertType(t, FieldType.DECIMAL);
			return Double.parseDouble(StringUtils.defaultIfEmpty(value, "0"));
		} else if(type.equals(Double.class)) {
			assertType(t, FieldType.DECIMAL);
			return Double.valueOf(Double.parseDouble(StringUtils.defaultIfEmpty(value, "0")));
		} else if(type.equals(Date.class)) {
			if(t==FieldType.TIMESTAMP) {
				if(StringUtils.isNotBlank(value)) {
					try {
						return Utils.parseTimestamp(value);
					} catch (Throwable e) {
						return Utils.parseDateTime(value);
					}
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


	public static Object fromString(FieldTemplate def, String value) throws ValidationException {
		switch(def.getFieldType()) {
		case BOOL:
			if(StringUtils.isNotBlank(value)) {
				return DocumentValidator.validate(def,value);
			} else {
				return false;
			}
		case DECIMAL:
			if(StringUtils.isNotBlank(value)) {
				return DocumentValidator.validate(def,value);
			} else {
				return null;
			}
		case INTEGER:
			if(StringUtils.isNotBlank(value)) {
				return DocumentValidator.validate(def,value);
			} else {
				return null;
			}
		case LONG:
			if(StringUtils.isNotBlank(value)) {
				return DocumentValidator.validate(def,value);
			} else {
				return null;
			}
		case PASSWORD:
		case TEXT:
		case TEXT_AREA:
		case PERMISSION:
		case ENUM:
			return DocumentValidator.validate(def,value);
		case TIMESTAMP:
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseTimestamp(value);
			} else {
				return null;
			}
		case DATE:
			if(StringUtils.isNotBlank(value)) {
				return Utils.parseDate(value, "yyyy-MM-dd");
			} else {
				return null;
			}
		case OPTIONS:
		case TIME:
		case COUNTRY:
		{
			return DocumentValidator.validate(def,value);
		}
		default:
			throw new IllegalStateException("Unhandled field type " + def.getFieldType() + " in DocumentHelper.fromString");
		}
	}

	private static Object buildStringCollection(ObjectField columnDefinition, List<?> list) {
		Collection<String> v = new ArrayList<>();
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

		Collection<Enum<?>> v = new ArrayList<>();
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
		Collection<Date> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Utils.parseDateTime(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Double> buildDoubleCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Double> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Double.parseDouble(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Float> buildFloatCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Float> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Float.parseFloat(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Long> buildLongCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Long> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Long.parseLong(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Integer> buildIntegerCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Integer> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Integer.parseInt(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}

	private static Collection<Boolean> buildBooleanCollection(ObjectField columnDefinition, List<?> items) {
		Collection<Boolean> v = new ArrayList<>();
		for(Object item : items) {
			v.add(Boolean.parseBoolean(checkForAndPerformDecryption(columnDefinition, item.toString())));
		}
		return v;
	}
	
	public static String generateContentHash(ObjectTemplate template, Document entity) {
		
		try {
			SHA256Digest sha2 = new SHA256Digest();
			generateObjectHash(template, entity, sha2);
			byte[] tmp = new byte[16];
			new Random().nextBytes(tmp);
			sha2.update(tmp, 0, tmp.length);
			sha2.finish();
			return new String(Hex.encode(sha2.getEncodedState()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public static void generateObjectHash(ObjectTemplate template, Document entity, SHA256Digest sha2) throws UnsupportedEncodingException {


	}

}
