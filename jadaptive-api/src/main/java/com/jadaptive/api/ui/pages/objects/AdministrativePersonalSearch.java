package com.jadaptive.api.ui.pages.objects;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationDevice;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.SearchUtils;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.ext.TableRenderer;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Component
@RequestPage(path="admin-search/{resourceKey}/{uuid}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "help", "i18n"} )
public class AdministrativePersonalSearch extends AbstractSearchPage  {

	String uuid;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectService objectService;;
	
	@Autowired
	private UserService userService; 
	
	@Override
	public String getUri() {
		return "admin-search";
	}

	@Override
	public void onCreate() throws FileNotFoundException {
		super.onCreate();
		
		Request.get().getSession().removeAttribute(template.getResourceKey());
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(template.getResourceKey());
		}
		
		User user = userService.getObjectByUUID(uuid);
		Feedback.info(AuthenticationDevice.RESOURCE_KEY, "currentlyViewing.text", StringUtils.defaultIfBlank(user.getDisplayName(), user.getUsername()));
	}
	
	protected Collection<AbstractObject> generateTable(ObjectTemplate template,
			Integer start, Integer length, Map<String,FieldTemplate> searchFieldTemplates, SearchField... fields) {
		return objectService.tableObjectsNoScope(template.getResourceKey(), start, length, sortColumn, sortOrder, 
				SearchUtils.combine(fields, SearchField.eq("ownerUUID", uuid)));
	}

	@Override
	protected TableRenderer createTableRenderer(boolean readOnly, ObjectTemplate template) {
		return new TableRenderer(readOnly, template, false, false);
	}

	protected long generateCount(ObjectTemplate template, SearchField... fields) {
		return  objectService.countObjectsNoScope(template.getCollectionKey(), SearchUtils.combine(fields, SearchField.eq("ownerUUID", uuid)));
	}

}
