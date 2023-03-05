package com.jadaptive.api.templates;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;

public interface TemplateVersionService {

	Iterable<TemplateVersion> list() throws RepositoryException, ObjectException;

	<E extends AbstractUUIDEntity>  void processTemplates(Tenant tenant, JsonTemplateEnabledService<E> repository);

	void registerAnnotatedTemplates(boolean newSchema);

	Class<? extends ObjectEvent<?>> getEventClass(String resourceKey);

	void registerTenantIndexes(boolean newSchema);

	void rebuildReferences();

	void registerAnnotatedTemplate(Class<? extends UUIDDocument> clz, boolean newSchema, boolean isEvent);

	UUIDEntity extendWith(UUIDEntity baseObject, ObjectTemplate extensionTemplate, String packageName,
			String className);

}
