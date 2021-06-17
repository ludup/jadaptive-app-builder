package com.jadaptive.plugins.web.ui.setup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.renderers.DropdownInput;

@Extension
public class SelectMount extends SetupSection {

	@Autowired
	private TemplateService templateService; 
	
	@Override
	public String getName() {
		return "selectMount";
	}

	@Override
	public void process(Document document, Element element, Page page) throws IOException {
		
		DropdownInput input = new DropdownInput("type", "selectMount");
		
		ObjectTemplate template = templateService.get("virtualFolder");
		
		Map<String,String> values = new HashMap<>();
		for(String child : template.getChildTemplates()) {
			values.put(child, String.format("%s.name",child));
		}
		
		Element el = input.renderInput();
		input.renderValues(values, "localFolder", true);
		
		Element content = document.selectFirst("#content");
		content.appendChild(el);
		
		super.process(document, element, page);
	}

	
}
