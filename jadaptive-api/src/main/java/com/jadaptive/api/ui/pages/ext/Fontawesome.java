package com.jadaptive.api.ui.pages.ext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.utils.FileUtils;

@Component
public class Fontawesome extends AbstractPageExtension {

	final String free = "/app/content/fontawesome-free-6.4.0-web/css/all.css";
	final String pro = "/app/content/fontawesome-pro-6.4.0-web/css/all.css";
	boolean isPro = false;
	String iconSet = "fa-solid";
	
	@Autowired
	private ClassLoaderService classService; 
	
	String runtimePath = null;
	
	@Override
	public void process(Document document, Element element, Page page) {
	
		if(Objects.isNull(runtimePath)) {
			Collection<Class<?>> classes = classService.resolveAnnotatedClasses(EnableFontAwesomePro.class);
			if(classes.isEmpty()) {
				runtimePath = free;
			} else {
				Class<?> clz = classes.iterator().next();
				EnableFontAwesomePro a = clz.getAnnotation(EnableFontAwesomePro.class);
				if(StringUtils.isNotBlank(a.path())) {
					File themePath = new File(ApplicationProperties.getConfdFolder(),
							"system" + File.separator + "shared" + File.separator + 
							"webapp" + File.separator + a.path());
					
					if(themePath.exists()) {
						runtimePath = "/app/content/" + FileUtils.checkStartsWithNoSlash(FileUtils.checkEndsWithSlash(a.path())) + "css/all.css";
					} else {
						runtimePath = pro;
					}	
				} 
				isPro = true;
				iconSet = a.iconSet();		
			}
		}
		
		PageHelper.appendStylesheet(document, runtimePath);

		if(isPro) {
			page.addProcessor(new AbstractPageExtension() {
				@Override
				public void process(Document document, Element extensionElement, Page page) throws IOException {
					String appliedIconset = document.select("body").hasAttr("data-iconset") ?
							document.select("body").attr("data-iconset") : iconSet;
					document.select(".fa-solid").removeClass("fa-solid").addClass(appliedIconset);
					document.select("body").attr("data-iconset", appliedIconset);
				}

				@Override
				public String getName() {
					return "fontawesome-pro";
				}
			});
		}
	}
	
	@Override
	public String getName() {
		return "fontawesome";
	}

}