package com.jadaptive.api.ui.pages.ext;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;

@Component
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
			HttpServletRequest req = Request.get();
			try {
				sessionService.closeSession(sessionUtils.getActiveSession(req));
			}
			finally {
				req.getSession().invalidate();
			}
		} catch (AccessDeniedException | ObjectNotFoundException e) {
		}
		throw new PageRedirect(pageCache.resolveDefault());
	}

	@Override
	public String getUri() {
		return "logoff";
	}
}
