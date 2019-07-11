package com.jadaptive.entity.template;

import java.util.HashSet;
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

}
