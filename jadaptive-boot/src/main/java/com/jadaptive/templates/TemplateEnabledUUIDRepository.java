package com.jadaptive.templates;

import com.jadaptive.entity.repository.AbstractUUIDEntityImpl;
import com.jadaptive.repository.AbstractUUIDRepository;

public interface TemplateEnabledUUIDRepository<T extends AbstractUUIDEntityImpl> extends AbstractUUIDRepository<T> {

	Integer getWeight();

	void processTemplates();

}
