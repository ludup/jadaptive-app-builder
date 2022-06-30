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
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.plugins.web.objects.ConfigureDomain;
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
	
	
	@Override
	public void processReview(Document document, WizardState state) {
		

		Element content = document.selectFirst("#setupStep");
		TenantConfiguration conf = tenantConfig.getObject(TenantConfiguration.class);
		CreateTenant obj = ObjectUtils.assertObject(state.getObject(CreateTenant.class), CreateTenant.class);
		ConfigureDomain domain = ObjectUtils.assertObject(state.getObject(ConfigureDomain.class), ConfigureDomain.class);
		
		content.appendChild(new Element("div")
				.addClass("col-12 w-100 my-3")
				.appendChild(new Element("h4")
					.attr("jad:i18n", "review.tenant.header")
					.attr("jad:bundle", TenantWizard.RESOURCE_KEY))
				.appendChild(new Element("p")
						.attr("jad:bundle", TenantWizard.RESOURCE_KEY)
						.attr("jad:i18n", "review.tenant.desc"))
				.appendChild(new Element("div")
					.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
										.attr("jad:bundle", TenantWizard.RESOURCE_KEY)
										.attr("jad:i18n", "company.name")))
						.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
													.text(obj.getCompany()))))
					.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
										.attr("jad:bundle", TenantWizard.RESOURCE_KEY)
										.attr("jad:i18n", "name.name")))
						.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
													.text(obj.getName()))))
					.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
										.attr("jad:bundle", TenantWizard.RESOURCE_KEY)
										.attr("jad:i18n", "domain.name")))
						.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
													.text(String.format("%s.%s", domain.getSubdomain(), conf.getRootDomain())))))
					.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
										.attr("jad:bundle", TenantWizard.RESOURCE_KEY)
										.attr("jad:i18n", "emailAddress.name")))
						.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
													.text(obj.getEmailAddress()))))));
	
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
