package com.jadaptive.plugins.web.ui.setup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.api.wizards.AbstractWizard;
import com.jadaptive.api.wizards.WizardState;

@Extension
public class TenantWizard extends AbstractWizard implements FormHandler {

	public static final String RESOURCE_KEY = "createTenant";

	private static final String STATE_ATTR = "setupState";

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private AdminUserDatabase adminDatabase; 
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ApplicationService applicationService; 
	
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
	protected String getStateAttribute() {
		return STATE_ATTR;
	}

	@Override
	protected Collection<? extends WizardSection> getDefaultSections() {
		List<SetupSection> sections = new ArrayList<>();

		sections.add(applicationService.autowire(new CreateTenantSection()));
		return sections;
	}

	@Override
	protected WizardSection getStartSection() {
		return new DefaultSetupSection("setup", "startTenant", "/com/jadaptive/plugins/web/ui/setup/StartTenant.html");
	}

	@Override
	protected WizardSection getFinishSection() {
		return new DefaultSetupSection("setup", "finishTenant", "/com/jadaptive/plugins/web/ui/setup/FinishTenant.html");
	}
	
	@Override
	protected void assertPermissions(WizardState state) throws AccessDeniedException {
		
	}
	
	@Override
	public void finish(WizardState state) {
		

	}
	
}
