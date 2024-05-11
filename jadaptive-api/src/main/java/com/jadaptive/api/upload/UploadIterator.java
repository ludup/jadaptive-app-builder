package com.jadaptive.api.upload;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadIterator implements Iterator<Upload> {

	static final Logger log = LoggerFactory.getLogger(UploadIterator.class);
	
	FileItemInputIterator iter;
	FileItemInput next = null;
	public UploadIterator(FileItemInputIterator iter, FileItemInput next) throws FileUploadException, IOException {
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
			} catch (IOException e) {
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
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		if(Objects.isNull(next)) {
			throw new NoSuchElementException();
		}
		
		return new Upload(next);
	}

}
