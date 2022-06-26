package com.jadaptive.plugins.web.ui.tenant;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.Wizard;
import com.jadaptive.plugins.email.EmailNotificationService;
import com.jadaptive.plugins.web.objects.CreateTenant;
import com.jadaptive.utils.ObjectUtils;

public class ValidateEmailSection extends TenantSection {

	@Autowired
	private EmailNotificationService emailService; 
	
	public ValidateEmailSection() {
		super(TenantWizard.RESOURCE_KEY,
				"validateEmail", 
				"/com/jadaptive/plugins/web/ui/tenant/ValidateEmail.html");
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		super.process(document, element, page);
		
		CreateTenant obj = ObjectUtils.assertObject(Wizard.getCurrentState().getObject(CreateTenant.class), CreateTenant.class);
		emailService.validateEmailAddress(obj.getEmailAddress());
	
	}
}
