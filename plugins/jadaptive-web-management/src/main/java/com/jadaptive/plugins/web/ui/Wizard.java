package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.plugins.web.ui.setup.SetupWizard;
import com.jadaptive.plugins.web.wizard.WizardService;
import com.jadaptive.plugins.web.wizard.WizardState;

@Extension
@RequestPage(path="wizards/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class Wizard extends HtmlPage {

	@Autowired
	private WizardService wizardService;
	
	String resourceKey;
	WizardState state;
	
	public WizardState getState() {
		return state;
	}
	
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		state = wizardService.getWizard(resourceKey).getState(request);
	}
	
	@Override
	protected void processPost(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		state.moveNext();
		throw new UriRedirect("/app/ui/wizards/" + state.getResourceKey());
	}

	@Override
	protected void generateContent(Document document) throws FileNotFoundException {
		super.generateContent(document);
		
		AbstractPageExtension ext = state.getCurrentPage();
		Element el = document.selectFirst("#setupStep");
		el.attr("jad:id", ext.getName());
		
		Element actions = document.selectFirst("#actions");
		
		WizardState state = wizardService.getWizard(SetupWizard.RESOURCE_KEY).getState(Request.get());
		
		Element content = document.selectFirst("#content");
		if(!state.isStartPage()) {
			Element h2;
			content.prependChild(new Element("div")
					.addClass("col-12")
					.appendChild(h2 = new Element("h2")));
			
			if(!state.isFinishPage()) {
					h2.appendChild(new Element("span")
						.attr("jad:bundle", state.getResourceKey())
						.attr("jad:i18n", "step.name"))
					.appendChild(new Element("span")
							.text(" " + String.valueOf(state.getCurrentStep())))
					.appendChild(new Element("span")
									.text(" - "))
					.appendChild(new Element("span")
							.addClass("ml-1")
							.attr("jad:bundle", state.getResourceKey())
							.attr("jad:i18n", "step." + state.getCurrentStep() + ".name"));
			} else {
				h2.appendChild(new Element("span")
						.attr("jad:bundle", state.getResourceKey())
						.attr("jad:i18n", "finish.name"));
			}
		}
		
		if(!state.isStartPage()) {
			content.prependChild(new Element("div")
					.addClass("col-12")
					.appendChild(new Element("h1")
							.attr("jad:bundle", state.getResourceKey())
							.attr("jad:i18n", "wizard.name")));
		}
     
		if(state.hasBackButton()) {
			actions.appendChild(new Element("a")
					.attr("id", "backButton")
					.addClass("btn btn-danger float-left")
					.appendChild(new Element("i")
						.addClass("far fa-arrow-circle-left mr-1"))
					.appendChild(new Element("span")
						.attr("jad:bundle", "default")
						.attr("jad:i18n", "back.name")));
		}
		
		if(state.hasNextButton()) {
			actions.appendChild(new Element("a")
						.attr("id", "nextButton")
						.addClass("btn btn-success float-right")
						.appendChild(new Element("i")
							.addClass("far fa-arrow-circle-right mr-1"))
						.appendChild(new Element("span")
							.attr("jad:bundle", "default")
							.attr("jad:i18n", "next.name")));
		} else if(state.isFinishPage()) {
			actions.appendChild(new Element("a")
						.attr("id", "finishButton")
						.addClass("btn btn-primary float-right")
					.appendChild(new Element("i")
						.addClass("far fa-rocket mr-1"))
					.appendChild(new Element("span")
							.attr("jad:bundle", "default")
							.attr("jad:i18n", "finish.name")));
		}
	}
	
	@Override
	public String getUri() {
		return "wizards";
	}

//	public void doGet(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
//		
//		WizardFlow wizard = wizardService.getWizard(resourceKey);
//		WizardState state = wizard.getState(Request.get());
//		
//		currentState.set(state);
//		
//		try {
//			currentStep startPage = state.getCurrentPage();
//			Request.get().getSession().setAttribute(CURRENT_PAGE, startPage);
//			startPage.doGet(uri, request, response);
//		} finally {
//			currentState.remove();
//		}
//	}
//
//	public void doPost(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
//		
//		WizardFlow wizard = wizardService.getWizard(resourceKey);
//		WizardState state = wizard.getState(Request.get());
//		
//		currentState.set(state);
//		
//		try {
//			Page startPage = state.getCurrentPage();
//			Request.get().getSession().setAttribute(CURRENT_PAGE, startPage);
//			startPage.doPost(uri, request, response);
//		} finally {
//			currentState.remove();
//		}
//	}

}
