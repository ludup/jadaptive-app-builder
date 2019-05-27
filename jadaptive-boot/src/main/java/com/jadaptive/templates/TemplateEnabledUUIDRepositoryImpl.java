package com.jadaptive.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.Module;
import com.jadaptive.json.ObjectMapperHolder;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.AbstractUUIDRepositoryImpl;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.utils.Version;

public abstract class TemplateEnabledUUIDRepositoryImpl<E extends AbstractUUIDEntity> extends AbstractUUIDRepositoryImpl<E> {

	@Autowired
	protected TemplateRepository templateRepository; 
	
	@Autowired
	protected ObjectMapperHolder objectMapper;
	
	protected String getResourceKey() {
		return String.format("%s%s", getResourceClass().getSimpleName().substring(0,1).toLowerCase(),
				getResourceClass().getSimpleName().substring(1));
	}
	
	
	protected void registerSerializationModule(Module module) {
		objectMapper.getObjectMapper().registerModule(module);
	}
	
	public void processTemplates() {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for {}", getResourceKey());
		}
		
		try {
			
			Collection<File> orderedTemplates = findVersionedTemplates(
					String.format("templates%s%s", File.separator, getResourceKey()));
			
			for(File template : orderedTemplates) {
				
				String filename = template.getName().substring(0, template.getName().length()-5);
				String[] elements = filename.split("_");
				if(elements.length != 2) {
					throw new IOException("Template json file should be named <id>_<version>.json");
				}
				Version version = new Version(elements[1]);
				String templateResourceKey = elements[0];
				String uuid = getResourceKey() + "_" + templateResourceKey;
				Version currentVersion = templateRepository.getCurrentVersion(uuid);
				
				if(currentVersion==null || version.compareTo(currentVersion) > 0) {
					if(templateRepository.hasProcessed(uuid, version.toString())) {
						if(log.isInfoEnabled()) {
							log.info("Already processed {}", template.getName());
						}
						continue;
					}
					
					processTemplate(template, uuid, templateResourceKey, version);
				}
				
			}
			
			if(log.isInfoEnabled()) {
				log.info("Finished processing templates for {}", getResourceKey());
			}
			
		} catch(IOException | RepositoryException e) {
			log.error("Template repository failure!", e);
		}
		
	}


	protected Collection<File> findVersionedTemplates(String path) throws IOException {
		
		File dir = new File(".");
		File templateFolder = new File(dir, path);
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Searching for templates folder in %s [%b]", 
					templateFolder.getCanonicalPath(), 
					templateFolder.exists()));
		}
		
		FileFilter fileFilter = new WildcardFileFilter("*.json");
		List<File> orderedTemplates = new ArrayList<File>();
		File[] files = templateFolder.listFiles(fileFilter);
		if(!Objects.isNull(files)) {
			for (File r : files) {
				orderedTemplates.add(r);
			}
			
			Collections.<File>sort(orderedTemplates, new Comparator<File>() {
	
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
		return orderedTemplates;
	}


	@SuppressWarnings("unchecked")
	private void processTemplate(File resource, String uuid, String resourceKey, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", uuid, version.toString());
			}
			
			List<E> templates = objectMapper.getObjectMapper().readValue(
					new FileInputStream(resource), 
					objectMapper.getObjectMapper().getTypeFactory().constructCollectionType(List.class, getResourceClass()));
			
			save(templates,  new TransactionAdapter<E>() {

				@Override
				public void afterSave(E object) throws RepositoryException {
					Template  t = new Template();
					t.setUuid(uuid);
					t.setVersion(version.toString());
					templateRepository.save(t);
					
					log.info("Created {} {} '{}' version {}", getResourceClass().getSimpleName(), resourceKey, object.getUuid(), version.toString());
				}
			});
			
		} catch (Throwable e) {
			log.error(String.format("Failed to process template %s", uuid), e);
		}
		
	}


	public abstract Integer getWeight();
}
