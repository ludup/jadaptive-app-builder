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
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.template.TemplateService;
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
	public void delete(String uuid) throws ObjectException {

	}
	@Override
	public Collection<ObjectTemplate> table(String searchField, String searchValue,  String order, int start, int length) throws RepositoryException, ObjectException {
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
	public void registerObjectDependency(String resourceKey, ObjectTemplate template) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<OrderedView> getViews(ObjectTemplate template) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Iterable<ObjectTemplate> allCollectionTemplates() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Class<?> getTemplateClass(String resourceKey) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void registerTemplateClass(String resourceKey, Class<?> templateClazz) {
		// TODO Auto-generated method stub
		
	}

}
