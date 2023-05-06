package com.jadaptive.app;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.pf4j.PluginRepository;
import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;

public class SinglePluginRepository implements PluginRepository {

	Path pluginPath;
	
	public SinglePluginRepository(Path pluginPath) {
		this.pluginPath = pluginPath;
	}

	@Override
	public List<Path> getPluginPaths() {
		return Arrays.asList(pluginPath);
	}

	@Override
	public boolean deletePluginPath(Path pluginPath) {
		if(pluginPath.equals(this.pluginPath)) {
			try {
	            FileUtils.delete(pluginPath);
	            return true;
	        } catch (NoSuchFileException e) {
	            return false; // Return false on not found to be compatible with previous API (#135)
	        } catch (IOException e) {
	            throw new PluginRuntimeException(e);
	        }
		}
		return false;
	}
	

}
