package com.jadaptive.plugins.web.ui.tenant;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.tenant.TenantConfiguration;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.Wizard;
import com.jadaptive.plugins.web.objects.CreateTenant;
import com.jadaptive.utils.ObjectUtils;

public class ConfigureDomainSection extends TenantSection {

	@Autowired
	private SingletonObjectDatabase<TenantConfiguration> tenantConfig;
	
	public ConfigureDomainSection() {
		super(TenantWizard.RESOURCE_KEY,
				"configureDomain", 
				"/com/jadaptive/plugins/web/ui/tenant/ConfigureDomain.html");
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		super.process(document, element, page);
		
		TenantConfiguration conf = tenantConfig.getObject(TenantConfiguration.class);
		document.selectFirst("#domain").text(conf.getRootDomain());
		
		CreateTenant obj = ObjectUtils.assertObject(Wizard.getCurrentState().getObject(CreateTenant.class), CreateTenant.class);
		String[] elements  = obj.getCompany().split(" ");
		StringBuilder buf = new StringBuilder();
		for(String e : elements) {
			e = StringUtils.stripAccents(e);
			e = e.replaceAll("[^a-zA-Z0-9]", "");
			e = e.toLowerCase();
			if(isNotCompanyType(e)) {
				buf.append(e.toLowerCase());
			}
		}
		
		document.selectFirst("#subdomain").val(buf.toString());
	}
	
	private boolean isNotCompanyType(String element) {

		switch(element.trim().toLowerCase()) {
		case "ltd":
		case "inc":
		case "limited":
		case "plc":
		case "gmbh":
		case "ag":
		case "llc":
		case "private":
			return false;
		default:
			return true;
		}
	}
	
}
