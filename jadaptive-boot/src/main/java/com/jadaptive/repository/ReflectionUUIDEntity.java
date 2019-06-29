package com.jadaptive.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.datasource.DataSourceEntity;
import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.utils.Utils;

public class ReflectionUUIDEntity extends AbstractUUIDEntity implements DataSourceEntity {

	@Override
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String, String> myProperties = new HashMap<>();
		try {
			for(Method m : ReflectionUtils.getGetters(getClass())) {
				String name = ReflectionUtils.calculateFieldName(m);
				Object value = m.invoke(this);
				if(!Objects.isNull(value)) { 
					if(m.getReturnType().equals(Date.class)) {
						value = Utils.formatDateTime((Date)value);
					} else if(m.getReturnType().isAssignableFrom(AbstractUUIDEntity.class)) {
						AbstractUUIDEntity embedded = (AbstractUUIDEntity) value;
						String uuid = embedded.getUuid();
						myProperties.put(name, uuid);
						AbstractUUIDRepository<?> repository = AbstractUUIDRepositoryImpl.getRepositoryForType(m.getReturnType());
						if(!Objects.isNull(repository)) {
							// Store by reference
							repository.saveObject(embedded);
						} else {
							// Embed
							Map<String,String> eProperties = new HashMap<>();
							embedded.toMap(eProperties);
							properties.put(uuid, eProperties);
						}

					} 
					
					myProperties.put(name, String.valueOf(value));
				}
				
			}
			
			properties.put(getUuid(), myProperties);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException e) {
			throw new IllegalStateException(String.format("Unexpected error storing UUID entity %s", getClass().getName()), e);		
		}
		
	}

	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> myProperties = properties.get(uuid);
		
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
				} else if(parameter.getType().isAssignableFrom(AbstractUUIDEntity.class)) {
					
					Map<String,String> embeddedProperties = properties.get(value);
					AbstractUUIDRepository<?> repository = AbstractUUIDRepositoryImpl.getRepositoryForType(parameter.getType());
					AbstractUUIDEntity e;
					if(!Objects.isNull(repository)) {
						// Load by reference
						e = repository.get(value);
					} else {
						e = (AbstractUUIDEntity) parameter.getType().newInstance();
						e.fromMap(embeddedProperties);
					}
					
					m.invoke(this, e);
					
				} else {
					throw new IllegalStateException(String.format("Unexpected type %s in object setter %s",
							parameter.getType().getName(),
							name));
				}
				
				// TODO support embedded objects and collections of primitive types or embedded objects
				
			}
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | RepositoryException | EntityNotFoundException | InstantiationException e) {
			throw new IllegalStateException(String.format("Unexpected error loading UUID entity %s", getClass().getName()), e);			
		}
		
	}

}
