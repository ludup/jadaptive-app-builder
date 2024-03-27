package com.jadaptive.app.session;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@WebFilter(urlPatterns = { "/*" }, dispatcherTypes = { DispatcherType.REQUEST })
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
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;

		if(req.getRequestURI().equals("/app/verify")) {
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
			return;
		}
		
		chain.doFilter(req, resp);
	}
}