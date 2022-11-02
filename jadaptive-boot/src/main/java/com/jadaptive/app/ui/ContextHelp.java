package com.jadaptive.app.ui;

import java.io.IOException;

import org.apache.commons.lang.WordUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;

@Extension
public class ContextHelp extends AbstractPageExtension {
	
	@Autowired
	private TemplateService templateService;
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		for(Element e : document.getElementsByAttribute("jad:help")) {
			String id = e.attr("jad:help");
			String bundle = e.attr("jad:bundle");
			String ctx = e.attr("jad:context");
			e.removeAttr("jad:help");
			e.removeAttr("jad:bundle");
			
			e.attr("data-bs-toggle", "offcanvas");
			e.attr("href", "#" + id);
			e.attr("role", "button");
			e.attr("aria-controls", id);
			
			Element main = document.selectFirst("main");
			Element body = null;
			main.appendChild(Html.div("offcanvas", "offcanvas-end")
					.attr("tabindex", "-1")
					.attr("id", id)
					.attr("aria-labelledby", id + "Label")
						.appendChild(Html.div("offcanvas-header")
								.appendChild(new Element("h5")
										.addClass("offcanvas-title")
										.attr("id", id + "Label")
										.appendChild(Html.i("far fa-books me-1"))
										.appendChild(Html.i18n(bundle, id + ".names")))
								.appendChild(new Element("button")
										.addClass("btn-close text-reset")
										.attr("data-bs-dismiss", "offcanvas")
										.attr("aria-label", "Close")))
						.appendChild(Html.div("offcanvas-body", "small").appendChild(body = Html.div())
						.appendChild(Html.div().appendChild(Html.i18n("vendor", id + ".help").attr("jad:optional", true)))));

			
			Class<?> clz;
			if("resource".equals(ctx)) {
				clz = templateService.getTemplateClass(id);
			} else {
				clz = page.getClass();
			}
			try {
				page.injectHtmlSection(document, body, clz, WordUtils.capitalize(id) + "Help.html", true);
			} catch(IOException ex) {
				e.remove();
			}
		}
	}

	@Override
	public String getName() {
		return "help";
	}
	
	

}
