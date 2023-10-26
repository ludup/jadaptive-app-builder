package com.jadaptive.api.auth.reset;

import java.io.IOException;
import java.util.Arrays;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.PasswordResetAuthenticationPolicy;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.setup.ObjectSection;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.utils.ObjectUtils;

@Component
public class AuthenticationModulesSection extends ObjectSection {

	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private AuthenticationPolicyService policyService; 
	
	public AuthenticationModulesSection() {
		super(PasswordResetPolicyWizard.RESOURCE_KEY, SelectModules.RESOURCE_KEY, 9999);
	}

	@Override
	protected void onValidate(UUIDEntity object, WizardState state) {
		
		SelectModules modules = ObjectUtils.assertObject(object, SelectModules.class);
		if(modules.getRequiredModules().isEmpty()) {
			if(modules.getOptionalModules().isEmpty()) {
				// No authentication modules selected
				throw new IllegalStateException("Invalid policy! You have not seleted ANY require or optional modules.");
			}
		}
		if(modules.getOptionalModules().size() < modules.getRequireOptional()) {
			// Requires more optional than selected
			throw new IllegalStateException("Invalid policy! You require more optional credentials than are selected.");
		}
		if(modules.getOptionalModules().size() == modules.getRequireOptional()) {
			// Optional effectively required
			throw new IllegalStateException("Invalid policy! The number of optional credentials cannot be equal to the required optional credentials value.");
		}
		for(AuthenticationModule m : modules.getRequiredModules()) {
			if(modules.getOptionalModules().contains(m)) {
				throw new IllegalStateException("Invalid policy! You cannot set a module as both required and optional.");
			}
		}
		
		super.onValidate(object, state);
	}

	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
		super.processSection(document, element, page);
		
		element.after(Html.div("row").appendChild(
				Html.div("col-12")
					.appendChild(Html.p(PasswordResetPolicyWizard.RESOURCE_KEY, "messaging.text")
							.addClass("alert alert-success"))));
	}

	@Override
	public void finish(WizardState state) {
		
		SelectModules modules = state.getObject(SelectModules.class);
		
		PasswordResetAuthenticationPolicy policy = new PasswordResetAuthenticationPolicy();
		policy.setName("Password Reset Policy");
		policy.setOptionalAuthenticators(modules.getOptionalModules());
		policy.setRequiredAuthenticators(modules.getRequiredModules());
		policy.setOptionalRequired(modules.getRequireOptional());
		policy.setRoles(Arrays.asList(roleService.getEveryoneRole()));
		policy.setWeight(0);
		
		policyService.saveOrUpdate(policy);
		
		super.finish(state);
	}
	
	

	
}
