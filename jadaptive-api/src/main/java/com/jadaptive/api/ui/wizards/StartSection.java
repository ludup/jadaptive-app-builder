package com.jadaptive.api.ui.wizards;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;

public class StartSection extends DefaultWizardSection {

	String title;
	String header;
	String content;
	
	public StartSection(WizardFlow wizard) {
		super(wizard.getBundle(), wizard.getResourceKey(), 0);	
	}
	
	public StartSection(String bundle, String name) {
		super(bundle, name, 0);	
	}
	
	public StartSection(WizardFlow wizard, String title, String header, String content) {
		this(wizard);
		this.title = title;
		this.header = header;
		this.content = content;
	}

	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
		if(Objects.isNull(title)) {
			document.selectFirst("title").appendChild(Html.i18n(bundle, "wizard.name"));
			document.selectFirst("#startHeader").appendChild(Html.i18n(bundle, "wizard.name"));
			document.selectFirst("#startInfo").appendChild(Html.i18n(bundle, "wizard.desc"));
		} else {
			document.selectFirst("title").appendChild(Html.span(title));
			document.selectFirst("#startHeader").appendChild(Html.span(header));
			document.selectFirst("#startInfo").appendChild(Html.div().html(content));
		}
		
	}

}
