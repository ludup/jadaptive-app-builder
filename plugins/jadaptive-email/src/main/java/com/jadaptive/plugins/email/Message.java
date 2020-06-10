package com.jadaptive.plugins.email;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(name = "Messages", resourceKey = Message.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class Message extends NamedUUIDEntity {

	public static final String RESOURCE_KEY = "messages";
	
	@ObjectField(name = "Message Group", description = "The group of messages this belongs to", 
			required = true, readOnly = true, type = FieldType.TEXT)
	String group;
	
	@ObjectField(name = "Subject", description = "The subject of this message", 
				required = true, searchable = true, type = FieldType.TEXT)
	String subject;
	
	@ObjectField(name = "Plain Text", description = "The plain text content of this message", 
			required = true, searchable = true, type = FieldType.TEXT_AREA)
	String plainText;
	
	@ObjectField(name = "HTML Template", 
			description = "The base HTML template for inserting content", 
			type = FieldType.OBJECT_REFERENCE)
	HTMLTemplate htmlTemplate;
	
	@ObjectField(name = "HTML Content", 
			description = "The HTML content of this message.", 
			type = FieldType.TEXT_AREA)
	String html;
	
	@ObjectField(name = "Enabled", 
			description = "Disabling this message prevents it from being sent", 
		    defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean enabled = true;
	
	@ObjectField(name = "Reply-To Name", 
			description = "Override the Reply-To option in this message", 
			type = FieldType.TEXT)
	String replyToName;
	
	@ObjectField(name = "Reply-To Email", 
			description = "Override the Reply-To option in this message", 
			type = FieldType.TEXT)
	String replyToEmail;
	
	@ObjectField(name = "Archive", 
			description = "Send a copy of this email to the archive address", 
		    defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean archive = true;
	
	@ObjectField(name = "Replacement Variables", 
			description = "You may use these variables in your content",
			type = FieldType.TEXT)
	Collection<String> replacementVariables;
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
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