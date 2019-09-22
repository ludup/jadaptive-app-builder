package com.jadaptive.templates;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.jadaptive.app.ConfigHelper;
import com.jadaptive.entity.EntityException;
import com.jadaptive.json.ObjectMapperHolder;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.tenant.Tenant;
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
	public <E extends AbstractUUIDEntity> void processTemplates(Tenant tenant, TemplateEnabledService<E> templateEnabledService) {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for {}", templateEnabledService.getResourceKey());
		}
		
		try {
			
			Collection<Path> orderedTemplates = findVersionedTemplates(
					buildTemplatePaths(tenant, templateEnabledService));
			
			Set<String> resourceKeys = new HashSet<>();
			for(Path template : orderedTemplates) {
				
				String filename = template.getFileName().toString().substring(0, template.getFileName().toString().length()-5);
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
							log.info("Already processed {}", template.getFileName());
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


	private List<Path> buildTemplatePaths(Tenant tenant, TemplateEnabledService<?> templateEnabledService) {
		
		List<Path> paths = new ArrayList<>();
		
		if(!templateEnabledService.isSystemOnly()) {
			File sharedConf = new File(ConfigHelper.getSharedFolder(), templateEnabledService.getTemplateFolder());
			paths.add(sharedConf.toPath());

			if(!tenant.getSystem()) {

				File tenantConf = new File(ConfigHelper.getTenantsFolder(), tenant.getHostname());
				File templateConf = new File(tenantConf, templateEnabledService.getTemplateFolder());
				paths.add(templateConf.toPath());
				
			} else {
				File prvConf = new File(ConfigHelper.getSystemPrivateFolder(), templateEnabledService.getTemplateFolder());
				paths.add(prvConf.toPath());
			}
			
		} else {
			paths.add(ConfigHelper.getSystemSubFolder(templateEnabledService.getTemplateFolder()).toPath());	
		}
		
		return paths;
	}

	protected Collection<Path> findVersionedTemplates(List<Path> paths) throws IOException {
		
		List<Path> orderedTemplates = new ArrayList<>();
		
		for(Path path : paths) {
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Searching for templates folder in %s", 
						path.toString()));
			}
			
			if(Files.exists(path)) {
				Files.list(path)
				.filter(f -> f.getFileName().toString().endsWith(".json"))
				.forEach(jsonFile -> {
					orderedTemplates.add(jsonFile);
				});
			}

		}
		
		Collections.<Path>sort(orderedTemplates, new Comparator<Path>() {
			
			@Override
			public int compare(Path o1, Path o2) {
				return o1.getFileName().compareTo(o2.getFileName());
			}
		});
		
		return orderedTemplates;
	}

	@SuppressWarnings("unchecked")
	private <E extends AbstractUUIDEntity> void processTemplate(Path resource, String resourceKey, TemplateEnabledService<E> repository, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", resourceKey, version.toString());
			}
			
			List<E> objects = objectMapper.getObjectMapper().readValue(
					Files.newInputStream(resource), 
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
