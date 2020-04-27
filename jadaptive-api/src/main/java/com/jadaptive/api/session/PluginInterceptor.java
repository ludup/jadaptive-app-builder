package com.jadaptive.api.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.ExtensionPoint;

public interface PluginInterceptor extends ExtensionPoint {

	boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;

	void postHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
