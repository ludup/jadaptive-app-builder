package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldView;

@Page({ BootstrapTable.class, BootBox.class, Widgets.class, FreeMarker.class, I18N.class})
@View(contentType = "text/html", paths = { "/table/{resourceKey}" })
@ClasspathResource
public class TablePage extends TemplatePage {

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

	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}
}
