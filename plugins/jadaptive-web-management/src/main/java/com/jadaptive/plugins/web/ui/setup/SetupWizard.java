package com.jadaptive.plugins.web.ui.setup;

import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.plugins.web.objects.CreateAccount;
import com.jadaptive.plugins.web.objects.CreateInterface;
import com.jadaptive.plugins.web.wizard.WizardFlow;
import com.jadaptive.plugins.web.wizard.WizardState;

@Extension
public class SetupWizard implements WizardFlow, FormHandler {

	public static final String RESOURCE_KEY = "setup";

	private static final String STATE_ATTR = "setupState";
	
	@Autowired
	ApplicationService applicationService; 

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@Override
	public WizardState getState(HttpServletRequest request) {
		WizardState state = (WizardState) request.getSession().getAttribute(STATE_ATTR);
		if(Objects.isNull(state)) {
			state = new WizardState(this);
			state.init(applicationService.getBean(StartSetup.class), 
					applicationService.getBean(FinishSetup.class), 
					applicationService.getBean(EULA.class), 
					applicationService.getBean(AdminCredentials.class),
					applicationService.getBean(ConfigureInterface.class),
					applicationService.getBean(SelectMount.class)); 
			request.getSession().setAttribute(STATE_ATTR, state);
		}
		return state;
	}
	
	@Override
	public void processReview(Document document) {
		
		Element content = document.selectFirst("#setupStep");
		
		WizardState state = getState(Request.get());
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
												.text(state.getAccount().getUsername()))))
					.appendChild(new Element("div")
							.addClass("col-3")
							.appendChild(new Element("span")
											.attr("jad:bundle", "createAccount")
											.attr("jad:i18n", "email.name")))
					.appendChild(new Element("div")
							.addClass("col-9")
							.appendChild(new Element("span")
									.appendChild(new Element("strong")
									.text(state.getAccount().getEmail()))))
					.appendChild(new Element("div")
							.addClass("col-3")
							.appendChild(new Element("span")
											.attr("jad:bundle", "setup")
											.attr("jad:i18n", "review.credentials.password")))
					.appendChild(new Element("div")
							.addClass("col-9")
							.appendChild(new Element("span")
									.appendChild(new Element("strong")
									.text(state.getAccount().getFirstPassword().charAt(0)
											+ "*********"
											+ state.getAccount().getFirstPassword().charAt(
													state.getAccount().getFirstPassword().length()-1)))))));
		
		content	.appendChild(new Element("div")
						.addClass("col-12 w-100 my-3")
						.appendChild(new Element("h4")
							.attr("jad:i18n", "review.interface.header")
							.attr("jad:bundle", "setup"))
					.appendChild(new Element("p")
							.attr("jad:bundle", "setup")
							.attr("jad:i18n", "review.interface.desc"))
					.appendChild(new Element("div")
							.addClass("row")
							.appendChild(new Element("div")
									.addClass("col-3")
									.appendChild(new Element("span")
											.attr("jad:bundle", "createInterface")
											.attr("jad:i18n", "addressToBind.name")))
							.appendChild(new Element("div")
										.addClass("col-9")
										.appendChild(new Element("span")
												.appendChild(new Element("strong")
														.text(state.getInterface().getAddressToBind()))))
							.appendChild(new Element("div")
									.addClass("col-3")
									.appendChild(new Element("span")
													.attr("jad:bundle", "createInterface")
													.attr("jad:i18n", "port.name")))
							.appendChild(new Element("div")
									.addClass("col-9")
									.appendChild(new Element("span")
											.appendChild(new Element("strong")
											.text(String.valueOf(state.getInterface().getPort())))))));
	}

	@Override
	public <T extends UUIDEntity> String saveObject(T object) {
		
		object.setUuid(UUID.randomUUID().toString());

		WizardState state = getState(Request.get());
		
		if(object instanceof CreateAccount) {
			CreateAccount obj = (CreateAccount) object;
			if(!obj.getFirstPassword().equals(obj.getSecondPassword())) {
				throw new ValidationException(CreateAccount.RESOURCE_KEY, "passwords.dontMatch");
			}
			state.setAccount(obj);
		} else if(object instanceof CreateInterface) {
			CreateInterface obj = (CreateInterface) object;
			validateAddressToBind(obj);
			state.setInterface(obj);
		}
		
		return object.getUuid();
	}

	private void validateAddressToBind(CreateInterface obj) {
		// TODO Auto-generated method stub
		
	}

}
