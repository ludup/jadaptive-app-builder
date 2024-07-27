package com.jadaptive.api.ui.pages;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@Component
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" })
@PageProcessors(extensions = { "i18n" })
@PageMenu(parent = ApplicationMenuService.SYSTEM_MENU_UUID, icon = "fa-address-card", weight = Integer.MAX_VALUE, withPermission = "tenant.read", path = "/app/ui/about", bundle = "userInterface", i18n = "about.name")
public class About extends AuthenticatedPage {

	@Autowired
	private ProductService productService;

	@Override
	public String getUri() {
		return "about";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws IOException {
		document.getElementById("productName").html(productService.getProductName());
		document.getElementById("vendor").html(productService.getVendor());
		document.getElementById("copyright").html(productService.getCopyright());
		document.getElementById("poweredBy").html(productService.getPoweredBy());
		document.getElementById("version").html(productService.getVersion());
	}

}
