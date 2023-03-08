package com.jadaptive.api.permissions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;

public class ExceptionHandlingController implements PluginController {

	Logger log = LoggerFactory.getLogger(ExceptionHandlingController.class);

	@ExceptionHandler(UnauthorizedException.class)
	public void handleException(HttpServletRequest request,
			HttpServletResponse response,
			Throwable e) throws IOException {
		
		Feedback.error("userInterface", "unauthorized.text");
		if(request.getRequestURI().startsWith("/app/ui/")) {
			response.sendRedirect("/app/ui/login");
		} else {
			response.sendError(HttpStatus.UNAUTHORIZED.value());
		}
	}
}
