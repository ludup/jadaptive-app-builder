package com.jadaptive.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.Module;
import com.jadaptive.Version;
import com.jadaptive.entity.template.EntityTemplateImpl;
import com.jadaptive.json.ObjectMapperHolder;
import com.jadaptive.repository.AbstractUUIDRepositoryImpl;
import com.jadaptive.repository.RepositoryException;

public abstract class TemplateEnabledUUIDRepositoryImpl<E extends EntityTemplateImpl> extends AbstractUUIDRepositoryImpl<E> {

	@Autowired
	TemplateRepository templateRepository; 
	
	@Autowired
	ObjectMapperHolder objectMapper;
	
	protected String getResourceKey() {
		return String.format("%s%s", getResourceClass().getSimpleName().substring(0,1).toLowerCase(),
				getResourceClass().getSimpleName().substring(1));
	}
	
	
	protected void registerSerializationModule(Module module) {
		objectMapper.getObjectMapper().registerModule(module);
	}
	
	public void processTemplates() {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for entity {}", getResourceKey());
		}
		
		try {
			Version currentVersion = templateRepository.getCurrentVersion(getResourceKey());
			
			File dir = new File(".");
			File templateFolder = new File(dir, String.format("templates%s%s", File.separator, getResourceKey()));
			
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
			
			for(File template : orderedTemplates) {
				
				Version version = new Version(template.getName().replace(".json", ""));
				
				if(currentVersion==null || version.compareTo(currentVersion) > 0) {
					if(templateRepository.hasProcessed(getResourceKey(), version.toString())) {
						if(log.isInfoEnabled()) {
							log.info("Already processed {}", template.getName());
						}
						continue;
					}
					
					processTemplate(template, version);
				}
				
			}
			
			if(log.isInfoEnabled()) {
				log.info("Finished processing templates for entity {}", getResourceKey());
			}
		} catch(IOException | RepositoryException e) {
			log.error("Template repository failure!", e);
		}
		
	}

	
	@SuppressWarnings("unchecked")
	private void processTemplate(File resource, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", getResourceKey(), resource.getName());
			}
			
			List<E> templates = objectMapper.getObjectMapper().readValue(
					new FileInputStream(resource), 
					objectMapper.getObjectMapper().getTypeFactory().constructCollectionType(List.class, getResourceClass()));
			
			save(templates,  new TransactionAdapter<E>() {

				@Override
				public void afterSave(E object) throws RepositoryException {
					Template  t = new Template();
					t.setUuid(object.getUuid());
					t.setVersion(version.toString());
					templateRepository.save(t);
					
					log.info("Created {} '{}'", getResourceClass().getSimpleName(), object.getUuid());
				}
			});
			

			
		} catch (Throwable e) {
			log.error(String.format("Failed to process template %s", getResourceKey()), e);
		}
		
	}


	public abstract Integer getWeight();
}
