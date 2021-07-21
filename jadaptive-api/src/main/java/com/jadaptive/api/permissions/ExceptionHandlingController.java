package com.jadaptive.api.permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.servlet.PluginController;

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
