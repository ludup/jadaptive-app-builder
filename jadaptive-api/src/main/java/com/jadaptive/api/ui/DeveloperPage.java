package com.jadaptive.api.ui;

import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = DeveloperPage.RESOURCE_KEY)
@TableView(defaultColumns = { "uri"})
public class DeveloperPage {

	public static final String RESOURCE_KEY = "developerPages";
	public static final String HTML_VIEW = "htmlView";
	public static final String SCRIPTL_VIEW = "scriptView";
	public static final String STYLE_VIEW = "styleView";
	
	@ObjectField(type = FieldType.TEXT)
	String uri;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.HTML_EDITOR)
	String developerHtml;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.HTML_EDITOR)
	String originalHtml;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.JAVA_EDITOR)
	String developerScript;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.JAVA_EDITOR)
	String originalScript;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.CSS_EDITOR)
	String developerStyle;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	@ObjectView(value = HTML_VIEW, renderer = FieldRenderer.CSS_EDITOR)
	String originalStyle;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDeveloperHtml() {
		return developerHtml;
	}

	public void setDeveloperHtml(String developerHtml) {
		this.developerHtml = developerHtml;
	}

	public String getOriginalHtml() {
		return originalHtml;
	}

	public void setOriginalHtml(String originalHtml) {
		this.originalHtml = originalHtml;
	}

	public String getDeveloperScript() {
		return developerScript;
	}

	public void setDeveloperScript(String developerScript) {
		this.developerScript = developerScript;
	}

	public String getOriginalScript() {
		return originalScript;
	}

	public void setOriginalScript(String originalScript) {
		this.originalScript = originalScript;
	}

	public String getDeveloperStyle() {
		return developerStyle;
	}

	public void setDeveloperStyle(String developerStyle) {
		this.developerStyle = developerStyle;
	}

	public String getOriginalStyle() {
		return originalStyle;
	}

	public void setOriginalStyle(String originalStyle) {
		this.originalStyle = originalStyle;
	}
}
