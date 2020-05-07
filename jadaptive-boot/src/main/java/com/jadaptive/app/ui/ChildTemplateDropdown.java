package com.jadaptive.app.ui;

import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;

@Widget
@View(contentType = "text/html")
@Resource(path = "Dropdown.html")
@Component
public class ChildTemplateDropdown extends TemplateDropdown {

	@Autowired
	private EntityTemplateService templateService; 
	
	@Override
	protected void onService(Elements contents, TemplatePage page) {
		
		String content = "<div id=\"${resourceKey}Dropdown\" class=\"input-group dropdown\">\n" + 
				"	<input id=\"${resourceKey}\" name=\"${resourceKey}\" type=\"hidden\">\n" + 
				"	<input id=\"${resourceKey}Text\" class=\"dropdown-toggle form-control\" readonly=\"readonly\" \n" + 
				"		type=\"text\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\">\n" + 
				"	<div class=\"input-group-append\">\n" + 
				"       <span class=\"input-group-text\"><i class=\"fas fa-chevron-down\"></i></span>\n" + 
				"    </div>\n" + 
				"	<div id=\"${resourceKey}DropdownMenu\" class=\"dropdown-menu\" aria-labelledby=\"${resourceKey}Dropdown\"></div>\n" + 
				"</div>\n" + 
				"<script type=\"text/javascript\">\n" + 
				"	$(document).ready(function(e) {\n" + 
				"		$('.${resourceKey}Click').click(function(e) {\n" + 
				"			e.preventDefault();\n" + 
				"			$('#${resourceKey}').val($(this).data('resourcekey'));\n" + 
				"			$('#${resourceKey}Text').val($(this).text());\n" + 
				"		});\n" + 
				"	});\n" + 
				"</script>";
		
		content = content.replace("${resourceKey}", page.getTemplate().getResourceKey());
		contents.append(content);
		
		for(EntityTemplate t : templateService.children(page.getTemplate().getParentTemplate())) {
			contents.select("#" + page.getTemplate().getResourceKey() + "Dropdown")
					.append("<a data-resourcekey=\"" + t.getResourceKey() + "\" class=\"" 
								+ page.getTemplate().getResourceKey() + "Click dropdown-item\" href=\"#\">" + t.getName() + "</a>");
		}
	}

}
