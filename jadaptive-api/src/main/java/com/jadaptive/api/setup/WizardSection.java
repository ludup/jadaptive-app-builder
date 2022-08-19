package com.jadaptive.api.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.wizards.WizardState;

public abstract class WizardSection extends AbstractPageExtension {

	String bundle;
	String name;
	String resource;
	
	public WizardSection(String bundle, String name, String resource) {
		this.bundle = bundle;
		this.name = name;
		this.resource = resource;
	}
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		

	}
	
	@Override
	public String getHtmlResource() {
		return resource;
	}

	@Override
	public String getCssResource() {
		return resource.replace(".html", ".css");
	}

	@Override
	public String getJsResource() {
		return resource.replace(".html", ".js");
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getBundle() {
		return bundle;
	}

	public void validateAndSave(UUIDEntity object, WizardState state) {
		onValidate(object, state);
		state.saveObject(object);
	}

	protected void onValidate(UUIDEntity object, WizardState state) {
		
	}

	public void processReview(Document document, WizardState state) {
		
	}

	public boolean isSystem() {
		return false;
	}
}
