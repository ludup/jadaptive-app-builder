package com.jadaptive.api.plugins;

import java.io.IOException;
import java.nio.file.Path;

public interface PluginManagerService {
	
	boolean installed(String groupId, String artifactId) throws IOException;
	
	Path installOrUpdate(String groupId, String artifactId) throws IOException;

	void setRepository(String url, String username, char[] password);

	boolean isUpdateable(String groupId, String artifactId) throws IOException;
	
	public static Path expandedDirectoryForZipFile(Path pdir) {
		var fname = pdir.getFileName().toString();
		var idx = fname.lastIndexOf('.');
		return pdir.getParent().resolve(fname.substring(0, idx));
	}
	
}
