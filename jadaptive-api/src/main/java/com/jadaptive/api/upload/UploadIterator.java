package com.jadaptive.api.upload;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadIterator implements Iterator<Upload> {

	static final Logger log = LoggerFactory.getLogger(UploadIterator.class);
	
	FileItemIterator iter;
	FileItemStream next = null;
	public UploadIterator(FileItemIterator iter, FileItemStream next) throws FileUploadException, IOException {
		this.iter = iter;
		this.next = next;
	}
	
	@Override
	public boolean hasNext() {
		if(Objects.nonNull(next)) {
			return true;
		} else {
			try {
				return iter.hasNext();
			} catch (FileUploadException | IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
	}

	@Override
	public Upload next() {
		if(Objects.nonNull(next)) {
			try {
			return new Upload(next);
			} finally {
				next = null;
			}
		}
		next = null;
		try {
			while(iter.hasNext()) {
				next = iter.next();
				if(next.isFormField()) {
					log.warn("Unexpected form field in UploadIterator");
					next = null;
					continue;
				}
				if(StringUtils.isBlank(next.getName())) {
					log.warn("Unexpected empty filename in UploadIterator");
					next = null;
					continue;
				}
				break;
			}
		} catch (FileUploadException | IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		if(Objects.isNull(next)) {
			throw new NoSuchElementException();
		}
		
		return new Upload(next);
	}

}
