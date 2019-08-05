package com.jadaptive.entity.template;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.jadaptive.repository.NamedUUIDEntity;

public class FieldCategory extends NamedUUIDEntity {

	String resourceKey;
	Integer weight;
	Set<FieldTemplate> fields = new HashSet<>(); 

	public String getResourceKey() {
		return StringUtils.defaultString(resourceKey);
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public Integer getWeight() {
		return weight;
	}

	public String getAnchor() {
		return "#" + getResourceKey();
	}
	
	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Set<FieldTemplate> getFields() {
		return fields;
	}

	public void setFields(Set<FieldTemplate> fields) {
		this.fields = fields;
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

}
