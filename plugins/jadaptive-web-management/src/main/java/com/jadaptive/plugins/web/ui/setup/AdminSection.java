package com.jadaptive.plugins.web.ui.setup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.plugins.web.objects.CreateAccount;
import com.jadaptive.utils.ObjectUtils;
import com.jadaptive.utils.Utils;

public class AdminSection extends SetupSection {

	@Autowired
	private AdminUserDatabase adminDatabase;
	
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
				state.getObject(this), 
				CreateAccount.class);
		
		adminDatabase.createAdmin(account.getUsername(), 
				account.getFirstPassword().toCharArray(), 
				account.getEmail(), false);

	}
	
	@Override
	public void processReview(Document document, WizardState state) {

		Element content = document.selectFirst("#setupStep");
		CreateAccount account = ObjectUtils.assertObject(state.getObject(this), CreateAccount.class);
		
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