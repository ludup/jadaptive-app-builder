package com.jadaptive.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.utils.Utils;

public abstract class AbstractUUIDEntity {

	static Logger log = LoggerFactory.getLogger(AbstractUUIDEntity.class);
			
	String uuid;
	Boolean system;
	Boolean hidden;
	
	public String getUuid() {
		return Objects.isNull(uuid) ? uuid = UUID.randomUUID().toString() : uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Boolean getSystem() {
		return system==null ? Boolean.FALSE : system;
	}

	public void setSystem(Boolean system) {
		this.system = system;
	}

	public Boolean getHidden() {
		return hidden==null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		
		log.info("Begin {}", getClass().getSimpleName());
		
		Map<String, String> myProperties = new HashMap<>();
		try {
			for(Method m : ReflectionUtils.getGetters(getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Object value = m.invoke(this);
				if(!Objects.isNull(value)) { 
					if(m.getReturnType().equals(Date.class)) {
						value = Utils.formatDateTime((Date)value);
					} else if(AbstractUUIDEntity.class.isAssignableFrom(m.getReturnType())) {
						value = storeObject((AbstractUUIDEntity)value, m.getReturnType(), properties);
					} else if(Collection.class.isAssignableFrom(m.getReturnType())) {
						value = storeCollection((Collection<?>)value, m.getReturnType(), properties);
					}
					
					log.info("{} : {}", name, String.valueOf(value));
					myProperties.put(name, String.valueOf(value));
				}
			}
			
			log.info("End {}", getClass().getSimpleName());
			properties.put(getUuid(), myProperties);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException e) {
			throw new IllegalStateException(String.format("Unexpected error storing UUID entity %s", getClass().getName()), e);		
		}
		
	}

	private String storeCollection(Collection<?> values, Class<?> returnType, Map<String, Map<String, String>> properties) throws ParseException {
		
		StringBuffer elementIndex = new StringBuffer();
		
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
				if(elementIndex.length() > 0) {
					elementIndex.append(",");
				}
				elementIndex.append(e.getUuid());
				e.store(properties);

			} else if(value.getClass().isEnum()) {
				
			} 
		}
		
		return elementIndex.toString();
	}

	private String storeObject(AbstractUUIDEntity object, Class<?> type, Map<String,Map<String,String>> properties) throws RepositoryException, ParseException {
		
		AbstractUUIDEntity embedded = (AbstractUUIDEntity) object;

		AbstractUUIDRepository<?> repository = AbstractUUIDRepositoryImpl.getRepositoryForType(type);
		if(!Objects.isNull(repository)) {
			repository.saveObject(embedded);
		} else {
			embedded.store(properties);
		}
		return embedded.getUuid();
	}

	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> myProperties = properties.get(uuid);
		
		if(Objects.isNull(myProperties)) {
			return;
		}
		try {
			for(Method m : ReflectionUtils.getSetters(getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Parameter parameter = m.getParameters()[0];
				String value = myProperties.get(name);
				if(parameter.getType().equals(String.class)) {
					m.invoke(this, value);
				} else if(parameter.getType().equals(Boolean.class)) {
					m.invoke(this, Boolean.parseBoolean(value));
				} else if(parameter.getType().equals(Integer.class)) {
					m.invoke(this, Integer.parseInt(value));
				} else if(parameter.getType().equals(Long.class)) {
					m.invoke(this, Long.parseLong(value));
				} else if(parameter.getType().equals(Float.class)) {
					m.invoke(this, Float.parseFloat(value));
				} else if(parameter.getType().equals(Double.class)) {
					m.invoke(this, Double.parseDouble(value));
				} else if(parameter.getType().equals(Date.class)) {
					m.invoke(this, Utils.parseDateTime(value));
				} else if(Collection.class.isAssignableFrom(parameter.getType())) { 
					ParameterizedType o = (ParameterizedType) parameter.getParameterizedType();
					Class<?> type = (Class<?>) o.getActualTypeArguments()[0];
					String elementIndex = myProperties.get(name);
					if(type.isEnum()) {
						 
					} else if(AbstractUUIDEntity.class.isAssignableFrom(type)) {
						Set<AbstractUUIDEntity> elements = new HashSet<>();	
						if(StringUtils.isNotBlank(elementIndex)) {
							for(String elementUUID : elementIndex.split(",")) {
								elements.add(loadObject(elementUUID, type, properties));
							}
						}
						m.invoke(this, elements);
						
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
					m.invoke(this, loadObject(value, parameter.getType(), properties));
				} else if(parameter.getType().isEnum()) { 
					Enum<?>[] enumConstants = (Enum<?>[]) parameter.getType().getEnumConstants();
					if(StringUtils.isBlank(value)) {
						m.invoke(this, (Object)null);
						return;  
					}
					if(NumberUtils.isCreatable(value)) {
						Enum<?> enumConstant = enumConstants[Integer.parseInt(value)];
						m.invoke(this, enumConstant);
						return;  
					} else {//name
						for (Enum<?> enumConstant : enumConstants) {
							if(enumConstant.name().equals(value)){
								m.invoke(this, enumConstant);
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
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | EntityNotFoundException | InstantiationException e) {
			throw new IllegalStateException(String.format("Unexpected error loading UUID entity %s", getClass().getName()), e);			
		}
		
	}
	
	private AbstractUUIDEntity loadObject(String uuid, Class<?> type, Map<String,Map<String,String>> properties) throws RepositoryException, EntityNotFoundException, InstantiationException, IllegalAccessException, ParseException {
		AbstractUUIDRepository<?> repository = AbstractUUIDRepositoryImpl.getRepositoryForType(type);
		AbstractUUIDEntity e;
		if(!Objects.isNull(repository)) {
			e = repository.get(uuid);
		} else {
			e = (AbstractUUIDEntity) type.newInstance();
			e.load(uuid, properties);
		}
		return e;
	}
}
