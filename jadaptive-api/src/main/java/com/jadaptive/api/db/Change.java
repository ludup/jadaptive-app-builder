package com.jadaptive.api.db;

import java.util.Optional;

import com.jadaptive.api.repository.UUIDDocument;

public interface Change<T extends UUIDDocument> {
	public enum Type {
	    INSERT, UPDATE, REPLACE, DELETE, INVALIDATE, DROP, DROP_DATABASE, RENAME, OTHER
	}
	
	String uuid();
	
	Type type();
	
	Optional<T> document();
}