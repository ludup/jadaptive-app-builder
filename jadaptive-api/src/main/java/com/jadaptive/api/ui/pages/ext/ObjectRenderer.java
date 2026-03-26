package com.jadaptive.api.ui.pages.ext;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateCapability;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.ObjectPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.pages.TemplatePage;

@Component
public class ObjectRenderer extends AbstractObjectRenderer {

	@Autowired
	private TemplateService templateService; 
	
	ThreadLocal<String> actionURL = new ThreadLocal<>();
	ThreadLocal<AbstractObject> object = new ThreadLocal<>();
	
	@Override
	public String getName() {
		return "objectRenderer";
	}
	
	public AbstractObject getCurrentObject() {
		return object.get();
	}

	@Override
	public void process(Document contents, Element element, Page page) throws IOException {

		 try {
			ObjectTemplate template = null;
			FieldView scope = FieldView.CREATE;
			String handler;
			
			
			if(page instanceof TemplatePage) {
				TemplatePage templatePage = (TemplatePage) page;
				template = templatePage.getTemplate();
				scope = templatePage.getScope();
				
				if(element.hasAttr("jad:handler")) {
					handler = element.attr("jad:handler");
				}
				else {
					handler = "default";
				}
			} else {
				String resourceKey = element.attr("jad:resourceKey");
	
				if(element.hasAttr("jad:handler")) {
					handler = element.attr("jad:handler");
				}
				else {
					handler = "default";
				}
				
				template = templateService.get(resourceKey);
				
				if(element.hasAttr("jad:scope")) {
					scope = FieldView.valueOf(element.attr("jad:scope"));
				}
			}
			
			if(element.hasAttr("jad:renderer")) {
				formRenderer.set(RenderScope.valueOf(element.attr("jad:renderer")));
			}
		
			var forceNew = element.attr("jad:forceNewObject");
			
			if(page instanceof ObjectPage && !Boolean.parseBoolean(forceNew)) {
				object.set(((ObjectPage)page).getObject());
			} 
			
			if(element.hasAttr("jad:disableViews")) {
				disableViews.set(Boolean.valueOf(element.attr("jad:disableViews")));
			} 
			
			if(element.hasAttr("jad:ignores")) {
				ignoreResources.set(new HashSet<>(Arrays.asList(element.attr("jad:ignores").split(","))));
			}
			
			AbstractObject displayObject = object.get();
			ObjectTemplate displayTemplate = template;
			var disableOverride = element.attr("jad:disableOverride");
			if(!Boolean.parseBoolean(disableOverride)) {
				if(Objects.nonNull(displayObject)) {
					if(!displayObject.getResourceKey().equals(displayTemplate.getResourceKey())) {
						displayTemplate = templateService.get(displayObject.getResourceKey());
					}
				}
			}
			
			/** Are there any file fields in the template? If so, switch to the JSON handler by 
			 * default. This can be overridden by {@link ObjectTemplateCapability#JSON_OBJECT_POST} or 
			 * {@link ObjectTemplateCapability#BASIC_OBJECT_POST} */
			
			var containsFiles = displayTemplate.getFields().stream()
				.filter(f -> f.getFieldType() == FieldType.ATTACHMENT || f.getFieldType() == FieldType.IMAGE)
				.findAny().isPresent();
			
			if(displayTemplate.getCapabilities().contains(ObjectTemplateCapability.JSON_OBJECT_POST) ||
			 ( containsFiles && !displayTemplate.getCapabilities().contains(ObjectTemplateCapability.BASIC_OBJECT_POST))) {	
				handlerPrefix.set("json-");
				formHandler.set("json-" + handler);
			} else  {
				handlerPrefix.set("");
				formHandler.set(handler);	
			}

			actionURL.set(String.format("/app/api/form/%s/%s", formHandler.get(), displayTemplate.getResourceKey()));
			
			super.process(contents, page, displayTemplate, displayObject, scope);
			
			
			/* Field validation error decoration */
			
			try {
				var errIdx = 0;
				for(var ve : Feedback.getValidationErrors()) {
					var el = contents.getElementsByAttributeValue("name", ve.getFormVariable()).first();
					if(el != null) {
						
						/* This code mirrors Javascript based field highlighting found in jadaptive-utils.js */
						
						var fieldParent = el.parents().select(".field").first();
						if(fieldParent == null) {
							continue;
						}
						var field = fieldParent.select(".form-control").first();
						if(field != null) {
							field.addClass("validation").addClass("border").addClass("border-5").addClass("border-danger");
						} else {
							field = el.select("textarea").first();
							if(field != null) {
								field.siblingElements().select(".tox-tinymce").first().addClass("validation").addClass("border").addClass("border-5").addClass("border-danger");
							}
						}
						
						fieldParent.select(".feedback-message").remove();
						var fieldLabel = fieldParent.select(".form-label").first();
						if(fieldLabel != null) {
							fieldLabel.after(Html.div("feedback-message", "text-danger").text(ve.getError()));
						} else {
							fieldParent.prependChild(Html.div("feedback-message", "text-danger").text(ve.getError()));
						}
						
						if(errIdx++ == 0) {
							/* Clear existing active tabs */
							contents.select("a.nav-link")
									.forEach(elx -> elx.removeClass("active").attr("aria-selected", "false"));
							contents.select(".tab-pane").forEach(elx -> elx.removeClass("active").removeClass("show"));
							
							/* Select the tab for the first error */
					        var parentId = field.parents().select(".tab-pane").first().attr("id");
							var triggerEl = contents.select("a[href=\"#" + parentId + "\"]").first();
							if (triggerEl != null) {
								triggerEl.addClass("active").attr("aria-selected", "true");
								
								contents.getElementById(parentId).addClass("active").addClass("show");
							}
							
							
//							fieldParent.attr("tabindex", "-1").attr("autofocus", "autofocus");
						}
						
					}
				}
			}
			finally {
				Feedback.clearValidation();
			}
		
		 } finally {
			actionURL.remove();
			object.remove();
			disableViews.remove();
			replacementVariables.remove();
		 }
	 }

	@Override
	protected String getActionURL() {
		return actionURL.get();
	}

	public void setParameters(List<String> params) {
		replacementVariables.set(params);
	}

}
