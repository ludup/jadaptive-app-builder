package com.jadaptive.app.ui;

import java.io.IOException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.ParentView;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;

@Widget({ BootstrapTable.class, PageResources.class, PageResourcesElement.class })
@View(contentType = "text/html")
@Resource
public class Table {

	 @Out
	    public Elements service(@In Elements contents, @ParentView TemplatePage page) throws IOException {
		
		
		Element table = contents.select("#table").first();
		table.attr("data-resourcekey", page.getTemplate().getResourceKey());
		table.append("<thead><tr></tr></thead>");
		Element tr = table.selectFirst("tr");
		
		for(FieldTemplate field : page.getTemplate().getFields()) {
			if(field.getViews().contains(FieldView.TABLE)) {
				tr.append(String.format("<th webbits:bundle=\"i18n/%s\" webbits:i18n=\"%s.%s.name\" data-field=\"%s\"></th>", 
						page.getTemplate().getResourceKey(),
						page.getTemplate().getResourceKey(),
						field.getResourceKey(),
						field.getResourceKey()));
				
			}
		}
		
		tr.append("<th webbits:bundle=\"i18n\" webbits:i18n=\"default.actions.name\" data-formatter=\"renderActions\"></th>");
		Element th = tr.selectFirst("th");
		th.append("<a href=\"/app/ui/update/%s/%s\" data-uuid=\"%s\"><i class=\"far fa-edit\"></i></a>");
		th.append("<a href=\"/app/ui/view/%s/%s\" data-uuid=\"%s\"><i class=\"far fa-eye\"></i></a>");

		return contents;
	 }
}
