package com.jadaptive.api.json;

import java.util.Collection;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.session.UnauthorizedException;

public interface BootstrapTablePageProcessor {
	
	Collection<?> getPage(String searchColumn, String searchPattern, int start, int length, String sortBy) throws UnauthorizedException, AccessDeniedException;
	
	Long getTotalCount(String searchColumn, String searchPattern) throws UnauthorizedException, AccessDeniedException;

}
