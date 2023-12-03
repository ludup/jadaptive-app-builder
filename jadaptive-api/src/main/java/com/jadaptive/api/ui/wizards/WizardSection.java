package com.jadaptive.api.ui.wizards;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
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
	Integer weight;
	
	public WizardSection(String bundle, String name, String resource, Integer weight) {
		this.bundle = bundle;
		this.name = name;
		this.resource = resource;
		this.weight = weight;
	}
	
	/**
	 * Automatic support for section as class name with HTML in classpath as
	 * either &lt;classname&gt;.html or &lt;classname&gt;Section.html
	 * @param bundle
	 */
	public WizardSection(String bundle, Integer weight) {
		this.bundle = bundle;
		this.name = StringUtils.uncapitalize(getClass().getSimpleName());
		this.resource = "/" + getClass().getName().replace(".", "/") + ".html";
		this.weight = weight;
	}
	
	public WizardSection(String bundle, String name, Integer weight) {
		this.bundle = bundle;
		this.name = name;
		this.resource = "/" + getClass().getName().replace(".", "/") + ".html";
		this.weight = weight;
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
		state.setCurrentObject(object);
	}

	protected void onValidate(UUIDEntity object, WizardState state) {
		
	}

	public void processReview(Document document, WizardState state) {
		
	}
	
	protected void processSection(Document document, Element element, Page page) throws IOException {
		
	}
	
	public void finish(WizardState state) {
		
	}
	
	public boolean isEnabled() {
		return true;
	}

	public boolean isSystem() {
		return false;
	}

	public Integer getWeight() {
		return weight;
	}

	public String getStateKey() {
		return getName();
	}

	public String getObjectName() {
		return getName();
	}

	public String getStepNamei18n() {
		return getName() + ".stepName";
	}
	
	public String getStepSummaryi18n() {
		return getName() + ".summary";
	}
}
