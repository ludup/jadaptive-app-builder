package com.jadaptive.entity.template;

import com.jadaptive.templates.TemplateEnabledUUIDRepository;

public interface EntityTemplateRepository extends TemplateEnabledUUIDRepository<EntityTemplateImpl> {

	EntityTemplateImpl getByResourceKey(String resourceKey);

}
