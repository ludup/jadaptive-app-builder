package com.jadaptive.app.plugins.maven;

import java.net.URI;
import java.util.Optional;

import com.jadaptive.api.plugins.GAV;
import com.jadaptive.app.plugins.maven.Http.HttpClientFactory;


public interface Repository {
	

	public interface RepositoryBuilder<BLDR extends RepositoryBuilder<?, REPO>, REPO> {
		
		String id();

		BLDR withName(String name);

		BLDR withRoot(String root);
		
		REPO build();
	}

	public interface ResolutionResult {

		static ResolutionResult of(URI uri) {
			return new ResolutionResult() {

				@Override
				public URI uri() {
					return uri;
				}
			};
		}

		URI uri();
		
		default String filename() {
			var uri = uri().toString();
			var idx = uri.lastIndexOf('/');
			return idx == -1 ? uri : uri.substring(idx + 1);
		}
	}
		
		default boolean supported(GAV gav) {
			return gav.repositoryOr().map(r -> r.equals(id())).orElse(false);
		}

	Optional<ResolutionResult> resolve(HttpClientFactory httpFactory, GAV gav);

	String name();
	
	String id();

}
