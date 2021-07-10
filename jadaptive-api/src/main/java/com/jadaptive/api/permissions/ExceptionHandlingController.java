package com.jadaptive.api.permissions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.ui.Redirect;

public class ExceptionHandlingController implements PluginController {

	Logger log = LoggerFactory.getLogger(ExceptionHandlingController.class);
	
//	@ExceptionHandler(Throwable.class)
//	public void handleException(HttpServletRequest request, 
//			HttpServletResponse response,
//			Throwable e) throws Throwable {
//		
//		if(e instanceof Redirect) {
//			throw e;
//		}
//		
//		log.error("{} generated error", request.getRequestURI(), e);
//		throw e;
//	}
}
