package com.jadaptive.plugins.web.ui.tenant;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantConfiguration;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.wizards.WizardService;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.plugins.web.objects.ConfigureDomain;
import com.jadaptive.utils.ObjectUtils;

@Extension
@RequestPage(path="tenant-complete")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
public class TenantComplete extends HtmlPage {

	@Autowired
	private WizardService wizardService; 
	
	@Autowired
	private SingletonObjectDatabase<TenantConfiguration> tenantConfig;
	
	@Override
	public String getUri() {
		return "tenant-complete";
	}

	@Override
	protected void generateContent(Document document) throws IOException {
		
		WizardState state = wizardService.getWizard(TenantWizard.RESOURCE_KEY).getState(Request.get());
		
		ConfigureDomain domain = ObjectUtils.assertObject(state.getObject(ConfigureDomain.class), ConfigureDomain.class);
		TenantConfiguration config = tenantConfig.getObject(TenantConfiguration.class);
		
		if(!state.isFinished()) {
			throw new IllegalStateException("Incomplete tenant wizard!");
		}

		if(Request.get().getServerPort() != -1) {
			document.selectFirst("#tenantURL").attr("href", 
					String.format("https://%s.%s:%d" , 
							domain.getSubdomain(), config.getRootDomain(), 
								Request.get().getServerPort()));
		} else {
			document.selectFirst("#tenantURL").attr("href", 
					String.format("https://%s.%s" , 
							domain.getSubdomain(), config.getRootDomain()));
		}
		
		
		wizardService.getWizard(TenantWizard.RESOURCE_KEY).clearState(Request.get());
		
		super.generateContent(document);
	}
}
