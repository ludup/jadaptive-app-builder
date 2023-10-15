package com.jadaptive.api.ui.wizards.setup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.product.ProductService;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.wizards.AbstractWizard;
import com.jadaptive.api.ui.wizards.DefaultWizardSection;
import com.jadaptive.api.ui.wizards.WizardSection;
import com.jadaptive.api.ui.wizards.WizardState;

@Component
public class SetupWizard extends AbstractWizard implements FormHandler {

	public static final String RESOURCE_KEY = "setup";

	private static final String STATE_ATTR = "setupState";

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ProductService productService; 
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public Page getCompletePage() throws FileNotFoundException {
		return pageCache.resolvePage(SetupComplete.class);
	}

	@Override
	protected Class<? extends WizardSection> getSectionClass() {
		return SetupSection.class;
	}
	
	@Override
	public boolean requiresUserSession() {
		return false;
	}

	@Override
	protected String getStateAttribute() {
		return STATE_ATTR;
	}
	
	@Override
	protected Collection<? extends WizardSection> getDefaultSections() {
		List<SetupSection> sections = new ArrayList<>();

		if(productService.requiresRegistration()) {
			sections.add(new SetupSection("setup", "createTenant", 
				"/com/jadaptive/api/ui/wizards/setup/CreateTenant.html", 1));
		}
		
		sections.add(applicationService.autowire(new AdminSection(productService.requiresRegistration())));
		return sections;
	}

	@Override
	protected WizardSection getStartSection() {
		return new DefaultWizardSection("setup", "startSetup", "/com/jadaptive/api/ui/wizards/setup/StartSetup.html", 0);
	}
	
	@Override
	protected void assertPermissions(WizardState state) throws AccessDeniedException {
		
		if(!state.isFinished() && (state.getFlow().getClass().equals(SetupWizard.class) && !tenantService.isSetupMode())) {
			throw new AccessDeniedException("Setup wizard can only be run in setup mode!");
		}
	}
	
	@Override
	public void finish(WizardState state) {
		
		transactionService.executeTransaction(()-> {
			for(WizardSection section : state.getSections()) {
				((SetupSection)section).finish(state);
			}
		});

		tenantService.completeSetup();
	}
	
}
