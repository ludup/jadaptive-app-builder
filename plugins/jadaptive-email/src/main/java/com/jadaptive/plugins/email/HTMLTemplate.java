package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableAction.Window;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = HTMLTemplate.RESOURCE_KEY, type = ObjectType.COLLECTION, defaultColumn = "name")
@ObjectViews({ 
	@ObjectViewDefinition(value = HTMLTemplate.HTML_VIEW, bundle = HTMLTemplate.RESOURCE_KEY),
	@ObjectViewDefinition(value = HTMLTemplate.OPTIONS_VIEW, bundle = HTMLTemplate.RESOURCE_KEY, weight = 100)})
@TableView(defaultColumns = "name", actions = { 
		@TableAction(bundle = HTMLTemplate.RESOURCE_KEY, url = "/app/api/html/preview/{uuid}", icon = "fa-magnifying-glass",
				target = Target.ROW, resourceKey = "preview", window = Window.BLANK)})
public class HTMLTemplate extends NamedUUIDEntity {

	private static final long serialVersionUID = 1070995818848710214L;

	public static final String RESOURCE_KEY = "htmlTemplates";
	
	public static final String HTML_VIEW = "htmlView";
	public static final String OPTIONS_VIEW = "optionsView";
	
	@ObjectField(required = true, defaultValue = "body", type = FieldType.TEXT)
	@ObjectView(value = OPTIONS_VIEW)
	String contentSelector;
	
	@ObjectField(required = true, type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.HTML_EDITOR)
	String html;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public String getContentSelector() {
		return contentSelector;
	}

	public void setContentSelector(String contentSelector) {
		this.contentSelector = contentSelector;
	}
}
