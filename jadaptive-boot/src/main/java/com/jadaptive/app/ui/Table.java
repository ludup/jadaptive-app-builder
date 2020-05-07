package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;

@Page({ BootstrapTable.class, BootBox.class, Widgets.class, FreeMarker.class, PageResources.class, PageResourcesElement.class })
@View(contentType = "text/html", paths = { "/table/{resourceKey}" })
@Component
@Resource
public class Table extends TemplatePage {

	@Autowired
	private PermissionService permissionService;
	
	protected void onCreated() throws FileNotFoundException {
		
		super.onCreated();
		try {
			permissionService.assertRead(template.getResourceKey());
		} catch(AccessDeniedException e) {
			throw new FileNotFoundException();
		}
	}
	
	public boolean isParentTemplate() {
		return StringUtils.isNotBlank(template.getParentTemplate());
	}
	
	@Out
	Document service(@In Document content) {
		
		try {
			permissionService.assertReadWrite(template.getResourceKey());
		} catch(AccessDeniedException e) {
			content.select(".readWrite").remove();
		}
		
		return content;
	}
}
