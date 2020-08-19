package com.jadaptive.app.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Created;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bootstrap.Bootstrapify;
import com.codesmith.webbits.extensions.Absolutify;
import com.codesmith.webbits.extensions.Enablement;
import com.codesmith.webbits.extensions.I18N;
import com.codesmith.webbits.extensions.PageResources;
import com.codesmith.webbits.extensions.PageResourcesElement;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionUtils;

@Page({ Bootstrapify.class, 
	PageResources.class, 
	PageResourcesElement.class, Absolutify.class, 
	Enablement.class, I18N.class })
@View(contentType = "text/html", paths = { "logoff"})
public class Logoff {

	@Autowired
	private SessionService sessionService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Created
	void created() throws FileNotFoundException {
		if(!sessionUtils.hasActiveSession(Request.get())) {
			throw new Redirect(Login.class);
		}
	}
	
    @Out(methods = HTTPMethod.GET)
    Document service(@In Document content) {
	
    	try {
	    	sessionService.closeSession(sessionUtils.getActiveSession(Request.get()));
    	} catch(AccessDeniedException | ObjectNotFoundException e) {
    	}
    	throw new Redirect(Login.class);
    }
}
