package com.jadaptive.templates;

import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.AbstractUUIDRepository;

public interface TemplateEnabledUUIDRepository<T extends AbstractUUIDEntity> extends AbstractUUIDRepository<T> {

	Integer getWeight();

	void processTemplates();

}
