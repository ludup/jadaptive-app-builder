package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.jadaptive.api.ui.Html;

public abstract class TemplatePage extends AuthenticatedPage {

	@Autowired
	protected TemplateService templateService;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	protected String resourceKey;
	
	protected ObjectTemplate template;
	protected Class<?> templateClazz;

	public ObjectTemplate getTemplate() {
		return template;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void onCreate() throws FileNotFoundException {
		
		
		try {
			template = templateService.get(resourceKey);
			templateClazz = templateService.getTemplateClass(resourceKey);
			
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
			sessionUtils.verifySameSiteRequest(request);
		} catch (UnauthorizedException e) {
			throw new IllegalStateException(e.getMessage(), e);
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

	}
	
	protected void documentComplete(Document document) {

//		Element form = document.selectFirst("form");
//		if(Objects.nonNull(form)) {
//			form.appendChild(Html.input("hidden", 
//					SessionUtils.CSRF_TOKEN_ATTRIBUTE, 
//						sessionUtils.setupCSRFToken(Request.get()))
//						.attr("id", "csrftoken"));
//		}
	}

	protected abstract void doGenerateTemplateContent(Document document) throws FileNotFoundException, IOException;
	
	public abstract FieldView getScope();
	
}
