package com.jadaptive.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.entity.EntityException;
import com.jadaptive.json.ObjectMapperHolder;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.utils.Version;

@Service
public class TemplateVersionServiceImpl extends AbstractLoggingServiceImpl implements TemplateVersionService  {
	
	@Autowired
	protected TemplateVersionRepository templateRepository; 
	
	@Autowired
	protected ObjectMapperHolder objectMapper;
	
	@Override
	public Collection<TemplateVersion> list() throws RepositoryException, EntityException {
		return templateRepository.list();
	}
	
	@Override
	public <E extends AbstractUUIDEntity> void processTemplates(TemplateEnabledService<E> templateEnabledService) {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for {}", templateEnabledService.getResourceKey());
		}
		
		try {
			
			Collection<File> orderedTemplates = findVersionedTemplates(
					String.format("templates%s%s", File.separator, templateEnabledService.getResourceKey()));
			
			Set<String> resourceKeys = new HashSet<>();
			for(File template : orderedTemplates) {
				
				String filename = template.getName().substring(0, template.getName().length()-5);
				String[] elements = filename.split("_");
				if(elements.length != 2) {
					throw new IOException("Template json file should be named <id>_<version>.json");
				}
				Version version = new Version(elements[1]);
				String templateResourceKey = elements[0];
				resourceKeys.add(templateResourceKey);
				
				String uuid = templateEnabledService.getResourceKey() + "_" + templateResourceKey;
				Version currentVersion = templateRepository.getCurrentVersion(uuid);
				
				if(currentVersion==null || version.compareTo(currentVersion) > 0) {
					if(templateRepository.hasProcessed(uuid, version.toString())) {
						if(log.isInfoEnabled()) {
							log.info("Already processed {}", template.getName());
						}
						continue;
					}
					
					processTemplate(template, uuid, templateEnabledService, version);
				}
				
			}
			
			templateEnabledService.onTemplatesComplete(resourceKeys.toArray(new String[0]));
			
			if(log.isInfoEnabled()) {
				log.info("Finished processing templates for {}", templateEnabledService.getResourceKey());
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
	private <E extends AbstractUUIDEntity> void processTemplate(File resource, String resourceKey, TemplateEnabledService<E> repository, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", resourceKey, version.toString());
			}
			
			List<E> objects = objectMapper.getObjectMapper().readValue(
					new FileInputStream(resource), 
					objectMapper.getObjectMapper().getTypeFactory().constructCollectionType(List.class, repository.getResourceClass()));
			
			repository.saveTemplateObjects(objects, new TransactionAdapter<E>() {

				@Override
				public void afterSave(E object) throws RepositoryException, EntityException {
					TemplateVersion  t = new TemplateVersion();
					t.setUuid(resourceKey);
					t.setVersion(version.toString());
					templateRepository.save(t);
					
					log.info("Created {} {} '{}' version {}",
							repository.getResourceClass().getSimpleName(), 
							repository.getResourceKey(), 
							object.getUuid(), 
							version.toString());
				}
			});

		} catch (Throwable e) {
			log.error(String.format("Failed to process template %s", resourceKey), e);
		}
		
	}
}
