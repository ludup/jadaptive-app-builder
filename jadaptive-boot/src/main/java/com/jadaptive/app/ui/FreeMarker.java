package com.jadaptive.app.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Extension
public class FreeMarker extends AbstractPageExtension{

	static Logger log = LoggerFactory.getLogger(FreeMarker.class);
	
	@Override
	public String getName() {
		return "freemarker";
	}
	
	@Override
	public void process(Document document, Page page) throws IOException {
		
		String name = UUID.randomUUID().toString();
		Template templateObj = createTemplate(name, StringUtils.defaultIfBlank(document.toString(), ""),
				System.currentTimeMillis());
		try {
			StringWriter swt = new StringWriter();
			Map<String,Object> objects = new HashMap<>();
			objects.put("page", page);
			
			templateObj.process(objects, swt);
			
			Element head = document.selectFirst("html");
			head.empty();
			Elements elements = Jsoup.parse(swt.toString()).selectFirst("html").children();
			for(Element e : elements) {
				e.appendTo(head);
			}
			
		} catch (TemplateException e) {
			log.error("Template error", e);
		} finally {
			stringLoader.removeTemplate(name);
		}

	}
	private StringTemplateLoader stringLoader = new StringTemplateLoader();

	protected Template createTemplate(String name, String templateSource, long lastModified) throws IOException {
		
		stringLoader.putTemplate(name, templateSource, lastModified);

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		cfg.setTemplateLoader(stringLoader);
		return cfg.getTemplate(name);
	}

}
