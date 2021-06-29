package com.jadaptive.api.setup;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.wizards.WizardState;

public abstract class WizardSection extends AbstractPageExtension {

	String bundle;
	String name;
	String resource;
	Integer position;
	
	public static final Integer START_OF_DEFAULT = 0;
	public static final Integer END_OF_DEFAULT = 9999;
	
	public WizardSection(String bundle, String name, String resource, Integer position) {
		this.bundle = bundle;
		this.name = name;
		this.resource = resource;
		this.position = position;
	}
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		super.process(document, element, page);
	}
	
	@Override
	public String getResource() {
		return resource;
	}
	@Override
	public String getName() {
		return name;
	}
	
	public Integer getPosition() {
		return position;
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

	public void processReview(Document document, WizardState state, Integer sectionIndex) {
		
	}

	public void finish(WizardState state, Integer sectionIndex) {
		
	}
}
