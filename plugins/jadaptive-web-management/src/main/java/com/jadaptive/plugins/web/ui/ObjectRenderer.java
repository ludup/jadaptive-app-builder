package com.jadaptive.plugins.web.ui;

import java.io.IOException;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.Page;

@Extension
public class ObjectRenderer extends AbstractObjectRenderer {

	@Autowired
	private TemplateService templateService; 
	
	ThreadLocal<String> actionURL = new ThreadLocal<>();
	ThreadLocal<AbstractObject> object = new ThreadLocal<>();
	
	@Override
	public String getName() {
		return "objectRenderer";
	}

	 @Override
	public void process(Document contents, Element element, Page page) throws IOException {

		 try {
			ObjectTemplate template = null;
			FieldView scope = FieldView.CREATE;
			String handler = "default";
			
			
			if(page instanceof TemplatePage) {
				TemplatePage templatePage = (TemplatePage) page;
				template = templatePage.getTemplate();
				scope = templatePage.getScope();
			} else {
				String resourceKey = element.attr("jad:resourceKey");
	
				if(element.hasAttr("jad:handler")) {
					handler = element.attr("jad:handler");
				}
				
				template = templateService.get(resourceKey);
				
				if(element.hasAttr("jad:scope")) {
					scope = FieldView.valueOf(element.attr("jad:scope"));
				}
			}
			
			if(page instanceof ObjectPage) {
				object.set(((ObjectPage)page).getObject());
			}
			
			if(element.hasAttr("jad:disableViews")) {
				disableViews.set(Boolean.valueOf(element.attr("jad:disableViews")));
			} 
	
			actionURL.set(String.format("/app/api/form/%s/%s", handler, template.getResourceKey()));
			
			AbstractObject displayObject = object.get();
			ObjectTemplate displayTemplate = template;
			if(Objects.nonNull(displayObject)) {
				if(!displayObject.getResourceKey().equals(displayTemplate.getResourceKey())) {
					displayTemplate = templateService.get(displayObject.getResourceKey());
				}
			}
			
			super.process(contents, page, displayTemplate, displayObject, scope);
		
		 } finally {
			actionURL.remove();
			object.remove();
			disableViews.remove();
		 }
	 }

	@Override
	protected String getActionURL() {
		return actionURL.get();
	}

}
