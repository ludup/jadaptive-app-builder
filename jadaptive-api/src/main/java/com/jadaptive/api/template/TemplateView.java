package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateView {
		
	String resourceKey;
	String parent;
	ViewType type;
	int weight;
	boolean rootView;
	boolean isExtension;
	String extension;
	String bundle;
	Map<String,TemplateViewField> fields = new HashMap<>();
	List<TemplateView> childViews = new ArrayList<>();

	public TemplateView(String bundle) {
		this.rootView = true;
		this.bundle = bundle;
		this.weight = Integer.MIN_VALUE;
		this.type = ViewType.TAB;
		this.resourceKey = "root";
		this.parent = null;
		this.isExtension = false;
	}
	
	public TemplateView(String bundle, String resourceKey) {
		this.rootView = false;
		this.bundle = bundle;
		this.weight = 0;
		this.type = ViewType.TAB;
		this.resourceKey = resourceKey;
		this.parent = null;
		this.isExtension = false;
	}
	
	public TemplateView(ObjectViewDefinition def, boolean isExtension, String extension, String bundle) {
		this.rootView = false;
		this.resourceKey = def.value();
		this.parent = def.parent();
		this.bundle = bundle;
		this.weight = def.weight();
		this.type = def.type();
		this.isExtension = isExtension;
		this.extension = extension;
	}
	
	public boolean isRoot() {
		return rootView;
	}
	
	public boolean isExtension() {
		return isExtension;
	}
	
	public ViewType getType() {
		return type;
	}
	
	public void addField(TemplateViewField field) {
		fields.put(field.getResourceKey(), field);
	}
	
	public List<TemplateViewField> getFields() {
		List<TemplateViewField> tmp = new ArrayList<>(fields.values());
		Collections.sort(tmp, new Comparator<TemplateViewField>() {
			@Override
			public int compare(TemplateViewField o1, TemplateViewField o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
		return tmp;
	}
	
	public void addChildView(TemplateView child) {
		childViews.add(child);
	}
	
	public List<TemplateView> getChildViews() {
		Collections.sort(childViews, new Comparator<TemplateView>() {

			@Override
			public int compare(TemplateView o1, TemplateView o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
			
		});
		return childViews;
	}

	public Integer getWeight() {
		return weight;
	}

	public String getBundle() {
		return bundle;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public String getParent() {
		return parent;
	}

	public String getExtension() {
		return extension;
	}

	public TemplateViewField getField(FieldTemplate field) {
		return fields.get(field.getResourceKey());
	}
}