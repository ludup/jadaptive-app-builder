package com.jadaptive.api.ui.pages.objects;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
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
}
