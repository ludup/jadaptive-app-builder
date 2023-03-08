package com.jadaptive.api.template;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;

public interface TemplateService {

	ObjectTemplate get(String uuid)
			throws RepositoryException, ObjectException;

	Iterable<ObjectTemplate> list() throws RepositoryException, ObjectException;
	
	Iterable<ObjectTemplate> singletons() throws RepositoryException, ObjectException;
	
	Collection<ObjectTemplate> table(String searchField, String searchValue, int start, int length, SortOrder order, String sortField) throws RepositoryException, ObjectException;

	void saveOrUpdate(ObjectTemplate template) throws RepositoryException, ObjectException;

//	void delete(String uuid) throws ObjectException;

	long count();

	<T extends UUIDEntity> T createObject(Map<String, Object> values, Class<T> baseClass) throws ParseException;

	Iterable<ObjectTemplate> children(String uuid);

	Iterable<ObjectTemplate> getTemplatesWithScope(ObjectScope personal);

//	void registerObjectDependency(String resourceKey, ObjectTemplate template);

	List<TemplateView> getViews(ObjectTemplate template, boolean singleView);

	Iterable<ObjectTemplate> allCollectionTemplates();

	Class<? extends UUIDDocument> getTemplateClass(String resourceKey);

	void registerTemplateClass(String resourceKey, Class<? extends UUIDDocument> templateClazz, ObjectTemplate template);

	String getTemplateResourceKey(Class<?> clz);

	String getTemplateResourceKey(String clz);

	FieldRenderer getRenderer(FieldTemplate field, ObjectTemplate template);

	SortOrder getTableSortOrder(ObjectTemplate template);

	String getTableSortField(ObjectTemplate def);

	void delete(ObjectTemplate objectTemplate);

	ObjectTemplate getParentTemplate(ObjectTemplate template);

	Collection<ExtensionRegistration> getTemplateExtensions(ObjectTemplate template);

	ObjectTemplate getBaseTemplate(ObjectTemplate template);

}
