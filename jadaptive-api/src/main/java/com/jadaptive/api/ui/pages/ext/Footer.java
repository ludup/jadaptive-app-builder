package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.CustomizablePage;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.utils.Utils;

@Component
@CustomizablePage
public class Footer extends AbstractPageExtension {
	
	private static final Logger log = LoggerFactory.getLogger(Footer.class);
	
	@Autowired
	private BootstrapThemeResolver themeResolver;
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {

		Element bootstrap = HtmlPage.getCurrentDocument().selectFirst("#bootstrapCss");
		if(log.isDebugEnabled()) {
			log.debug("Bootstrap css is {}", bootstrap);
		}
		if(Objects.nonNull(bootstrap)) {
			BootstrapTheme current = themeResolver.getTheme();
			if(log.isDebugEnabled()) {
				log.debug("Bootstrap theme is {}", current);
			}
			if(BootstrapTheme.hasCss(current)) {
				if(log.isDebugEnabled()) {
					log.debug("Applying theme {}", current);
				}
				PageHelper.appendStylesheet(document, BootstrapTheme.getThemeCssUrl(current), "bootstrapTheme");	
			}

			Element footer = document.selectFirst("#footer");
			boolean allowChange = true;
			if(allowChange) {
				
				IconWithDropdownInput input = new IconWithDropdownInput("theme", current.name().toLowerCase());
				input.up().dark();
				footer.appendChild(new Element("div")
						.addClass("float-end me-3")
						.appendChild(new Element("div").addClass("col-3 ms-3").appendChild(input.renderInput())));
				input.renderValues(BootstrapTheme.values(), current.name(), false, true);
			}
		}
	}

	public static BootstrapTheme getThemeFromCookie(BootstrapTheme defaultValue) {
		
		Cookie[] cookies = Request.get().getCookies();
		if(Objects.nonNull(cookies)) {
			for(Cookie c : cookies) {
				if("userTheme".equals(c.getName())) {
					if(NumberUtils.isNumber(c.getValue())) {
						return BootstrapTheme.values()[Utils.parseIntOrDefault(c.getValue(), 0)];
					} else {
						return BootstrapTheme.valueOf(c.getValue());
					}
				}
			}
		}
		return defaultValue;
	}

	@Override
	public String getName() {
		return "footer";
	}

}
