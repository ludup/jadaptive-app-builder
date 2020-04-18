package com.jadaptive.entity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.bson.Document;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.app.db.DocumentHelper;

public class MockEntityTemplateService implements EntityTemplateService {

	Map<String,EntityTemplate> templates;
	
	public MockEntityTemplateService(Map<String,EntityTemplate> templates) {
		this.templates = templates;
	}
	@Override
	public EntityTemplate get(String uuid) throws RepositoryException, EntityException {
		return templates.get(uuid);
	}

	@Override
	public Collection<EntityTemplate> list() throws RepositoryException, EntityException {
		return templates.values();
	}

	@Override
	public void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException {

	}

	@Override
	public void delete(String uuid) throws EntityException {

	}
	@Override
	public Collection<EntityTemplate> table(String searchField, String searchValue,  String order, int start, int length) throws RepositoryException, EntityException {
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

}
