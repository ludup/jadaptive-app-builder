package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.utils.FileUtils;

@Component
@RequestPage(path="wizards/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
@ModalPage
public class Wizard extends HtmlPage implements ObjectPage {

	@Autowired
	private WizardService wizardService;
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private ClassLoaderService classService; 
	
	String resourceKey;
	WizardState state;
	
	static ThreadLocal<WizardState> currentState = new ThreadLocal<>();
	
	public WizardState getState() {
		return state;
	}
	
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		state = wizardService.getWizard(resourceKey).getState(request);
		currentState.set(state);
	}
	
	protected void afterProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		currentState.remove();
	}

	public static WizardState getCurrentState() {
		return currentState.get();
	}

	protected Class<?> getResourceClass() {
		return Wizard.class;
	}
	
	@Override
	protected void generateContent(Document document) throws IOException {
		super.generateContent(document);
		
		WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
		
		if(state.isFinished()) {
			throw new PageRedirect(state.getCompletePage());
		}
		
		document.selectFirst("body").attr("jad:wizard", resourceKey);
		
		WizardSection ext = state.getCurrentPage();
		Element el = document.selectFirst("#wizardContent");

		injectHtmlSection(document, el, ext);
		
		Element actions = document.selectFirst("#actions");
		Element content = document.selectFirst("#content");
		if(!state.isStartPage()) {
			Element h2;
			content.prependChild(new Element("div")
					.addClass("col-12")
					.appendChild(h2 = new Element("h2")));
			
			if(!state.isFinishPage()) {
					h2.appendChild(new Element("span")
						.attr("jad:bundle", "setup")
						.attr("jad:i18n", "step.name"))
					.appendChild(new Element("span")
							.text(" " + String.valueOf(state.getCurrentStep())))
					.appendChild(new Element("span")
									.text(" - "))
					.appendChild(new Element("span")
							.addClass("ms-1")
							.attr("jad:bundle", state.getCurrentPage().getBundle())
							.attr("jad:i18n", state.getCurrentPage().getName() + ".stepName"));
			} else {
				h2.appendChild(new Element("span")
						.attr("jad:bundle", state.getCurrentPage().getBundle())
						.attr("jad:i18n", "finish.name"));
			}
		}
		
		if(!state.isStartPage()) {
			content.prependChild(new Element("div")
					.addClass("col-12")
					.appendChild(new Element("h1")
							.attr("jad:bundle", state.getBundle())
							.attr("jad:i18n", "wizard.name")));
		} 
     
		if(state.hasBackButton()) {
			actions.appendChild(new Element("a")
					.attr("id", "backButton")
					.addClass("btn btn-danger float-start wizardBack")
					.appendChild(new Element("i")
						.addClass("fa-solid fa-arrow-circle-left me-1"))
					.appendChild(new Element("span")
						.attr("jad:bundle", "default")
						.attr("jad:i18n", "back.name")));
		}
		
		if(state.hasNextButton()) {
			actions.appendChild(new Element("a")
						.attr("id", "nextButton")
						.addClass("btn btn-success float-end wizardNext")
						.appendChild(new Element("i")
							.addClass("fa-solid fa-arrow-circle-right me-1"))
						.appendChild(new Element("span")
							.attr("jad:bundle", "default")
							.attr("jad:i18n", "next.name")));
		} else if(state.isFinishPage()) {
			actions.appendChild(new Element("a")
						.attr("id", "finishButton")
						.addClass("btn btn-primary float-end wizardFinish")
					.appendChild(new Element("i")
						.addClass("fa-solid fa-rocket me-1"))
					.appendChild(new Element("span")
							.attr("jad:bundle", "default")
							.attr("jad:i18n", "finish.name")));
			
			for(WizardSection section : state.getSections()) {
				section.processReview(document, state);
			}
		}
		
	}
	
	protected void documentComplete(Document document) throws IOException {

		super.documentComplete(document);
		
		WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
		WizardSection ext = state.getCurrentPage();

		URL url = ext.getClass().getResource(ext.getJsResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendHeadScript(document, "/app/script/" +
					ext.getClass().getPackage().getName().replace('.', '/') 
						+ "/" + FileUtils.checkStartsWithNoSlash(ext.getJsResource()));
		} else {
			url = classService.getResource(ext.getJsResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendHeadScript(document, "/app/script/" +
						FileUtils.checkStartsWithNoSlash(ext.getJsResource()));
			}
		}
		
		url = ext.getClass().getResource(ext.getCssResource());
		if(Objects.nonNull(url)) {
			PageHelper.appendStylesheet(document, "/app/style/" +
					ext.getClass().getPackage().getName().replace('.', '/') 
						+ "/" + FileUtils.checkStartsWithNoSlash(ext.getCssResource()));
		} else {
			url = classService.getResource(ext.getCssResource());
			if(Objects.nonNull(url)) {
				PageHelper.appendStylesheet(document, "/app/style/" +
						FileUtils.checkStartsWithNoSlash(ext.getCssResource()));
			}
		}
	}
	
	@Override
	public String getUri() {
		return "wizards";
	}

	@Override
	public AbstractObject getObject() {
		try {
			WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
			UUIDEntity obj = state.getObject(state.getCurrentPage());
			if(Objects.isNull(obj)) {
				return null;
			}
			return objectService.convert(obj);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);		
		}
	}

}
