package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.ObjectPage;

public abstract class ObjectTemplatePage extends TemplatePage implements ObjectPage {
	
	@Autowired
	protected ObjectService objectService;

	@Autowired
	protected PermissionService permissionService;

	protected String uuid;
	
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
	
	protected void assertPermissions() {
		
		if(template.hasParent()) {
			ObjectTemplate parent = templateService.getParentTemplate(template);
			permissionService.assertWrite(parent.getResourceKey());
		} else {
			permissionService.assertWrite(resourceKey);
		}
	}
	
	@Override
	protected void doGenerateTemplateContent(Document document) throws FileNotFoundException, IOException {
		
		Element body = document.selectFirst("body");
		if(Objects.nonNull(body)) {
			body.attr("data-resourcekey", template.getResourceKey());
		}
		
		Element element = document.selectFirst("#cancelButton");
		
		if(Objects.nonNull(element)) {
			element.attr("href", getCancelURI());
			element.attr("data-resourcekey", template.getCollectionKey());
		}
	}
	
	protected String getCancelURI() {
		return Request.get().getHeader(HttpHeaders.REFERER);
	}

	public void onCreate() throws FileNotFoundException {

		super.onCreate();

		try {
			
			Object obj = Request.get().getSession().getAttribute(template.getResourceKey());
			if(Objects.nonNull(obj)) {
				if(obj instanceof AbstractObject) {
					object = (AbstractObject)obj;
				} else if(obj instanceof UUIDEntity) {
					object = objectService.toAbstractObject((UUIDEntity) obj);
				}
				
			} else {
				if (Objects.nonNull(uuid)) {
					object = objectService.get(template.getResourceKey(), uuid);
				} else if (template.getType() == ObjectType.SINGLETON) {
					object = objectService.getSingleton(template.getResourceKey());
				} else {
					object = objectService.createNew(template);
				}
			}

			try {
				if(template.getPermissionProtected()) {
					assertPermissions();
				}
			} catch (AccessDeniedException e) {
				switch (getScope()) {
				case CREATE:
				case UPDATE:
				case IMPORT:
					throw new FileNotFoundException(
							String.format("You do not have permission to %s", getScope().name().toLowerCase()));
				default:
					try {
						if(template.getPermissionProtected()) {
							permissionService.assertRead(template.getResourceKey());
						}
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

	@Override
	public AbstractObject getObject() {
		return object;
	}

	protected boolean isErrorOnNotFound() {
		return true;
	}
}
