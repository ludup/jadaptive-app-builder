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
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.ui.Html;
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
@PageProcessors(extensions = { "i18n"} )
@ModalPage
public class Wizard extends HtmlPage implements ObjectPage {

	@Autowired
	private WizardService wizardService;
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private ClassLoaderService classService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	private String resourceKey;
	private WizardState state;
	
	private static ThreadLocal<WizardState> currentState = new ThreadLocal<>();
	
	public WizardState getState() {
		return state;
	}
	
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		super.beforeProcess(uri, request, response);
		state = wizardService.getWizard(resourceKey).getState(request);
		if(state.getFlow().requiresUserSession() && Session.getOr(request).isEmpty()) {
			throw new AccessDeniedException();
		}

		currentState.set(state);
	}
	
	protected void afterProcess(String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		super.afterProcess(uri, request, response);
		currentState.remove();
	}

	public static WizardState getCurrentState() {
		return currentState.get();
	}
	
	public static void setCurrentState(WizardState state) {
		currentState.set(state);
	}
	
	public static void clearCurrentState() {
		currentState.remove();
	}

	public Class<?> getResourceClass() {
		return Wizard.class;
	}
	
	@Override
	protected void generateContent(Document document) throws IOException {
		
		super.generateContent(document);
		
		permissionService.asSystem(()->{
			WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
			
			if(state.isFinished()) {
				throw new PageRedirect(state.getCompletePage());
			}

			Element body = document.selectFirst("body");
			
			body.attr("jad:wizard", resourceKey);
			
			WizardSection ext = state.getCurrentPage();
			ext.beforeProcess(document, state);
			
			Element el = document.selectFirst("#wizardContent");

			injectHtmlSection(document, el, ext);
			
			/**
			 * This is here to remove the previous style of wizard where an info alert
			 * panel is used. We now render this automatically below so this code removes
			 * it from any HTML files. Once the HTML code-base has been cleaned this code
			 * can be removed.
			 */
			Element wizardContent = document.selectFirst("#wizardContent");
			if(Objects.nonNull(wizardContent) && wizardContent.childNodeSize() > 0) {
				Element div = wizardContent.child(0);
				if(div.tag().getName().equals("div") && div.childNodeSize() > 0) {
					Element e = div.child(0);
					if(e.tag().getName().equals("p") && e.hasClass("alert-info")) {
						div.remove();
					}
				}
			}
			
			Element actions = document.selectFirst("#actions");
			Element content = document.selectFirst("#content");
			
			if(!state.isStartPage()) {

				Element h2;
				content.prependChild(new Element("div")
						.addClass("col-12")
						.appendChild(h2 = new Element("h2")));
				
				content.prependChild(new Element("div")
						.addClass("col-12")
						.appendChild(new Element("h1")
								.attr("jad:bundle", state.getBundle())
								.attr("jad:i18n", "wizard.name")));
				
				if(!state.isFinishPage()) {
						h2.appendChild(new Element("span")
							.attr("jad:bundle", "setup")
							.attr("jad:i18n", "step.name"))
						.appendChild(new Element("span")
								.text(" " + String.valueOf(state.getDisplayStep())))
						.appendChild(new Element("span")
										.text(" - "))
						.appendChild(new Element("span")
								.addClass("ms-1")
								.attr("jad:bundle", state.getCurrentPage().getBundle())
								.attr("jad:i18n", state.getCurrentPage().getStepNamei18n()));
						
						
						h2.after(new Element("h4")
											.appendChild(Html.i("fa-solid fa-info-square text-primary me-2"))
											.appendChild(Html.i18n(state.getCurrentPage().getBundle(),
															state.getCurrentPage().getStepSummaryi18n()))
															.addClass("my-3 text-primary"));
						
				} else {
					h2.appendChild(new Element("span")
							.attr("jad:bundle", state.getCurrentPage().getBundle())
							.attr("jad:i18n", "finish.name"));
				}
				
				document.selectFirst("#cancelButton").attr("href", "/app/api/wizard/cancel/" + resourceKey);
			} else {
				document.selectFirst("#cancelButton").remove();
			} 

			Element form = body.selectFirst("form");
			if(Objects.nonNull(form)) {
				if(form.id().equals("")) {
					form.attr("id", resourceKey + "_form");
				}
			}
			
			if(!state.hasBackButton()) {
				document.selectFirst("#backButton").remove();
			} else {
				if(Objects.nonNull(form)) {
					document.selectFirst("#backButton")
						.attr("form",  form.id());
				}
			}
			
			if(!state.hasNextButton()) {
				document.selectFirst("#nextButton").remove();
			} else {
				if(Objects.nonNull(form)) {
					document.selectFirst("#nextButton")
						.attr("form",  form.id());
				}
			}
			
			if(state.isFinishPage()) {
				document.selectFirst("#backButton").parent().insertChildren(0, new Element("button")
							.attr("id", "finishButton")
							.addClass("btn btn-primary wizardFinish me-3")
						.appendChild(new Element("i")
							.addClass("fa-solid fa-rocket me-1"))
						.appendChild(new Element("span")
								.attr("jad:bundle", "default")
								.attr("jad:i18n", "finish.name")));
				
				for(WizardSection section : state.getSections()) {
					if(!section.isHidden()) {
						section.processReview(document, state);
					}
				}
			}

			ext.afterProcess(document, state);
			
			return null;
		});

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
		
		WizardState state = wizardService.getWizard(resourceKey).getState(Request.get());
		
		AbstractObject o = objectService.fromStashToAbstractObject(state.getCurrentPage().getObjectName());
		if(Objects.nonNull(o)) {
			state.setCurrentObject((UUIDEntity)objectService.toUUIDDocument(o));
			return o;
		}

		UUIDEntity obj = state.getObject(state.getCurrentPage());
		if(Objects.isNull(obj)) {
			return null;
		}
		return objectService.toAbstractObject(obj);
		
	}

}
