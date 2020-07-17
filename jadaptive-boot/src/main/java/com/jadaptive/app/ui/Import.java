package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldView;

@Page(BootstrapTable.class)
@View(contentType = "text/html", paths = { "/import/{resourceKey}"})
@Resource
public class Import extends TemplatePage {
    
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	protected void onCreated() throws FileNotFoundException {

		super.onCreated();
		
		try {
			permissionService.assertReadWrite(getTemplate().getResourceKey());
		} catch(AccessDeniedException e) {
			throw new FileNotFoundException(e.getMessage());
		}
	}

	public boolean isModal() {
		return true;
	}
	
	@Out(methods = HTTPMethod.POST)
    Document service(@In Document content) {
    	return content;
    }

	@Override
	public FieldView getScope() {
		return FieldView.IMPORT;
	}
}
