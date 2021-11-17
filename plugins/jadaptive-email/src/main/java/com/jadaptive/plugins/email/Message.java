package com.jadaptive.plugins.email;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = Message.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class Message extends NamedUUIDEntity {

	private static final long serialVersionUID = 2912430699573395419L;

	public static final String RESOURCE_KEY = "messages";
	
	@ObjectField(required = true, readOnly = true, type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	String shortName;
	
	@ObjectField(required = true, readOnly = true, type = FieldType.TEXT)
	String group;
	
	@ObjectField(required = true, type = FieldType.TEXT)
	String subject;
	
	@ObjectField(required = true, type = FieldType.TEXT_AREA)
	@ExcludeView(values = FieldView.TABLE)
	String plainText;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE)
	@ExcludeView(values = FieldView.TABLE)
	HTMLTemplate htmlTemplate;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ExcludeView(values = FieldView.TABLE)
	String html;
	
	@ObjectField(defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean enabled = true;
	
	@ObjectField(type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	String replyToName;
	
	@ObjectField(type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	String replyToEmail;
	
	@ObjectField(defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean archive = true;
	
	@ObjectField(type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> replacementVariables;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup() {
		return group;
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

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public HTMLTemplate getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(HTMLTemplate htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public String getReplyToEmail() {
		return replyToEmail;
	}

	public void setReplyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public Collection<String> getReplacementVariables() {
		return replacementVariables;
	}

	public void setReplacementVariables(Collection<String> replacementVariables) {
		this.replacementVariables = replacementVariables;
	}
}
