package com.jadaptive.entity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ExtensionRegistration;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectExtension;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.app.db.DocumentHelper;

public class MockEntityTemplateService implements TemplateService {

	Map<String,ObjectTemplate> templates;
	
	public MockEntityTemplateService(Map<String,ObjectTemplate> templates) {
		this.templates = templates;
	}
	@Override
	public ObjectTemplate get(String uuid) throws RepositoryException, ObjectException {
		return templates.get(uuid);
	}

	@Override
	public Collection<ObjectTemplate> list() throws RepositoryException, ObjectException {
		return templates.values();
	}

	@Override
	public void saveOrUpdate(ObjectTemplate template) throws RepositoryException, ObjectException {

	}

	@Override
	public Collection<ObjectTemplate> table(String searchField, String searchValue,  int start, int length, SortOrder order, String sortField) throws RepositoryException, ObjectException {
		return new ArrayList<>(templates.values()).subList(start, Math.min(start + length, templates.values().size()-1));
	}
	@Override
	public long count() {
		return templates.size();
	}

	@Override
	public <T extends UUIDEntity> T createObject(Map<String,Object> values, Class<T> baseClass) throws ParseException {
		return DocumentHelper.convertDocumentToObject(baseClass, new Document(values));
	}
	@Override
	public Collection<ObjectTemplate> children(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<ObjectTemplate> singletons() throws RepositoryException, ObjectException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<ObjectTemplate> getTemplatesWithScope(ObjectScope personal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TemplateView> getViews(ObjectTemplate template, boolean singleView) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Iterable<ObjectTemplate> allCollectionTemplates() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Class<? extends UUIDDocument> getTemplateClass(String resourceKey) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void registerTemplateClass(String resourceKey, Class<? extends UUIDDocument> templateClazz, ObjectTemplate template) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getTemplateResourceKey(Class<?> clz) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTemplateResourceKey(String clz) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FieldRenderer getRenderer(FieldTemplate field, ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SortOrder getTableSortOrder(ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getTableSortField(ObjectTemplate def) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void delete(ObjectTemplate objectTemplate) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ObjectTemplate getParentTemplate(ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<ExtensionRegistration> getTemplateExtensions(ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ObjectTemplate getBaseTemplate(ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}

}
