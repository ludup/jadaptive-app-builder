package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.AuthenticatedPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class TemplatePage extends AuthenticatedPage {

	@Autowired
	protected TemplateService templateService;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	protected SessionUtils sessionUtils;
	
	protected String resourceKey;
	protected String displayKey;
	
	protected ObjectTemplate template;
	protected Class<?> templateClazz;

	public ObjectTemplate getTemplate() {
		return template;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public Class<?> getTemplateClass() {
		return templateClazz;
	}
	
	protected ObjectTemplate findTemplate() {
		return templateService.get(getResourceKey());
	}

	public void onCreate() throws FileNotFoundException {
		
		super.onCreate();
		try {
			resourceKey = getResourceKey();
			template = findTemplate();
			templateClazz = templateService.getTemplateClass(getResourceKey());
			displayKey = getResourceKey();

			if(!tenantService.getCurrentTenant().isSystem() && template.isSystem()) {
				throw new FileNotFoundException(String.format("%s not found", resourceKey));
			}
			
			
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw e;
		} catch (ObjectException e) {
			e.printStackTrace();
			throw new FileNotFoundException(String.format("%s not found", resourceKey));
		}
	
	}
	
	protected void beforeForm(Document document, HttpServletRequest request, HttpServletResponse response) {
		
		try {
			sessionUtils.verifySameSiteRequest(request, template);
		} catch (UnauthorizedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		beforeGenerateContent(document);

	}
		
	protected void setupTemplate(Document document) {
		
		for(Element e : document.select("[jad:bundle]")) {
			e.attr("jad:bundle", e.attr("jad:bundle")
					.replace("${page.template.bundle}", template.getBundle()
							.replace("${page.template.name}", template.getBundle())));
		}
		
		for(Element e : document.select("[jad:i18n]")) {
			e.attr("jad:i18n", e.attr("jad:i18n")
					.replace("${page.template.resourceKey}", template.getResourceKey()));
		}
		
		for(Element e : document.select("[jad:help]")) {
			e.attr("jad:help", e.attr("jad:help")
					.replace("${page.template.resourceKey}", template.getResourceKey()));
		}

		Element form = document.selectFirst("#form");
		if(Objects.nonNull(form)) {
			String action = form.attr("action");
			document.selectFirst("#form").attr("action", action.replace("${page.template.resourceKey}", template.getResourceKey()));
		}
		
	}

	
	@Override
	protected final void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {

		beforeGenerateContent(document);
		doGenerateTemplateContent(document);
		afterGenerateContent(document);
	}
	
	protected void afterGenerateContent(Document document) {
		
	}

	protected void beforeGenerateContent(Document document) {
		setupTemplate(document);
	}
	
	protected void documentComplete(Document document) throws IOException {

		super.documentComplete(document);
		
		setupCSRFToken(document);
	}

	protected void setupCSRFToken(Document document) {
		Element form = document.selectFirst("form");
		if(Objects.nonNull(form)) {
			
			sessionUtils.addContentSecurityPolicy(Request.response(), "form-action", "self");
			sessionUtils.setupFormCSRFToken(Request.get(), template, form);

		}
	}

	protected abstract void doGenerateTemplateContent(Document document) throws FileNotFoundException, IOException;
	
	public abstract FieldView getScope();
	
}
