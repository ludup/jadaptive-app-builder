package com.jadaptive.app.plugins.maven;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import com.jadaptive.api.plugins.GAV;
import com.jadaptive.app.plugins.maven.Http.HttpClientFactory;

public interface RemoteRepository extends Repository {
	public interface RemoteRepositoryBuilder extends Repository.RepositoryBuilder<RemoteRepositoryBuilder, RemoteRepository> {
		RemoteRepository build();

		RemoteRepositoryBuilder withRoot(URI root);
	}

	InputStream download(HttpClientFactory httpClient, GAV gav, ResolutionResult result, Optional<ResolutionMonitor> monitor) throws IOException;

}
