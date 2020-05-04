package com.jadaptive.app.ui;

import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.extensions.Bind;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;

@Widget({ Bind.class, PageResources.class, PageResourcesElement.class, FreeMarker.class })
@View(contentType = "text/html")
@Resource(path = "Dropdown.html")
@Component
public class ChildTemplateDropdown extends TemplateDropdown {

	@Autowired
	private EntityTemplateService templateService;
	
	@Override
	protected void onService(Elements contents, TemplatePage page) {
		
		Elements menu = contents.select(".dropdown-menu");
		for(EntityTemplate child : templateService.children(page.getTemplate().getParentTemplate())) {
			menu.append("<a data-resourceKey=\"" 
					+ child.getResourceKey() 
					+ "\" class=\"dropdown-item\" href=\"#\">" 
					+ child.getName() 
					+"</a>");
		}

	}

}
