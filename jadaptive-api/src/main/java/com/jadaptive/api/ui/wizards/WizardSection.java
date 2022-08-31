package com.jadaptive.api.ui.wizards;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.utils.FileUtils;

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
	public final void process(Document document, Element element, Page page) throws IOException {
		processSection(document, element, page);
	}
	
	@Override
	public String getHtmlResource() {
		return resource;
	}

	@Override
	public String getCssResource() {
		return FileUtils.checkStartsWithNoSlash(resource).replace(".html", ".css");
	}

	@Override
	public String getJsResource() {
		return FileUtils.checkStartsWithNoSlash(resource).replace(".html", ".js");
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
	
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
	}

	public boolean isSystem() {
		return false;
	}
}
