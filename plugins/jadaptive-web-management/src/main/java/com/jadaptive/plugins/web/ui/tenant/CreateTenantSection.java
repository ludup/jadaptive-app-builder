package com.jadaptive.plugins.web.ui.tenant;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.ui.Page;

public class CreateTenantSection extends SetupSection {

	public CreateTenantSection() {
		super("setup",
		"tenantCreation", 
		"/com/jadaptive/plugins/web/ui/tenant/CreateTenant.html");
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		super.process(document, element, page);
		
		document.selectFirst("#domain").text(".securefile.exchange");
	}

}
