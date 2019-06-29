package com.jadaptive.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Map;

import com.jadaptive.datasource.DataSourceEntity;

public class ReflectionUUIDEntity extends AbstractUUIDEntity implements DataSourceEntity {

	@Override
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		
		try {
			for(Method m : getClass().getMethods()) {
				if(m.getName().startsWith("get") && m.getName().length() > 3) {
					String name = m.getName().substring(3,1).toLowerCase() + m.getName().substring(4);
					Object value = m.invoke(this);
					
				}
			}
			
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		
		Map<String,String> myProperties = properties.get(uuid);
		
		for(Method m : getClass().getMethods()) {
			if(m.getName().startsWith("set") && m.getName().length() > 3) {
				String name = m.getName().substring(3,1).toLowerCase() + m.getName().substring(4);
				String value = myProperties.get(name);
				
				
			}
		}
		
	}

}
