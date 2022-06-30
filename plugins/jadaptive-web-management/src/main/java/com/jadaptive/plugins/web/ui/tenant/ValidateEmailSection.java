package com.jadaptive.plugins.web.ui.tenant;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.Wizard;
import com.jadaptive.api.ui.wizards.WizardState;
import com.jadaptive.plugins.email.EmailVerificationService;
import com.jadaptive.plugins.web.objects.CreateTenant;
import com.jadaptive.plugins.web.objects.ValidateEmail;
import com.jadaptive.utils.ObjectUtils;

public class ValidateEmailSection extends TenantSection {

	@Autowired
	private EmailVerificationService emailService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	public ValidateEmailSection() {
		super(TenantWizard.RESOURCE_KEY,
				"validateEmail", 
				"/com/jadaptive/plugins/web/ui/tenant/ValidateEmail.html");
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		
		super.process(document, element, page);
		
		permissionService.setupSystemContext();
		
		try {
			CreateTenant obj = ObjectUtils.assertObject(Wizard.getCurrentState().getObject(CreateTenant.class), CreateTenant.class);
			emailService.verifyEmail(obj.getEmailAddress());
		} finally {
			permissionService.clearUserContext();
		}
	
	}

	@Override
	protected void onValidate(UUIDEntity object, WizardState state) {
		
		super.onValidate(object, state);
		
		permissionService.setupSystemContext();
		
		try {
			ValidateEmail obj = (ValidateEmail) object;
			CreateTenant tenant = ObjectUtils.assertObject(state.getObject(CreateTenant.class), CreateTenant.class);
			emailService.assertCode(tenant.getEmailAddress(), obj.getCode());
		} finally {
			permissionService.clearUserContext();
		}

	}
	
	
}
