package com.jadaptive.app.templates;

import java.io.File;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionRepository;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.app.ConfigHelper;
import com.jadaptive.app.ResourcePackage;
import com.jadaptive.app.json.ObjectMapperHolder;
import com.jadaptive.app.repository.AbstractUUIDEntity;
import com.jadaptive.app.repository.RepositoryException;
import com.jadaptive.app.repository.TransactionAdapter;
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
			
			Collection<PathInfo> orderedTemplates = findVersionedTemplates(
					buildTemplatePaths(tenant, templateEnabledService));
			
			Set<String> resourceKeys = new HashSet<>();
			for(PathInfo template : orderedTemplates) {
				
				String filename = template.path.getFileName().toString().substring(0, template.path.getFileName().toString().length()-5);
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
							log.info("Already processed {}", template.path.getFileName());
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


	private List<PathInfo> buildTemplatePaths(Tenant tenant, TemplateEnabledService<?> templateEnabledService) throws IOException {
		
		List<PathInfo> paths = new ArrayList<>();
		
		if(log.isInfoEnabled()) {
			log.info("Looking for {} templates in {}", templateEnabledService.getTemplateFolder(), tenant.getName());
		}
		
		if(!templateEnabledService.isSystemOnly()) {
			File sharedConf = new File(ConfigHelper.getSharedFolder(), templateEnabledService.getTemplateFolder());
			
			if(sharedConf.exists()) {
				paths.add(new PathInfo(sharedConf.toPath()));
			}
			
			for(ResourcePackage pkg : ConfigHelper.getSharedPackages()) {
				paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
			}
			
			if(!tenant.getSystem()) {

				File tenantConf = new File(ConfigHelper.getTenantsFolder(), tenant.getHostname());
				File templateConf = new File(tenantConf, templateEnabledService.getTemplateFolder());
				if(templateConf.exists()) {
					paths.add(new PathInfo(templateConf.toPath()));
				}
				for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
					paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
				}
			} else {
				File prvConf = new File(ConfigHelper.getSystemPrivateFolder(), templateEnabledService.getTemplateFolder());
				if(prvConf.exists()) {
					paths.add(new PathInfo(prvConf.toPath()));
				}
				for(ResourcePackage pkg : ConfigHelper.getSystemPrivatePackages()) {
					paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
				}
			}
			
		} else {
			File systemConf = ConfigHelper.getSystemSubFolder(templateEnabledService.getTemplateFolder());
			if(systemConf.exists()) {
				paths.add(new PathInfo(systemConf.toPath()));
			}
			for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
				paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
			}
		}
		
		return paths;
	}

//	private void lookupTemplatesInZipFiles(Path path, String templateName, List<PathInfo> paths) {
//		
//		try {
//			if(Files.exists(path)) {
//				Files.list(path)
//				.filter(f -> f.getFileName().toString().endsWith(".zip"))
//				.forEach(zipFile -> {
//					URI uri = URI.create(String.format("jar:file:%s", zipFile.toAbsolutePath().toString()));
//	
//					
//					try {
//						FileSystem zipfs;
//						try {
//							zipfs = FileSystems.getFileSystem(uri);
//						} catch(FileSystemNotFoundException e) {
//							zipfs = FileSystems.newFileSystem(uri, new HashMap<>());
//						}
//						Path inzip = zipfs.getPath(templateName);
//						if(Files.exists(inzip)) {
//							paths.add(new PathInfo(uri, zipfs, inzip.toAbsolutePath()));
//						}
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//	
//	
//				});
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
		
	protected Collection<PathInfo> findVersionedTemplates(List<PathInfo> paths) throws IOException {
		
		List<PathInfo> orderedTemplates = new ArrayList<>();
		
		for(PathInfo path : paths) {
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Searching for templates folder in %s", 
						path.path.toString()));
			}

			if(Files.exists(path.getPath())) {
				Files.list(path.getPath())
				.filter(f -> f.getFileName().toString().endsWith(".json"))
				.forEach(jsonFile -> {
					orderedTemplates.add(new PathInfo(jsonFile));
				});
			}
			

		}
		
		Collections.<PathInfo>sort(orderedTemplates, new Comparator<PathInfo>() {
			
			@Override
			public int compare(PathInfo o1, PathInfo o2) {
				return o1.getFilename().toString().compareTo(o2.getFilename().toString());
			}
		});
		
		return orderedTemplates;
	}

	@SuppressWarnings("unchecked")
	private <E extends AbstractUUIDEntity> void processTemplate(PathInfo resource, String resourceKey, TemplateEnabledService<E> repository, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", resourceKey, version.toString());
			}
			
			List<E> objects = objectMapper.getObjectMapper().readValue(
					Files.newInputStream(resource.path), 
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
	
	class PathInfo {
		
		ResourcePackage pkg;
		Path path;
		
		PathInfo(ResourcePackage pkg, String path) {
			this.pkg = pkg;
			this.path = pkg.resolvePath(path);
		}
		
		PathInfo(Path path) {
			this.path = path;
		}

		public boolean isPackage() {
			return Objects.nonNull(pkg);
		}
		
		public ResourcePackage getPkg() {
			return pkg;
		}

		public Path getPath() {
			return path;
		}
		
		public String getFilename() {
			return Objects.nonNull(pkg) ? pkg.getFilename() : path.getFileName().toString();
		}
		
		
	}
}
