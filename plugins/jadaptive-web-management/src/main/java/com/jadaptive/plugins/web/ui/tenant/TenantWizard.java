package com.jadaptive.plugins.web.ui.tenant;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.wizards.AbstractWizard;
import com.jadaptive.api.wizards.WizardState;

@Extension
public class TenantWizard extends AbstractWizard implements FormHandler {

	public static final String RESOURCE_KEY = "createTenant";

	private static final String STATE_ATTR = "setupState";

	@Autowired
	private PageCache pageCache;
	
//	@Autowired
//	private TenantService tenantService; 
//	
//	@Autowired
//	private AdminUserDatabase adminDatabase; 
//	
//	@Autowired
//	private TransactionService transactionService;
	
	@Autowired
	private ApplicationService applicationService; 
	
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
		return new DefaultTenantSection("setup", "startTenant", "/com/jadaptive/plugins/web/ui/tenant/StartTenant.html");
	}

	@Override
	protected WizardSection getFinishSection() {
		return new DefaultTenantSection("setup", "finishTenant", "/com/jadaptive/plugins/web/ui/tenant/FinishTenant.html");
	}
	
	@Override
	protected void assertPermissions(WizardState state) throws AccessDeniedException {
		
	}
	
	@Override
	public void finish(WizardState state) {
		

	}
	
}
