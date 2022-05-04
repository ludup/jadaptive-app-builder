package com.jadaptive.plugins.web.ui;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.IconWithDropdownInput;
import com.jadaptive.utils.Utils;

@Extension
public class Footer extends AbstractPageExtension {
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		BootstrapTheme current = getThemeFromCookie(BootstrapTheme.DEFAULT);
		if(current!=BootstrapTheme.DEFAULT) {
			PageHelper.appendStylesheet(document, String.format("/app/content/themes/%s/bootstrap.min.css", current.name().toLowerCase()), "bootstrapTheme");	
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
		
		boolean loggedIn = sessionUtils.hasActiveSession(Request.get());
		if(loggedIn) {
			footer.appendChild(new Element("div")
					.addClass("float-end me-3")
					.appendChild(new Element("div")
							.attr("id", "logoff")
							.appendChild(new Element("a")
								.attr("style", "text-decoration: none;")
								.attr("href", "/app/ui/logoff")
								.addClass("text-light fas fa-sign-out-alt"))));

		}
	}

	private BootstrapTheme getThemeFromCookie(BootstrapTheme defaultValue) {
		
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
