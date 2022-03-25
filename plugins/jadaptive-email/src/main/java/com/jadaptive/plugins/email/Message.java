package com.jadaptive.plugins.email;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = Message.RESOURCE_KEY, type = ObjectType.COLLECTION, system = true, deletable = false, creatable = false)
@TableView(defaultColumns = { "name", "group", "enabled", "archive"} )
@ObjectViews({ @ObjectViewDefinition(bundle = Message.RESOURCE_KEY, value = "locales"),
	@ObjectViewDefinition(bundle = Message.RESOURCE_KEY, value = "replyTo")})
public class Message extends NamedUUIDEntity {

	private static final long serialVersionUID = 2912430699573395419L;

	public static final String RESOURCE_KEY = "messages";
	
	@ObjectField(required = true, readOnly = true, type = FieldType.TEXT)
	String group;
	
	@ObjectField(defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean enabled = true;
	
	@ObjectView("replyTo")
	@ObjectField(type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	String replyToName;
	
	@ObjectView("replyTo")
	@ObjectField(type = FieldType.TEXT)
	@ExcludeView(values = FieldView.TABLE)
	String replyToEmail;
	
	@ObjectField(defaultValue = "true", 
		    type = FieldType.BOOL)
	boolean archive = true;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> replacementVariables;
	
	@ObjectView("locales")
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	@Validator(value = MessageContent.RESOURCE_KEY, type = ValidationType.RESOURCE_KEY)
	Collection<MessageContent> content;
	
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

	public Collection<MessageContent> getContent() {
		return content;
	}

	public void setContent(Collection<MessageContent> content) {
		this.content = content;
	}
	
	
}
