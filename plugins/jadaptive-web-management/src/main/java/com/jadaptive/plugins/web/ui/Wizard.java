package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.wizards.WizardService;
import com.jadaptive.api.wizards.WizardState;
import com.jadaptive.plugins.web.ui.setup.SetupWizard;

@Extension
@RequestPage(path="wizards/{resourceKey}")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "freemarker", "i18n"} )
public class Wizard extends HtmlPage implements ObjectPage {

	@Autowired
	private WizardService wizardService;
	
	@Autowired
	private ObjectService objectService; 
	
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

	protected Class<?> getResourceClass() {
		return Wizard.class;
	}
	
	@Override
	protected void generateContent(Document document) throws IOException {
		super.generateContent(document);
		
		WizardState state = wizardService.getWizard(SetupWizard.RESOURCE_KEY).getState(Request.get());
		
		WizardSection ext = state.getCurrentPage();
	
		Element el = document.selectFirst("#setupStep");
		doProcessEmbeddedExtensions(document, el, ext);
		
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
							.addClass("ml-1")
							.attr("jad:bundle", state.getCurrentPage().getBundle())
							.attr("jad:i18n", state.getCurrentPage().getName() + ".setup.name"));
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
			
			state.getFlow().processReview(document, state);
			
			Integer index = Integer.valueOf(1);
			for(WizardSection section : state.getSections()) {
				section.processReview(document, state, index);
				index++;
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
			WizardState state = wizardService.getWizard(SetupWizard.RESOURCE_KEY).getState(Request.get());
			UUIDEntity obj = state.getCurrentObject();
			if(Objects.isNull(obj)) {
				return null;
			}
			return objectService.convert(obj);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);		
		}
	}

}
