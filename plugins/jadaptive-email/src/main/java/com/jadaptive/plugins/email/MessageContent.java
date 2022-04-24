package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = MessageContent.RESOURCE_KEY, type = ObjectType.OBJECT, defaultColumn = "locale")
@ObjectViews({ 
	@ObjectViewDefinition(value = HTMLTemplate.HTML_VIEW, bundle = MessageContent.RESOURCE_KEY)})
@TableView(defaultColumns = { "locale", "enabled", "subject"} )
public class MessageContent extends AbstractUUIDEntity {

	private static final long serialVersionUID = 6197804501850188779L;

	public static final String HTML_VIEW = "htmlView";
	
	public static final String RESOURCE_KEY = "messageContent";
	
	@ObjectField(defaultValue = "", type = FieldType.TEXT, readOnly = true)
	String locale;
	
	@ObjectField(type = FieldType.TEXT)
	String subject;
	
	@ObjectField(defaultValue = "false", type = FieldType.BOOL)
	boolean enabled = true;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = HTMLTemplate.RESOURCE_KEY)
	@Validator(value = HTMLTemplate.RESOURCE_KEY, type = ValidationType.RESOURCE_KEY)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.HTML_EDITOR)
	HTMLTemplate htmlTemplate;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.HTML_EDITOR)
	String htmlText;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}


	public String getLocale() {
		return locale;
	}


	public void setLocale(String locale) {
		this.locale = locale;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}

	public HTMLTemplate getHtmlTemplate() {
		return htmlTemplate;
	}


	public void setHtmlTemplate(HTMLTemplate htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}


	public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}


	public boolean isEnabled() {
		return enabled;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	

}
