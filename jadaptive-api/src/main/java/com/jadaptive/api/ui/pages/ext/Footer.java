package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.CustomizablePage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.utils.Utils;

@Component
@CustomizablePage
public class Footer extends AbstractPageExtension {
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		BootstrapTheme current = getThemeFromCookie(BootstrapTheme.DEFAULT);
		
		if(document.selectFirst("#bootstrapEnabled") != null) {
			if(BootstrapTheme.hasCss(current)) {
				PageHelper.appendStylesheet(document, String.format("/app/content/themes/%s/bootstrap.min.css", BootstrapTheme.getThemeCssName(current)), "bootstrapTheme");	
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
