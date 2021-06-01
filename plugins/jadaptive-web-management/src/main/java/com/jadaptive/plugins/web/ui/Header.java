package com.jadaptive.plugins.web.ui;

import java.util.Objects;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.Page;

@Extension
public class Header extends AbstractPageExtension {

	@Autowired
	private PermissionService permissionService;
	
	@Override
	public void process(Document document, Page page) {
		if(Objects.nonNull(page.getClass().getAnnotation(ModalPage.class))) {
			document.select("script").remove();
			document.select("#searchForm").remove();
			document.select("#topMenu").remove();
		}
		
		if(!permissionService.hasUserContext()) {
			document.select("script").remove();
			document.select("#searchForm").remove();
			document.select("#topMenu").remove();
			document.select("#logoff").remove();
		} 
	}

	@Override
	public String getName() {
		return "header";
	}

}
