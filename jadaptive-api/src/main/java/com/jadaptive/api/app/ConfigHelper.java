package com.jadaptive.api.app;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.jadaptive.api.tenant.Tenant;

public class ConfigHelper {


	static List<ResourcePackage> sharedPackages;
	static List<ResourcePackage> systemPrivatePackages;
	static Map<String,List<ResourcePackage>> tenantPackages = new HashMap<>();
	
	public static File getConfFolder() {
		return new File(System.getProperty("jadaptive.templatePath", "conf"));
	}

	public static File getSystemFolder() {
		return new File(getConfFolder(), "system");
	}
	
	public static File getSystemSubFolder(String folder) {
		return new File(getSystemFolder(), folder);
	}

	public static File getSharedFolder() {
		return new File(getSystemFolder(), "shared");
	}
	
	public static File getSharedSubFolder(String folder) {
		return new File(getSharedFolder(), folder);
	}
	
	public static File getSystemPrivateFolder() {
		return new File(getSystemFolder(), "private");
	}
	
	public static File getSystemPrivateSubFolder(String folder) {
		return new File(getSystemPrivateFolder(), folder);
	}
	
	public static File getTenantsFolder() {
		return new File(getConfFolder(), "tenants");
	}
	
	public static File getTenantFolder(Tenant tenant) {
		if(tenant.getSystem()) {
			return getSystemPrivateFolder();
		}
		return new File(getTenantsFolder(), tenant.getHostname());
	}
	
	public static File getTenantSubFolder(Tenant tenant, String folder) {
		return new File(getTenantFolder(tenant), folder);
	}
	
	public static Collection<ResourcePackage> getSharedPackages() throws IOException {
		
		if(sharedPackages==null) {
			sharedPackages = new ArrayList<>();
			File[] pkgs = getSharedFolder().listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			});
			if(Objects.nonNull(pkgs)) {
				for(File pkg : pkgs) {
					try {
						URI uri = new URI(String.format("jar:%s", pkg.toURI().toString()));
						sharedPackages.add(getZipPackage(uri, pkg.getName()));
					} catch (URISyntaxException e) {
						throw new IOException(e.getMessage(), e);
					}
				}
			}
			
		}
		return Collections.unmodifiableCollection(sharedPackages);
	}
	
	public static Collection<ResourcePackage> getTenantPackages(Tenant tenant) throws IOException {
		
		if(!tenantPackages.containsKey(tenant.getHostname())) {
			tenantPackages.put(tenant.getHostname(), new ArrayList<>());
			File[] pkgs = getTenantFolder(tenant).listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			});
			
			if(Objects.nonNull(pkgs)) {
				for(File pkg : pkgs) {
					try {
						URI uri = new URI(String.format("jar:%s", pkg.toURI().toString()));
						tenantPackages.get(tenant.getHostname()).add(getZipPackage(uri, pkg.getName()));
					} catch (URISyntaxException e) {
						throw new IOException(e.getMessage(), e);
					}
				}
			}
			
			
		}
		return Collections.unmodifiableCollection(tenantPackages.get(tenant.getHostname()));
	}
	
	public static Collection<ResourcePackage> getSystemPrivatePackages() throws IOException {
		
		if(systemPrivatePackages==null) {
			systemPrivatePackages = new ArrayList<>();
			File[] pkgs = getSystemPrivateFolder().listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			});
			if(Objects.nonNull(pkgs)) {
				for(File pkg : pkgs) {
					try {
						URI uri = new URI(String.format("jar:%s", pkg.toURI().toString()));
						systemPrivatePackages.add(getZipPackage(uri, pkg.getName()));
					} catch (URISyntaxException e) {
						throw new IOException(e.getMessage(), e);
					}
				}
			}
			
		}
		return Collections.unmodifiableCollection(systemPrivatePackages);
	}
	
	private static ResourcePackage getZipPackage(URI uri, String filename) throws IOException {
		
		FileSystem fs;
		try {
			fs = FileSystems.getFileSystem(uri);
		} catch(FileSystemNotFoundException e) {
			fs = FileSystems.newFileSystem(uri, new HashMap<>());
		}
		
		Path propertiesPath = fs.getPath("/package.properties");
		Properties properties = new Properties();
		if(Files.exists(propertiesPath)) {
			try(InputStream in = Files.newInputStream(propertiesPath)) {
				properties.load(in);
			}
		}
		return new ResourcePackage(uri, fs, filename, properties);
	}
	
}
