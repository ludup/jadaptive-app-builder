package com.jadaptive.api.templates;

import java.util.Collection;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.ObjectUpdateEvent;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;

public interface TemplateVersionService {

	Iterable<TemplateVersion> list() throws RepositoryException, ObjectException;

	<E extends AbstractUUIDEntity>  void processTemplates(Tenant tenant, JsonTemplateEnabledService<E> repository);

	void registerAnnotatedTemplates(boolean newSchema);

	Class<? extends ObjectEvent<?>> getEventClass(String resourceKey);
	
	Class<? extends ObjectUpdateEvent<?>> getUpdateEventClass(String resourceKey);

	void registerTenantIndexes(boolean newSchema);

	void rebuildReferences();

	ObjectTemplate registerAnnotatedTemplate(Class<? extends UUIDDocument> clz, boolean newSchema);

	void loadExtendedTemplates(Tenant tenant);

	AbstractObject extendWith(AbstractObject baseObject, 
			ObjectTemplate extensionTemplate,
			Collection<String> extensions);

}
