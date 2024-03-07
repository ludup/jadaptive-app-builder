package com.jadaptive.utils;

import org.jsoup.nodes.Document;

import com.jadaptive.api.ui.PageHelper;

public class Npm {

	public final static void scripts(Document document, String pkg, String... paths) {
		if(pkg.startsWith("@")) {
			scriptsScope(document, pkg.substring(1, pkg.indexOf('/')), pkg.substring(pkg.indexOf('/') + 1), paths);
		}
		else {
			scriptsScope(document, null, pkg, paths);
		}
	}
	
	public final static void scriptsScope(Document document, String scope, String pkg, String... paths) {
		for(var path : paths) {
			PageHelper.appendHeadScript(document, 
					String.format("/app/content/npm2mvn/%s/%s/current/%s", 
					scope == null ? "npm" : "npm." + scope, pkg, 
					path));
		}
	}
	
	public final static void modules(Document document, String pkg, String... paths) {
		if(pkg.startsWith("@")) {
			modulesScope(document, pkg.substring(1, pkg.indexOf('/')), pkg.substring(pkg.indexOf('/') + 1), paths);
		}
		else {
			modulesScope(document, null, pkg, paths);
		}
	}
	
	public final static void modulesScope(Document document, String scope, String pkg, String... paths) {
		for(var path : paths) { 
			PageHelper.appendHeadModule(document, 
					String.format("/app/content/npm2mvn/%s/%s/current/%s", 
					scope == null ? "npm" : "npm." + scope, pkg, 
					path));
		}
	}
	
	public final static void stylesheets(Document document, String pkg, String... paths) {
		if(pkg.startsWith("@")) {
			stylesheetsScope(document, pkg.substring(1, pkg.indexOf('/')), pkg.substring(pkg.indexOf('/') + 1), paths);
		}
		else {
			stylesheetsScope(document, null, pkg, paths);
		}
	}
	
	public final static void stylesheetsScope(Document document, String scope, String pkg, String... paths) {
		for(var path : paths) {
			PageHelper.appendStylesheet(document, 
					String.format("/app/content/npm2mvn/%s/%s/current/%s", 
					scope == null ? "npm" : "npm." + scope, pkg, 
					path));
		}
	}
}
