package com.jadaptive.api.session;

import org.pf4j.ExtensionPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface PluginInterceptor extends ExtensionPoint {

	boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;

	void postHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
