package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.util.Objects;

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

	String uuid;
	
	protected AbstractObject object;

	public boolean isModal() {
		return true;
	}
	
	public String getUuid() {
		return uuid;
	}

	@Override
	public String getResourceKey() {
		return Objects.nonNull(object) ? object.getResourceKey() : template.getResourceKey();
	}
	
	public void created() throws FileNotFoundException {

		super.created();

		try {
			if (Objects.nonNull(uuid)) {
				object = objectService.get(template.getResourceKey(), uuid);
			} else if (template.getType() == ObjectType.SINGLETON) {
				object = objectService.getSingleton(template.getResourceKey());
			}

			try {
				permissionService.assertReadWrite(template.getResourceKey());
			} catch (AccessDeniedException e) {
				switch (getScope()) {
				case CREATE:
				case UPDATE:
				case IMPORT:
					throw new FileNotFoundException(
							String.format("You do not have permission to %s", getScope().name().toLowerCase()));
				default:
					try {
						permissionService.assertRead(template.getResourceKey());
					} catch (AccessDeniedException ex) {
						throw new FileNotFoundException(
								String.format("You do not have permission to %s", getScope().name().toLowerCase()));
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
}
