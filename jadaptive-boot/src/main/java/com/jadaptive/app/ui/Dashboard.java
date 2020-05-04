package com.jadaptive.app.ui;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.Bootstrapify;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.codesmith.webbits.extensions.Relativize;
import com.codesmith.webbits.fontawesome.FontAwesomeify;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.auth.AuthenticationService;

@Page({ Bootstrapify.class, FontAwesomeify.class, 
	PageResources.class, 
	PageResourcesElement.class, Absolutify.class, 
	Enablement.class, I18N.class })
@View(contentType = "text/html", paths = { "/dashboard"})
@Resource
public class Dashboard extends AuthenticatedView {

	
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content) {
    	return content;
    }

	@Override
	protected void onCreated() {

	}

}
