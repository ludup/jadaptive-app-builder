package com.jadaptive.plugins.web.ui.tenant;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantConfiguration;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.wizards.AbstractWizard;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.plugins.web.objects.ConfigureDomain;
import com.jadaptive.plugins.web.objects.CreateTenant;
import com.jadaptive.plugins.web.ui.setup.AdminSection;
import com.jadaptive.utils.ObjectUtils;

@Extension
public class TenantWizard extends AbstractWizard implements FormHandler {

	public static final String RESOURCE_KEY = "setupTenant";

	private static final String STATE_ATTR = "setupState";

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private TenantService tenantService; 

	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private SingletonObjectDatabase<TenantConfiguration> tenantConfig;

	@Autowired
	private TemplateVersionService templateService; 
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public Page getCompletePage() throws FileNotFoundException {
		return pageCache.resolvePage(TenantComplete.class);
	}

	@Override
	protected Class<? extends WizardSection> getSectionClass() {
		return TenantSection.class;
	}

	@Override
	protected String getStateAttribute() {
		return STATE_ATTR;
	}

	@Override
	protected Collection<? extends WizardSection> getDefaultSections() {
		List<WizardSection> sections = new ArrayList<>();

		sections.add(new DefaultTenantSection(TenantWizard.RESOURCE_KEY,
				"tenantCreation", 
				"/com/jadaptive/plugins/web/ui/tenant/CreateTenant.html"));
		
		sections.add(applicationService.autowire(new ValidateEmailSection()));
		sections.add(applicationService.autowire(new ConfigureDomainSection()));
		sections.add(applicationService.autowire(new AdminSection()));
		
		for(WizardSection section : applicationService.getBeans(SetupSection.class)) {
			if(section.isSystem()) {
				continue;
			}
			sections.add(section);
		}
		
		return sections;
	}

	@Override
	protected WizardSection getStartSection() {
		return new DefaultTenantSection(RESOURCE_KEY, 
				"startTenant",
				"/com/jadaptive/plugins/web/ui/tenant/StartTenant.html");
	}

	@Override
	protected WizardSection getFinishSection() {
		return new DefaultTenantSection(RESOURCE_KEY, 
				"finishTenant", 
				"/com/jadaptive/plugins/web/ui/tenant/FinishTenant.html");
	}
	
	@Override
	public void finish(WizardState state) {
		
		CreateTenant acc = ObjectUtils.assertObject(state.getObject(CreateTenant.class), CreateTenant.class);
		ConfigureDomain domain = ObjectUtils.assertObject(state.getObject(ConfigureDomain.class), ConfigureDomain.class);
		TenantConfiguration config = tenantConfig.getObject(TenantConfiguration.class);
		
		Tenant tenant = tenantService.createTenant(acc.getCompany(), String.format("%s.%s", domain.getSubdomain(), config.getRootDomain()));
		
		tenantService.executeAs(tenant, ()-> {
			templateService.registerTenantIndexes();
			tenantService.initialiseTenant(tenant, true);
		});
		
		transactionService.executeTransaction(()-> {
			
			tenantService.executeAs(tenant, ()-> {
				for(WizardSection section : state.getSections()) {
					if(section instanceof SetupSection) {
						((SetupSection)section).finish(state);
					}
				}
				tenantService.completeSetup();
			});
		});

	}
	
}
