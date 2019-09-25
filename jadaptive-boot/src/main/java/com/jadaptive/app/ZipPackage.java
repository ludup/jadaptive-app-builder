package com.jadaptive.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZipPackage {

	URI uri;
	FileSystem fs;
	String filename;
	
	public ZipPackage(URI uri, FileSystem fs, String filename) {
		this.uri = uri;
		this.fs = fs;
		this.filename = filename;
	}
	
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public FileSystem getFs() {
		return fs;
	}
	
	public void setFs(FileSystem fs) {
		this.fs = fs;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public Path resolvePath(String path) {
		return fs.getPath(path);
	}
	
	public boolean containsPath(String path) {
		return Files.exists(resolvePath(path));
	}
	
	public InputStream getInputStream(String path) throws IOException {
		Path zippath = fs.getPath(path);
		return Files.newInputStream(zippath);
	}
	
}
