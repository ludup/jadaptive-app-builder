package com.jadaptive.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.PropertyService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;

@Service
public class PropertyServiceImpl implements PropertyService {

	@Autowired
	private TenantAwareObjectDatabase<Property> propertyDatabase;
	
	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		
		try {
			Property property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
			return Boolean.parseBoolean(property.getValue());
		} catch(ObjectNotFoundException e) {
			return defaultValue;
		}
	}
	
	@Override
	public void setBoolean(String key, boolean value) {
		
		Property property;
		try {
			property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			property = new Property();
			property.setKey(key);
		}
		property.setValue(String.valueOf(value));
		propertyDatabase.saveOrUpdate(property);
	}
	
	@Override
	public int getInteger(String key, int defaultValue) {
		
		try {
			Property property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
			return Integer.parseInt(property.getValue());
		} catch(ObjectNotFoundException e) {
			return defaultValue;
		}
	}
	
	@Override
	public void setInteger(String key, int value) {
		
		Property property;
		try {
			property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			property = new Property();
			property.setKey(key);
		}
		property.setValue(String.valueOf(value));
		propertyDatabase.saveOrUpdate(property);
	}
	
	@Override
	public String getString(String key, String defaultValue) {
		
		try {
			Property property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
			return property.getValue();
		} catch(ObjectNotFoundException e) {
			return defaultValue;
		}
	}
	
	@Override
	public void setString(String key, String value) {
		
		Property property;
		try {
			property = propertyDatabase.get(Property.class, SearchField.eq("key", key));
		} catch(ObjectNotFoundException e) {
			property = new Property();
			property.setKey(key);
		}
		property.setValue(String.valueOf(value));
		propertyDatabase.saveOrUpdate(property);
	}
	
	
}
