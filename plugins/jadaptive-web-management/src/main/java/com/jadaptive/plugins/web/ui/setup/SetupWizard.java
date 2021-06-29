package com.jadaptive.plugins.web.ui.setup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.setup.SetupSection;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.api.user.User;
import com.jadaptive.api.wizards.WizardFlow;
import com.jadaptive.api.wizards.WizardState;
import com.jadaptive.plugins.web.objects.CreateAccount;
import com.jadaptive.plugins.web.objects.CreateInterface;
import com.jadaptive.plugins.web.ui.Login;
import com.jadaptive.utils.ObjectUtils;
import com.jadaptive.utils.Utils;

@Extension
public class SetupWizard extends AbstractWizard implements WizardFlow, FormHandler {

	public static final String RESOURCE_KEY = "setup";

	private static final String STATE_ATTR = "setupState";
	
	@Autowired
	private ApplicationService applicationService; 

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private AdminUserDatabase adminDatabase; 
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@Override
	public WizardState getState(HttpServletRequest request) {
		WizardState state = (WizardState) request.getSession().getAttribute(STATE_ATTR);
		if(Objects.isNull(state)) {
			state = new WizardState(this);
			
			List<SetupSection> sections = new ArrayList<>();
			sections.addAll(Arrays.asList(
					new SetupSection("setup", "eula", "/com/jadaptive/plugins/web/ui/setup/EULA.html", SetupSection.START_OF_DEFAULT),
					new AdminSection(),
					new InterfaceSection()));
			sections.addAll(applicationService.getBeans(SetupSection.class));
			
			Collections.sort(sections, new Comparator<SetupSection>() {

				@Override
				public int compare(SetupSection o1, SetupSection o2) {
					return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
				}
				
			});
			state.init(new SetupSection("setup", "startSetup", "/com/jadaptive/plugins/web/ui/setup/StartSetup.html", -1),
					new SetupSection("setup", "finishSetup", "/com/jadaptive/plugins/web/ui/setup/FinishSetup.html", -1), 
					sections.toArray(new SetupSection[0])); 
			request.getSession().setAttribute(STATE_ATTR, state);
		}
		return state;
	}
	
	@Override
	public void processReview(Document document, WizardState state) {
		
	}

	@Override
	public <T extends UUIDEntity> String saveObject(T object) {
		
		if(StringUtils.isBlank(object.getUuid())) {
			object.setUuid(UUID.randomUUID().toString());
		}
		WizardState state = getState(Request.get());
		
		state.getCurrentPage().validateAndSave(object, state);
	
		return object.getUuid();
	}

	class InterfaceSection extends SetupSection {

		public InterfaceSection() {
			super("setup", 
					"configureInterface", 
					"/com/jadaptive/plugins/web/ui/setup/ConfigureInterface.html", 
					SetupSection.START_OF_DEFAULT + 3);
		}
		
		@Override
		public void processReview(Document document, WizardState state, Integer sectionIndex) {
	
			Element content = document.selectFirst("#setupStep");
			CreateInterface iface = ObjectUtils.assertObject(state.getObjectAt(sectionIndex), CreateInterface.class);
			
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
															.text(iface.getAddressToBind()))))
								.appendChild(new Element("div")
										.addClass("col-3")
										.appendChild(new Element("span")
														.attr("jad:bundle", "createInterface")
														.attr("jad:i18n", "port.name")))
								.appendChild(new Element("div")
										.addClass("col-9")
										.appendChild(new Element("span")
												.appendChild(new Element("strong")
												.text(String.valueOf(iface.getPort())))))));
		}
		
	}
	
	class AdminSection extends SetupSection {

		private static final String ADMIN_UUID = "adminUUID";

		public AdminSection() {
			super("setup",
					"adminCredentials", 
					"/com/jadaptive/plugins/web/ui/setup/AdminCredentials.html", 
					SetupSection.START_OF_DEFAULT + 2);
		}

		protected void onValidate(UUIDEntity object, WizardState state) {
			
			CreateAccount obj = ObjectUtils.assertObject(object, CreateAccount.class);
			if(!obj.getFirstPassword().equals(obj.getSecondPassword())) {
				throw new ValidationException(CreateAccount.RESOURCE_KEY, "passwords.dontMatch");
			}

		}
		
		public void finish(WizardState state, Integer sectionIndex) {
			
			CreateAccount account = ObjectUtils.assertObject(
					state.getObjectAt(sectionIndex), 
					CreateAccount.class);
			
			String uuid = (String) state.getParameter(ADMIN_UUID);
			if(StringUtils.isNotBlank(uuid)) {
				adminDatabase.deleteObject(adminDatabase.getObjectByUUID(uuid));
			}
			User user = adminDatabase.createAdmin(account.getUsername(), 
					account.getFirstPassword().toCharArray(), 
					account.getEmail(), false);
			state.setParameter(ADMIN_UUID,user.getUuid());
		}
		
		@Override
		public void processReview(Document document, WizardState state, Integer sectionIndex) {
	
			Element content = document.selectFirst("#setupStep");
			CreateAccount account = ObjectUtils.assertObject(state.getObjectAt(sectionIndex), CreateAccount.class);
			
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

	@Override
	public Page getCompletePage() throws FileNotFoundException {
		return pageCache.resolvePage(Login.class);
	}

	@Override
	public void finish() {
		
		WizardState state = (WizardState) Request.get().getSession().getAttribute(STATE_ATTR);
		if(Objects.isNull(state)) {
			throw new IllegalStateException();
		}
		
		Integer sectionIndex = 1;
		for(WizardSection section : state.getSections()) {
			section.finish(state, sectionIndex++);
		}
		
		state.setFinished(true);
		tenantService.completeSetup();
	}
	
}
