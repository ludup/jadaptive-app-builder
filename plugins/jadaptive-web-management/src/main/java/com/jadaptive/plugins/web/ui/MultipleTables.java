package com.jadaptive.plugins.web.ui;

import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.pages.TemplatePage;
import com.jadaptive.api.ui.renderers.DropdownInput;

@Extension
@RequestPage(path="tables/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "bootstrapTable", "fontawesome", "jadaptive-utils", "i18n"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class MultipleTables extends TemplatePage {
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private TemplateService templateService; 
	
	@Override
	public String getUri() {
		return "tables";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) {
		
		try {
			permissionService.assertReadWrite(template.getResourceKey());
		} catch(AccessDeniedException e) {
			document.select(".readWrite").remove();
		}
		
		document.select("#searchDropdown").first().appendChild(
				new DropdownInput("searchField", "name").renderInputWithTemplateFields(template.getFields()));
		
		Iterable<ObjectTemplate> children = templateService.children(template.getResourceKey());
		Iterator<ObjectTemplate> it = children.iterator();
		Element dropdown = document.select("#childDropdown").first();
		if(!it.hasNext()) {
			dropdown.remove();
		} else {
			DropdownInput input = new DropdownInput("searchTable", "default");
			dropdown.appendChild(input.renderInputWithValues(children, true));
		}
	}

	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}
}
