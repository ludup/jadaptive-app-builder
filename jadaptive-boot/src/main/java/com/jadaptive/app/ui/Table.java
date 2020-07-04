package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.BootBox;
import com.codesmith.webbits.bootstrap.BootstrapTable;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.Widgets;
import com.codesmith.webbits.freemarker.FreeMarker;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldView;

@Page({ BootstrapTable.class, BootBox.class, Widgets.class, FreeMarker.class, I18N.class})
@View(contentType = "text/html", paths = { "/table/{resourceKey}" })
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

		Element table = content.selectFirst("#table");
		table.append("<thead><tr></tr></thead>");
		Element tr = table.selectFirst("tr");
		
		for(FieldTemplate field : template.getFields()) {
			if(field.getViews().contains(FieldView.TABLE)) {
				tr.append(String.format("<th webbits:bundle=\"%s\" webbits:i18n=\"%s.name\"></th>", 
						template.getResourceKey(),
						field.getResourceKey()));
				
			}
		}
		
		tr.append("<th webbits:bundle=\"i18n\" webbits:i18n=\"actions.name\"></th>");
		Element th = tr.selectFirst("th");
		th.append("<a href=\"/app/ui/update/%s/%s\" data-uuid=\"%s\"><i class=\"far fa-edit\"></i></a>");
		th.append("<a href=\"/app/ui/view/%s/%s\" data-uuid=\"%s\"><i class=\"far fa-eye\"></i></a>");

		return content;
	}

	@Override
	public FieldView getScope() {
		return FieldView.TABLE;
	}
}
