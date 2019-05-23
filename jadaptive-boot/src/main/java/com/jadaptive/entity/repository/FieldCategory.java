package com.jadaptive.entity.repository;

import java.util.Set;

import com.jadaptive.entity.template.FieldTemplateImpl;

public interface FieldCategory extends AbstractUUIDEntity {

	String getResourceKey();

	void setResourceKey(String resourceKey);

	Integer getWeight();

	void setWeight(Integer weight);

	Set<FieldTemplateImpl> getTemplates();

	void setTemplates(Set<FieldTemplateImpl> templates);

}
