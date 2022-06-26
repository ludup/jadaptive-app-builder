package com.jadaptive.plugins.web.ui.setup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import com.jadaptive.api.ui.wizards.AbstractWizard;
import com.jadaptive.api.ui.wizards.WizardState;

@Extension
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

		
		sections.add(new SetupSection("setup", "eula", "/com/jadaptive/plugins/web/ui/setup/EULA.html") {
				@Override
				public void process(Document document, Element element, Page page) throws IOException {
					document.selectFirst("textarea")
						.attr("readonly", "readonly")
						.text(IOUtils.toString(getClass().getResource("EULA.txt"), "UTF-8"));
					super.process(document, element, page);
				}
		});
		
		
		sections.add(applicationService.autowire(new AdminSection()));
		return sections;
	}

	@Override
	protected WizardSection getStartSection() {
		return new DefaultSetupSection("setup", "startSetup", "/com/jadaptive/plugins/web/ui/setup/StartSetup.html");
	}

	@Override
	protected WizardSection getFinishSection() {
		return new DefaultSetupSection("setup", "finishSetup", "/com/jadaptive/plugins/web/ui/setup/FinishSetup.html");
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
