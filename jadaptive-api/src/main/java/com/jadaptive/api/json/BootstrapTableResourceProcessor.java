package com.jadaptive.api.json;

import java.io.IOException;

import com.jadaptive.api.permissions.AccessDeniedException;

public interface BootstrapTableResourceProcessor<T> extends BootstrapTablePageProcessor {

	T getResource() throws AccessDeniedException, IOException;
}
