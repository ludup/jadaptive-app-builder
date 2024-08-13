package com.jadaptive.api.ui.wizards.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.Wizard;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.utils.ObjectUtils;
import com.jadaptive.utils.Utils;

public class AdminSection extends SetupSection {

	@Autowired
	private AdminUserDatabase adminDatabase;
	
	@Autowired
	private TenantService tenantService; 
	
	private boolean setOwner;
	
	public AdminSection(boolean setOwner) {
		super("setup", 2);
		this.setOwner = setOwner;
	}
	
	public Integer getPosition() {
		return 2;
	}

	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
		try {
			@SuppressWarnings("unused")
			CreateAccount obj = (CreateAccount) Wizard.getCurrentState().getObject(CreateAccount.class);
		} catch(IllegalStateException e) {
			
			try {
				CreateTenant obj = ObjectUtils.assertObject(Wizard.getCurrentState().getObject(CreateTenant.class), CreateTenant.class);
				CreateAccount acc = new CreateAccount();
				acc.setUsername("admin");
				acc.setEmail(obj.getEmailAddress());
				Wizard.getCurrentState().setCurrentObject(acc);
			} catch(Throwable e2) {
				// This just means there is no default
			}
		
		}
		
		super.processSection(document, element, page);
	}

	protected void onValidate(UUIDEntity object, WizardState state) {
		
		CreateAccount obj = ObjectUtils.assertObject(object, CreateAccount.class);
		if(!obj.getFirstPassword().equals(obj.getSecondPassword())) {
			throw new ValidationException(CreateAccount.RESOURCE_KEY, "passwords.dontMatch");
		}

	}
	
	@Override
	public void finish(WizardState state) {
		
		CreateAccount account = state.getObject(CreateAccount.class);
		
		adminDatabase.createAdmin(account.getUsername(), 
				account.getFirstPassword().toCharArray(), 
				account.getEmail(), false);
		
		if(setOwner) {
			CreateTenant obj = state.getObject(CreateTenant.class);
			tenantService.setSystemOwner(obj.getCompany(), obj.getName(), account.getEmail());
		}

	}
	
	@Override
	public void processReview(Document document, WizardState state) {

		Element content = document.selectFirst("#wizardContent");
		CreateAccount account = state.getObject(CreateAccount.class);
		
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