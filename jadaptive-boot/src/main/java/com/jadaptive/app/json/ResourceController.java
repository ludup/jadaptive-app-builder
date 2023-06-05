package com.jadaptive.app.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.app.ResourcePackage;
import com.jadaptive.api.permissions.ExceptionHandlingController;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.ResponseHelper;
import com.jadaptive.utils.FileUtils;

@Controller
public class ResourceController extends ExceptionHandlingController {
	
	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PluginManager pluginManager; 
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private SessionUtils sessionUtils;
	
	private MimeMappings mimeTypes = new MimeMappings(MimeMappings.DEFAULT);
	
	@RequestMapping(value="/ping", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	public String ping(HttpServletRequest request, HttpServletResponse response) {
		return "PONG\r\n";
	}

	@RequestMapping(value="/app/content/**", method = RequestMethod.GET)
	public void doResourceGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		tenantService.setCurrentTenant(request);
		
		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 12 ? uri.substring(12) : "";
		
		try {	
			
			if(resourceUri.endsWith("security.properties")) {
				ResponseHelper.send404NotFound(uri, request, response);
				return;
			}
			
			Path resource = resolveResource(request, resourceUri);
			
			if(Files.exists(resource) && Files.isDirectory(resource) && !uri.endsWith("/")) {
				ResponseHelper.sendRedirect(uri + "/", request, response);
				return;
			}
			
			if(!Files.exists(resource)  || Files.isDirectory(resource) ) {
				if(!Files.isDirectory(resource)) {
					resource = resolveResource(request, FileUtils.checkEndsWithNoSlash(resourceUri) + ".html");
				} else {
					resource = resolveResource(request, FileUtils.checkEndsWithSlash(resourceUri) + "index.html");
				}
				
				if(!Files.exists(resource)) {
					if(log.isInfoEnabled()) {
						log.info("Resource not found {}", uri);
					}

					tryClasspathFailover(request, response, resourceUri);
					return;
				}
			} 
			
			if(log.isDebugEnabled()) {
				log.debug("Returning content for {}", uri);
			}
			
			sessionUtils.setCachable(response, 600);
			
			ResponseHelper.sendContent(resource, getContentType(resource), request, response);
		
		} catch(FileNotFoundException e) { 
			try {
				tryClasspathFailover(request, response, resourceUri);
			} catch(FileNotFoundException e2) {
				ResponseHelper.send404NotFound(uri, request, response);
			}
		} catch(Throwable e) { 
			log.error("Error loading content resource " + uri, e);
		} finally {
			tenantService.clearCurrentTenant();
		}

	}

	private void tryClasspathFailover(HttpServletRequest request, HttpServletResponse response, String uri) throws FileNotFoundException, IOException {
		
		InputStream in = getClass().getClassLoader().getResourceAsStream("webapp" + uri);

		if(Objects.nonNull(in)) {
			if(log.isDebugEnabled()) {
				log.debug("Returning content from InputStream for webapp{}", uri);
			}
			
			sessionUtils.setCachable(response, 600);
			
			ResponseHelper.sendContent(IOUtils.toString(in, "UTF-8"), getContentType(uri), request, response);
			return;
		}
		
		if(log.isDebugEnabled()) {
			log.debug("No match for {}", uri);
		}
		
		throw new FileNotFoundException();
		
	}

	private String getContentType(Path resource) {
		String type = mimeTypes.get(getExtension(resource.getFileName().toString()));
		if(Objects.isNull(type)) {
			return "application/octet-stream";
		}
		return type;
	}
	
	private String getContentType(String resource) {
		String type = mimeTypes.get(getExtension(resource));
		if(Objects.isNull(type)) {
			return "application/octet-stream";
		}
		return type;
	}

	private String getExtension(String filename) {
		int idx = filename.toString().lastIndexOf(".");
		if(idx > -1) {
			return filename.substring(idx+1);
		}
		return filename;
	}

	private Path resolveResource(HttpServletRequest request, String resourceUri) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		String uri = "webapp" + resourceUri;
		
		if(log.isDebugEnabled()) {
			log.debug("Resolving resource {}", resourceUri);
		}
		
		if(!tenant.isSystem()) {
			
			/**
			 * Process tenant files, then packages
			 */
			File res = new File(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri);
			if(res.exists()) {
				if(log.isDebugEnabled()) {
					log.debug("Resource {} was found in tenant webapp folder", resourceUri);
				}
				return res.toPath();
			}
			
			try {
				for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
					if(pkg.containsPath(uri)) {
						if(log.isDebugEnabled()) {
							log.debug("Resource {} was found in tenant zip package", resourceUri);
						}
						return pkg.resolvePath(uri);
					}
				}
			} catch (Throwable e) {
				log.error("Failed to process package file for " + uri, e);
			}
			
		} else {
			
			/**
			 * Process system files, then packages
			 */
			File res = new File(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri);
			if(res.exists()) {
				if(log.isDebugEnabled()) {
					log.debug("Resource {} was found in system private webapp folder", resourceUri);
				}
				return res.toPath();
			}			
			
			try {
				for(ResourcePackage pkg : ConfigHelper.getSystemPrivatePackages()) {
					if(pkg.containsPath(uri)) {
						if(log.isDebugEnabled()) {
							log.debug("Resource {} was found in system private zip package", resourceUri);
						}
						return pkg.resolvePath(uri);
					}
				}
			} catch (Throwable e) {
				log.debug("Failed to process package file for " + uri, e);
			}
		}
		
		File res = new File(ConfigHelper.getSharedSubFolder("webapp"), resourceUri);
		if(res.exists()) {
			if(log.isDebugEnabled()) {
				log.debug("Resource {} was found in system shared webapp folder", resourceUri);
			}
			return res.toPath();
		}
		
		try {
			for(ResourcePackage pkg : ConfigHelper.getSharedPackages()) {
				if(pkg.containsPath(uri)) {
					if(log.isDebugEnabled()) {
						log.debug("Resource {} was found in system shared zip package", resourceUri);
					}
					return pkg.resolvePath(uri);
				}
			}
		} catch (Throwable e) {
			log.debug("Failed to process package file for " + uri, e);
		}

		try {
			res = ResourceUtils.getFile("classpath:" + uri);
			if(log.isDebugEnabled()) {
				log.debug("Resource {} was found in spring boot resources with relative path", resourceUri);
			}
			return res.toPath();

		} catch(FileNotFoundException e) {
			log.debug("Failed to process spring boot resource for " + uri, e);
		}
		
		try {
			res = ResourceUtils.getFile("classpath:/" + uri);
			if(log.isDebugEnabled()) {
				log.debug("Resource {} was found in spring boot resources with absolute path", resourceUri);
			}
			return res.toPath();

		} catch(FileNotFoundException e) {
			log.debug("Failed to process spring boot resource for " + uri, e);
		}

		Resource resource = applicationContext.getResource("classpath:" + uri);
		if(resource.exists()) {
			try {
				Path path = resource.getFile().toPath();
				if(log.isDebugEnabled()) {
					log.debug("Resource {} was found in application context resources", resourceUri);
				}
				return path;
			} catch (Throwable e) {
				log.debug("Failed to process classpath resource for " + uri, e);
			}
		}
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			URL url = w.getPluginClassLoader().getResource(FileUtils.checkStartsWithNoSlash(uri));
			if(Objects.nonNull(url)) {
				try {
					Path path = Paths.get(url.toURI());
					if(log.isDebugEnabled()) {
						log.debug("Resource {} was found in plugin {} classpath", resourceUri, w.getPluginId());
					}
					return path;
				} catch (Throwable e) {
					log.debug("Failed to process classpath resource for " + uri, e);
				}
			}
		}

		URL url = getClass().getClassLoader().getResource(uri);
		if(Objects.nonNull(url)) {
			try {
				log.info(url.toURI().toASCIIString());
				Path path = Paths.get(url.toURI());
				if(log.isDebugEnabled()) {
					log.debug("Resource {} was found in class loader resources", resourceUri);
				}
				return path;
			} catch (Throwable e) {
				log.debug("Failed to process classpath resource for " + uri, e);
			}
		}
		
		throw new FileNotFoundException(String.format("%s not found", uri));

	}


	

}
