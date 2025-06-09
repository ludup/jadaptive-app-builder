package com.jadaptive.app.session;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.jadaptive.api.servlet.Request;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/app/verify" }, dispatcherTypes = { DispatcherType.REQUEST })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionStatusFilter implements Filter {

	static Logger log = LoggerFactory.getLogger(SessionStatusFilter.class);
	
	public final static boolean isPreventAccess(ServletRequest req) {
		return req != null && Boolean.TRUE.equals(req.getAttribute("preventAccess"));
	}
	
	public final static void setPreventAccess(ServletRequest req, boolean preventAccess) {
		req.setAttribute("preventAccess", preventAccess);
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Request.setUp((HttpServletRequest)request, (HttpServletResponse)response);
		
//		try {
			HttpServletRequest req = (HttpServletRequest)request;
			HttpServletResponse resp = (HttpServletResponse)response;
	
			/**
			 * LDP - This only need to be performed on one URL so am adding the URL pattern
			 * for this rather than a check inside the filter.
			 */

			/* This is the one call we do not the session last access time to be updated 
			 * with. So we check the session ourselves, and just return what is effectively
			 * a session valid / invalid status. 
			 * 
			 * We also have to avoid letting error handlers be trigger, as these will
			 * also access and update the session.
			 */
			setPreventAccess(req, true);
			
			var sesh = req.getSession(false);
			resp.setHeader("Cache-Control", "no-store");
			if(sesh == null) {
				resp.sendError(HttpServletResponse.SC_GONE);
			}
			else {
				resp.sendError(HttpServletResponse.SC_OK);
			}
			
			response.flushBuffer();
		
//		} finally {
//			Request.tearDown();
//		}
	}
}
