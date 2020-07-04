package com.jadaptive.app.ui;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.app.ui.renderers.DropdownInput;

@Page({ BootstrapTable.class, BootBox.class, Widgets.class, FreeMarker.class })
@View(contentType = "text/html", paths = { "/tables/{resourceKey}" })
@Resource
public class ParentTable extends TemplatePage {

	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private TemplateService templateService; 
	
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
		
		/**
		 * TODO: Fix this 
		 */
		new DropdownInput(content.select("#searchDropdown"),
				"searchField", "name");
//					.renderValues(new ArrayList<FieldTemplate>(template.getFields()));
		
		Iterable<ObjectTemplate> children = templateService.children(template.getResourceKey());
		Iterator<ObjectTemplate> it = children.iterator();
		if(!it.hasNext()) {
			content.select("#childDropdown").remove();
		} else {
			new DropdownInput(content.select("#childDropdown"), "searchTable", 
					it.next().getResourceKey())
				.renderValues(children);
		}
		return content;
	}

	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}
}
