package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;

public abstract class ObjectPage extends TemplatePage {

	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	protected String uuid;
	protected AbstractObject object;
	
	public boolean isModal() {
		return true;
	}
	
	protected void onCreated() throws FileNotFoundException {

		super.onCreated();
	
		if(Objects.isNull(uuid) && (template.getType()==ObjectType.COLLECTION || template.getType()==ObjectType.OBJECT)) {
			throw new FileNotFoundException("UUID required for COLLECTION or OBJECT type");
		} else if(Objects.nonNull(uuid) && template.getType()==ObjectType.SINGLETON) {
			throw new FileNotFoundException("UUID not required for SINGLETON type");
		}
		
		try {
			if(Objects.nonNull(uuid)) {
				object = objectService.get(template.getResourceKey(), uuid);
			} else if(template.getType()==ObjectType.SINGLETON) {
				object = objectService.getSingleton(template.getResourceKey());
			}
			
			try {
				permissionService.assertReadWrite(template.getResourceKey());
			} catch(AccessDeniedException e) { 
				switch(getScope()) {
				case CREATE:
				case UPDATE:
				case IMPORT:
					throw new FileNotFoundException(String.format(
							"You do not have permission to %s", 
							getScope().name().toLowerCase()));
				default:
					try {
						permissionService.assertRead(template.getResourceKey());
					} catch(AccessDeniedException ex) {
						throw new FileNotFoundException(String.format(
								"You do not have permission to %s", 
								getScope().name().toLowerCase()));
					}
				}

 			}
		} catch (ObjectNotFoundException nse) {
			throw new FileNotFoundException(String.format("No %s with id %s", resourceKey, uuid));
		}
		
	}

	public AbstractObject getObject() {
		return object;
	}
	
	protected boolean isErrorOnNotFound() {
		return true;
	}
	
	public String getUuid() {
		return StringUtils.defaultString(uuid);
	}
}
