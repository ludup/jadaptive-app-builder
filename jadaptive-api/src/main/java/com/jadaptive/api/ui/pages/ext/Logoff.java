package com.jadaptive.api.ui.pages.ext;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.pages.RevertImpersonatePage;

@Component
public class Logoff extends AuthenticatedPage { 
	
	@Autowired
	private PageCache pageCache;
	
	@Override
	protected void generateAuthenticatedContent(Document doc) throws FileNotFoundException {
		
		Optional<Session> session = Session.getOr();
		if(session.isPresent() && session.get().isImpersontating()) {
			throw new PageRedirect(pageCache.resolvePage(RevertImpersonatePage.class));
		}
		
		try {
			
			Request.get().getSession().invalidate();
		} catch (AccessDeniedException | ObjectNotFoundException e) {
		}
		throw new PageRedirect(pageCache.resolveDefault());
	}

	@Override
	public String getUri() {
		return "logoff";
	}
}
