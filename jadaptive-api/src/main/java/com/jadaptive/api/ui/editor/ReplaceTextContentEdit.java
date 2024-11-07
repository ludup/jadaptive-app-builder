package com.jadaptive.api.ui.editor;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.UniqueIndex;

@ObjectDefinition(resourceKey = ReplaceTextContentEdit.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION, defaultColumn = "name")
@GenerateEventTemplates(ReplaceTextContentEdit.RESOURCE_KEY)
@UniqueIndex(columns = { "locale", "bundle", "key" })
public class ReplaceTextContentEdit extends ContentEdit {

	private static final long serialVersionUID = -3753074297630922180L;

	public static final String RESOURCE_KEY = "replaceText";
	
	@ObjectField(type = FieldType.TEXT)
	String locale;
	
	@ObjectField(type = FieldType.TEXT)
	String bundle;
	
	@ObjectField(type = FieldType.TEXT)
	String key;
	
	@ObjectField(type = FieldType.TEXT)
	String replacementText;
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public String getBundle() {
		return bundle;
	}

	public void setBundle(String bundle) {
		this.bundle = bundle;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getReplacementText() {
		return replacementText;
	}

	public void setReplacementText(String replacementText) {
		this.replacementText = replacementText;
	}
}
