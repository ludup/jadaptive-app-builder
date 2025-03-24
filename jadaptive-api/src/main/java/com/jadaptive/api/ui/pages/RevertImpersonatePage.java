package com.jadaptive.api.ui.pages;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.UriRedirect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RevertImpersonatePage extends AuthenticatedPage {
	
	@Autowired
	private SessionService sessionService; 
	
	@Override
	public String getUri() {
		return "revert-impersonation";
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		
		try {
			Session session = Session.get(request);
			if(session.isImpersontating()) {
				sessionService.unimpersonate(session);
				Feedback.success("userInterface", "impersonate-end.success", session.getTenant().getName());
			}

		} catch(Throwable e) {
			Feedback.error(e.getMessage());
		}
		
		throw new UriRedirect("/app/ui/search/tenant");
	}

}
