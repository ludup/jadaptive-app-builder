package com.jadaptive.api.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.WizardState;

public class ObjectSection extends SetupSection {

	public ObjectSection(String bundle, int weight) {
		super(bundle, bundle, weight);
	}
	
	public ObjectSection(String bundle, String name, int weight) {
		super(bundle, name, weight);
	}
	
	public boolean isSystem() {
		return false;
	}

	@Override
	public void processReview(Document document, WizardState state) {
		renderObjectReview(document, state);
	}
	
	@Override
	protected void processSection(Document document, Element element, Page page) throws IOException {
		renderObjectSection(document, getName());
	}

}
