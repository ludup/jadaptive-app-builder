package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OrderedView {
		
	ObjectViewDefinition def = null;
	String defaultBundle = null;
	List<OrderedField> fields = new ArrayList<>();
	List<OrderedView> childViews = new ArrayList<>();

	
	public OrderedView(ObjectViewDefinition def) {
		this.def = def;
	}
	
	public OrderedView(String defaultBundle) {
		this.defaultBundle = defaultBundle;
	}
	
	public boolean isRoot() {
		return def == null;
	}
	
	public ViewType getType() {
		return def.type();
	}
	
	public void addField(OrderedField field) {
		fields.add(field);
	}
	
	public List<OrderedField> getFields() {
		Collections.sort(fields, new Comparator<OrderedField>() {
			@Override
			public int compare(OrderedField o1, OrderedField o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});
		return fields;
	}
	
	public void addChildView(OrderedView child) {
		childViews.add(child);
	}
	
	public List<OrderedView> getChildViews() {
		Collections.sort(childViews, new Comparator<OrderedView>() {

			@Override
			public int compare(OrderedView o1, OrderedView o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
			
		});
		return childViews;
	}

	public Integer getWeight() {
		return Integer.valueOf(def==null ? Integer.MIN_VALUE : def.weight());
	}

	public String getBundle() {
		return Objects.nonNull(def) ? def.bundle() : defaultBundle;
	}
	
	public String getResourceKey() {
		return def.value();
	}

	public String getParent() {
		return def.parent();
	}
}