package com.jadaptive.app;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.tenant.Tenant;

public class ConfigHelper {


	static List<ZipPackage> sharedPackages;
	static List<ZipPackage> systemPrivatePackages;
	static Map<String,List<ZipPackage>> tenantPackages = new HashMap<>();
	
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
		return new File(getTenantsFolder(), tenant.getHostname());
	}
	
	public static File getTenantSubFolder(Tenant tenant, String folder) {
		return new File(getTenantFolder(tenant), folder);
	}
	
	public static Collection<ZipPackage> getSharedPackages() throws IOException {
		
		if(sharedPackages==null) {
			sharedPackages = new ArrayList<>();
			for(File pkg : getSharedFolder().listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			})) {
				try {
					URI uri = new URI(String.format("jar:%s", pkg.toURI().toString()));
					sharedPackages.add(getZipPackage(uri, pkg.getName()));
				} catch (URISyntaxException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
			
			
		}
		return Collections.unmodifiableCollection(sharedPackages);
	}
	
	public static Collection<ZipPackage> getTenantPackages(Tenant tenant) throws IOException {
		
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
	
	public static Collection<ZipPackage> getSystemPrivatePackages() throws IOException {
		
		if(systemPrivatePackages==null) {
			systemPrivatePackages = new ArrayList<>();
			for(File pkg : getSystemPrivateFolder().listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".zip");
				}
			})) {
				try {
					URI uri = new URI(String.format("jar:%s", pkg.toURI().toString()));
					systemPrivatePackages.add(getZipPackage(uri, pkg.getName()));
				} catch (URISyntaxException e) {
					throw new IOException(e.getMessage(), e);
				}
			}
			
			
		}
		return Collections.unmodifiableCollection(systemPrivatePackages);
	}
	
	
	private static ZipPackage getZipPackage(URI uri, String filename) throws IOException {
		
		FileSystem fs;
		try {
			fs = FileSystems.getFileSystem(uri);
		} catch(FileSystemNotFoundException e) {
			fs = FileSystems.newFileSystem(uri, new HashMap<>());
		}
		
		return new ZipPackage(uri, fs, filename);
	}
	
}
