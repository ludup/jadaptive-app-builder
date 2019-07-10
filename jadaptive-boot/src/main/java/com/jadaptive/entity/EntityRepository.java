package com.jadaptive.entity;

import com.jadaptive.templates.TemplateEnabledUUIDRepository;

public interface EntityRepository extends TemplateEnabledUUIDRepository<Entity> {

	void delete(String resourceKey, String uuid);

}
