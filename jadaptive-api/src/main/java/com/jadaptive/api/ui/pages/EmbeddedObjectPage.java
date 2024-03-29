package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationType;

public abstract class EmbeddedObjectPage extends ObjectTemplatePage {

	@Autowired
	private PermissionService permissionService;

	String childUuid;
	String fieldName;
	String childResourceKey;
	ObjectTemplate childTemplate;
	Class<?> childClazz;
	AbstractObject childObject;
	
	public boolean isModal() {
		return true;
	}
	
	public String getUuid() {
		return uuid;
	}

	@Override
	public String getResourceKey() {
		return Objects.nonNull(childObject) ? childObject.getResourceKey() : childTemplate.getResourceKey();
	}
	
	protected void assertPermissions() {
		permissionService.assertWrite(template.getResourceKey());
	}
	
	public void onCreate() throws FileNotFoundException {

		super.onCreate();

		try {
			FieldTemplate field = template.getField(fieldName);
			childResourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
			childTemplate = templateService.get(childResourceKey);
			childClazz = templateService.getTemplateClass(childResourceKey);
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw e;
		} catch (ObjectException e) {
			e.printStackTrace();
			throw new FileNotFoundException(String.format("%s not found", resourceKey));
		}
		
		FieldTemplate field = template.getField(fieldName);
		if(field.getCollection()) {
			for(AbstractObject o : object.getObjectCollection(fieldName)) {
				if(o.getUuid().equals(childUuid)) {
					childObject = o;
					break;
				}
 			}
		} else {
			childObject = object.getChild(field);
		}
	}

	@Override
	public AbstractObject getObject() {
		return childObject;
	}

	protected boolean isErrorOnNotFound() {
		return true;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getChildResourceKey() {
		return childResourceKey;
	}

	public ObjectTemplate getChildTemplate() {
		return childTemplate;
	}

	public AbstractObject getChildObject() {
		return childObject;
	}
	
	
}
