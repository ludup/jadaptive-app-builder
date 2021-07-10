package com.jadaptive.api.permissions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jadaptive.api.servlet.PluginController;

public class ExceptionHandlingController implements PluginController {

	Logger log = LoggerFactory.getLogger(ExceptionHandlingController.class);
	
	@ExceptionHandler(Throwable.class)
	public void handleException(HttpServletRequest request, 
			HttpServletResponse response,
			Throwable e) throws IOException {
		log.error("{} generated error", request.getRequestURI(), e);
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}
