package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.CustomizablePage;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;

@Component
@CustomizablePage
public class Footer extends AbstractPageExtension {
	
	private static final Logger log = LoggerFactory.getLogger(Footer.class);
	
	@Autowired
	private BootstrapThemeService themeResolver;
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {

		Element bootstrap = HtmlPage.getCurrentDocument().selectFirst("#bootstrapCss");
		if(log.isDebugEnabled()) {
			log.debug("Bootstrap css is {}", bootstrap);
		}
		if(Objects.nonNull(bootstrap) && page.isThemePage()) {
			var current = themeResolver.getTheme();
			if(log.isDebugEnabled()) {
				log.debug("Bootstrap theme is {}", current);
			}
			if(current.hasCss()) {
				if(log.isDebugEnabled()) {
					log.debug("Applying theme {}", current);
				}
				PageHelper.appendStylesheet(document, current.getThemeCssUrl(), "bootstrapTheme");	
			}

			Element footer = document.selectFirst("#footer");
			boolean allowChange = true;
			if(allowChange) {
				
				IconWithDropdownInput input = new IconWithDropdownInput("theme", current.name().toLowerCase());
				input.up().dark();
				footer.appendChild(new Element("div")
						.appendChild(new Element("div")
							.addClass("ms-3")
							.appendChild(input.renderInput())));
				themeResolver.getAllThemes().forEach(set -> {
					input.renderValues(set, current.name(), false, true);
				});
			}
		}
	}

	@Override
	public String getName() {
		return "footer";
	}

}
