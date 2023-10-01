package com.jadaptive.api.ui.pages.ext;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.utils.FileUtils;

@Component
public class Bootstrap extends AbstractPageExtension {

	static Logger log = LoggerFactory.getLogger(Bootstrap.class);
	
	@Autowired
	private ClassLoaderService classService; 
	
	private String runtimePathJs = null;
	private String runtimePathCss = null;
	
	@Override
	public void process(Document document, Element element, Page page) {
	
		if(Objects.isNull(runtimePathJs)) {
			
			runtimePathJs = "/app/content/bootstrap-5.2.2-dist/js/bootstrap.bundle.min.js";
			runtimePathCss = "/app/content/bootstrap-5.2.2-dist/css/bootstrap.min.css";
			
			Collection<Class<?>> classes = classService.resolveAnnotatedClasses(EnableBootstrapTheme.class);
			if(!classes.isEmpty()) {
				Class<?> clz = classes.iterator().next();
				EnableBootstrapTheme a = clz.getAnnotation(EnableBootstrapTheme.class);
				if(StringUtils.isNotBlank(a.path())) {
					
					File themePath = new File(ApplicationProperties.getConfdFolder(),
							"system" + File.separator + "shared" + File.separator + 
							"webapp" + a.path());
					
					if(themePath.exists()) {
						runtimePathJs = "/app/content/" + FileUtils.checkStartsWithNoSlash(FileUtils.checkEndsWithSlash(a.path())) + "js/bootstrap.bundle.min.js";
						runtimePathCss = "/app/content/" + FileUtils.checkStartsWithNoSlash(FileUtils.checkEndsWithSlash(a.path())) + "css/bootstrap.min.css"; 
					}
				}
			}
		}
		
		PageHelper.appendHeadScript(document, runtimePathJs);
		PageHelper.appendHeadScript(document, "/app/content/bootbox/bootbox.all.min.js");
		PageHelper.appendStylesheet(document, runtimePathCss, "bootstrapCss");
		PageHelper.appendStylesheet(document, runtimePathCss, "printBootstrap", "print");

	}

	@Override
	public String getName() {
		return "bootstrap";
	}

}
