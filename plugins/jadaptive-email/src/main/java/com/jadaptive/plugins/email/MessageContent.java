package com.jadaptive.plugins.email;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = MessageContent.RESOURCE_KEY, type = ObjectType.OBJECT)
@TableView(defaultColumns = { "locale", "enabled", "subject"} )
public class MessageContent extends AbstractUUIDEntity {

	private static final long serialVersionUID = 6197804501850188779L;

	public static final String RESOURCE_KEY = "messageContent";
	
	@ObjectField(defaultValue = "", type = FieldType.TEXT)
	String locale;
	
	@ObjectField(type = FieldType.TEXT)
	String subject;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String plainText;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE)
	@Validator(value = HTMLTemplate.RESOURCE_KEY, type = ValidationType.RESOURCE_KEY)
	HTMLTemplate htmlTemplate;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String htmlText;
	
	@ObjectField(defaultValue = "false", type = FieldType.BOOL)
	boolean enabled = true;

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


	public String getPlainText() {
		return plainText;
	}


	public void setPlainText(String plainText) {
		this.plainText = plainText;
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
