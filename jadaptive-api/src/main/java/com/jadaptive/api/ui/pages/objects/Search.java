package com.jadaptive.api.ui.pages.objects;

import java.io.FileNotFoundException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;

@Component
@RequestPage(path="search/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "help", "i18n"} )
public class Search extends AbstractSearchPage  {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ObjectService objectService;;
	
	@Override
	public String getUri() {
		return "search";
	}

	@Override
	public void onCreate() throws FileNotFoundException {
		super.onCreate();
		
		Request.get().getSession().removeAttribute(template.getResourceKey());
		
		if(template.getPermissionProtected()) {
			permissionService.assertRead(template.getResourceKey());
		}

		if(!template.getCollectionKey().equals(resourceKey)) {
			throw new UriRedirect(String.format("/app/ui/search/%s", template.getCollectionKey()));
		}
		
		if(template.isSingleton()) {
			throw new UriRedirect(String.format("/app/ui/config/%s", template.getCollectionKey()));
		}
	}
	
	protected Collection<AbstractObject> generateTable(ObjectTemplate template, String searchField, String searchValue,
			Integer start, Integer length) {
		return objectService.table(template.getResourceKey(), searchField, searchValue, start, length);
	}

	protected long generateCount(ObjectTemplate template, String searchField2, String searchValue2) {
		return  objectService.count(template.getCollectionKey(), searchField, searchValue);
	}

}
