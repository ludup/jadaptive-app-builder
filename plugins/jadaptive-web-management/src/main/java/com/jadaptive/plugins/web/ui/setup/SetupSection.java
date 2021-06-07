package com.jadaptive.plugins.web.ui.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.plugins.web.wizard.WizardService;
import com.jadaptive.plugins.web.wizard.WizardState;

public abstract class SetupSection extends AbstractPageExtension {

	@Autowired
	private WizardService wizardService; 
	
	@Override
	public void process(Document document, Page page) throws IOException {
		super.process(document, page);
		
		

	}


	
}
