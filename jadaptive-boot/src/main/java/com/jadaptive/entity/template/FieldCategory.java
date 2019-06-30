package com.jadaptive.entity.template;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.jadaptive.repository.AbstractUUIDEntity;

public class FieldCategory extends AbstractUUIDEntity {

	String resourceKey;
	Integer weight;
	Set<FieldTemplate> templates  = new HashSet<>(); 

	public String getResourceKey() {
		return StringUtils.defaultString(resourceKey);
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Set<FieldTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Set<FieldTemplate> templates) {
		this.templates = templates;
	}
	
	@Override
	public int hashCode() {
		return  new HashCodeBuilder(7, 43)
				.append(getUuid())
				.append(resourceKey).build();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FieldCategory) {
			FieldCategory category = (FieldCategory) obj;
		return new EqualsBuilder().append(getUuid(), category.getUuid())
				.append(resourceKey, category.getResourceKey()).build();
		}
		return false;
	}

	public void toMap(Map<String, Map<String,String>> properties) throws ParseException {
		
		Map<String,String> m = new HashMap<>();
		
		m.put("resourceKey", resourceKey);
		m.put("weight", String.valueOf(weight));
		StringBuffer index = new StringBuffer();
		for(FieldTemplate t : templates) {
			if(index.length() > 0) {
				index.append(",");
			}
			index.append(t.getUuid());
			Map<String,String> tm = new HashMap<>();
			t.toMap(tm);
			properties.put(t.getUuid(), tm);
		}
		m.put("templates", index.toString());
		
		properties.put(getUuid(), m);
	}
	
	public void fromMap(String uuid, Map<String, Map<String,String>> properties) throws ParseException {
		
		setUuid(uuid);
		Map<String,String> m = properties.get(uuid);
		
		this.resourceKey = m.get("resourceKey");
		this.weight = Integer.parseInt(m.get("weight"));
		this.templates = new HashSet<>();
		
		for(String templateUUID : m.get("templates").split(",")) {
			FieldTemplate t = new FieldTemplate();
			t.fromMap(templateUUID, properties.get(templateUUID));
			templates.add(t);
		}
	}
}
