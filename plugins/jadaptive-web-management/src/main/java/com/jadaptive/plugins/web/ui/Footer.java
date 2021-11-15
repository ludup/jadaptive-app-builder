package com.jadaptive.plugins.web.ui;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageHelper;
import com.jadaptive.api.ui.renderers.DropdownInput;

@Extension
public class Footer extends AbstractPageExtension {
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		String theme = null;
		if(StringUtils.isNotBlank(theme) && !BootstrapTheme.DEFAULT.name().equalsIgnoreCase(theme)) {
			PageHelper.appendStylesheet(document, String.format("/app/content/themes/%s/bootstrap.min.css", theme));
		}
		
		Element footer = document.selectFirst("#footer");
		boolean allowChange = true;
		if(allowChange) {
			DropdownInput input = new DropdownInput("theme", "default");
			input.up().dark();
			footer.appendChild(new Element("div").addClass("col-3 ms-3").appendChild(input.renderInput()));
			input.renderValues(BootstrapTheme.values(), "DEFAULT", false);
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

	@Override
	public String getName() {
		return "footer";
	}

}
