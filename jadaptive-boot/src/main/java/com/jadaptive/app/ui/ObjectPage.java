package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.entity.EntityType;
import com.jadaptive.app.entity.MongoEntity;

public class ObjectPage extends TemplatePage {

	@Autowired
	private EntityService<MongoEntity> objectService; 
	
	protected String uuid;
	protected MongoEntity object;
	
	protected void onCreated() throws FileNotFoundException {

		super.onCreated();
	
		if(Objects.isNull(uuid) && (template.getType()==EntityType.COLLECTION || template.getType()==EntityType.OBJECT)) {
			throw new FileNotFoundException("UUID required for COLLECTION or OBJECT type");
		} else if(Objects.nonNull(uuid) && template.getType()==EntityType.SINGLETON) {
			throw new FileNotFoundException("UUID not required for SINGLETON type");
		}
		
		try {
			if(Objects.nonNull(uuid)) {
				object = objectService.get(template.getResourceKey(), uuid);
			} else if(template.getType()==EntityType.SINGLETON) {
				object = objectService.getSingleton(template.getResourceKey());
			}
		} catch (EntityNotFoundException nse) {
			throw new FileNotFoundException(String.format("No %s with id %s", resourceKey, uuid));
		}
		
	}

	public MongoEntity getObject() {
		return object;
	}
	
	protected boolean isErrorOnNotFound() {
		return true;
	}
	
	public String getUuid() {
		return StringUtils.defaultString(uuid);
	}
}
