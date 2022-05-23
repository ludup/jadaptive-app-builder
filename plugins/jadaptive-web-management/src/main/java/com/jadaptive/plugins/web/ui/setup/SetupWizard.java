package com.jadaptive.plugins.web.ui.setup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.api.wizards.AbstractWizard;
import com.jadaptive.api.wizards.WizardState;
import com.jadaptive.plugins.web.objects.CreateAccount;
import com.jadaptive.utils.ObjectUtils;
import com.jadaptive.utils.Utils;

@Extension
public class SetupWizard extends AbstractWizard implements FormHandler {

	public static final String RESOURCE_KEY = "setup";

	private static final String STATE_ATTR = "setupState";

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private AdminUserDatabase adminDatabase; 
	
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
		return Arrays.asList(
				new SetupSection("setup", "eula", "/com/jadaptive/plugins/web/ui/setup/EULA.html") {
					@Override
					public void process(Document document, Element element, Page page) throws IOException {
						document.selectFirst("textarea")
							.attr("readonly", "readonly")
							.text(IOUtils.toString(getClass().getResource("EULA.txt"), "UTF-8"));
						super.process(document, element, page);
					}
				},
				new AdminSection());
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
		
		if(!state.isFinished() && !tenantService.isSetupMode()) {
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
	
	class AdminSection extends SetupSection {

		public AdminSection() {
			super("setup",
					"adminCredentials", 
					"/com/jadaptive/plugins/web/ui/setup/AdminCredentials.html");
		}
		
		public Integer getPosition() {
			return 2;
		}

		protected void onValidate(UUIDEntity object, WizardState state) {
			
			CreateAccount obj = ObjectUtils.assertObject(object, CreateAccount.class);
			if(!obj.getFirstPassword().equals(obj.getSecondPassword())) {
				throw new ValidationException(CreateAccount.RESOURCE_KEY, "passwords.dontMatch");
			}

		}
		
		@Override
		public void finish(WizardState state) {
			
			CreateAccount account = ObjectUtils.assertObject(
					state.getObject(getClass()), 
					CreateAccount.class);
			
			adminDatabase.createAdmin(account.getUsername(), 
					account.getFirstPassword().toCharArray(), 
					account.getEmail(), false);

		}
		
		@Override
		public void processReview(Document document, WizardState state) {
	
			Element content = document.selectFirst("#setupStep");
			CreateAccount account = ObjectUtils.assertObject(state.getObject(getClass()), CreateAccount.class);
			
			content.appendChild(new Element("div")
					.addClass("col-12 w-100 my-3")
					.appendChild(new Element("h4")
						.attr("jad:i18n", "review.credentials.header")
						.attr("jad:bundle", "setup"))
					.appendChild(new Element("p")
							.attr("jad:bundle", "setup")
							.attr("jad:i18n", "review.credentials.desc"))
					.appendChild(new Element("div")
						.addClass("row")
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
										.attr("jad:bundle", "createAccount")
										.attr("jad:i18n", "username.name")))
						.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
													.text(account.getUsername()))))
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
												.attr("jad:bundle", "createAccount")
												.attr("jad:i18n", "email.name")))
						.appendChild(new Element("div")
								.addClass("col-9")
								.appendChild(new Element("span")
										.appendChild(new Element("strong")
										.text(account.getEmail()))))
						.appendChild(new Element("div")
								.addClass("col-3")
								.appendChild(new Element("span")
												.attr("jad:bundle", "setup")
												.attr("jad:i18n", "review.credentials.password")))
						.appendChild(new Element("div")
								.addClass("col-9")
								.appendChild(new Element("span")
										.appendChild(new Element("strong")
										.text(Utils.maskingString(account.getFirstPassword(), 2, "*")))))));
		}
	}
	
}
