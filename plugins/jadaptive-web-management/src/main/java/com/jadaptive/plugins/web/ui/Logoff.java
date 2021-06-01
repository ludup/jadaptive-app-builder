package com.jadaptive.plugins.web.ui;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;

@Extension
public class Logoff extends AuthenticatedPage {

	@Autowired
	private SessionService sessionService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	protected void generateAuthenticatedContent(Document doc) throws FileNotFoundException {

		try {
			sessionService.closeSession(sessionUtils.getActiveSession(Request.get()));
		} catch (AccessDeniedException | ObjectNotFoundException e) {
		}
		throw new PageRedirect(pageCache.resolvePage(Login.class));
	}

	@Override
	public String getUri() {
		return "logoff";
	}
}
