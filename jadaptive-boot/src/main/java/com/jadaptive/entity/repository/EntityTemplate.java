package com.jadaptive.entity.repository;

import java.util.Set;

import com.jadaptive.entity.template.FieldCategoryImpl;

public interface EntityTemplate {

	EntityType getType();

	void setType(EntityType type);

	Set<FieldCategoryImpl> getCategories();

	void setCategories(Set<FieldCategoryImpl> categories);

}
