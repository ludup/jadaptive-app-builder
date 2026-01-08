package com.jadaptive.app.plugins.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.jadaptive.api.plugins.GAV;
import com.jadaptive.app.plugins.maven.Http.HttpClientFactory;


public final class MavenRemoteRepositoryImpl implements RemoteRepository {
	final static Logger LOG = LoggerFactory.getLogger(MavenRemoteRepositoryImpl.class);

	public final static class RemoteRepositoryBuilder implements RemoteRepository.RemoteRepositoryBuilder {
		private URI root = URI.create("https://repo1.maven.org/maven2");
		private String name = "Remote Repository";

		@Override
		public RemoteRepositoryBuilder withName(String name) {
			this.name = name;
			return this;
		}

		@Override
		public RemoteRepositoryBuilder withRoot(String root) {
			return withRoot(URI.create(root));
		}

		@Override
		public RemoteRepositoryBuilder withRoot(URI root) {
			this.root = root;
			return this;
		}

		@Override
		public RemoteRepository build() {
			return new MavenRemoteRepositoryImpl(this);
		}

		@Override
		public String id() {
			return "central";
		}
	}

	private final URI root;
	private final String name;
	private final String id;

	public MavenRemoteRepositoryImpl(RemoteRepositoryBuilder builder) {
		this.root = builder.root;
		this.name = builder.name;
		this.id = builder.id();
	}

	@Override
	public boolean supported(GAV gav) {
		return gav.repositoryOr().isEmpty() || gav.repository().equals(id());
	}

	@Override
	public Optional<ResolutionResult> resolve(HttpClientFactory factory, GAV gav) {
		if(gav.isSnapshot()) {
			/* Need meta-data to get actual snapshot version */
			var httpClient = factory.get().build();

			var uri = URI.create(root.toString() + '/' + dottedToPath(gav.groupId()) + '/'
					+ gav.artifactId() + '/' + gav.version() + '/' + "maven-metadata.xml");
			
			LOG.info("Looking for meta-data from {}", uri);
			
			var request = HttpRequest.newBuilder().GET().uri(uri).build();
			var handler = HttpResponse.BodyHandlers.ofInputStream();
			
			try {
				var response = httpClient.send(request, handler);
				switch (response.statusCode()) {
				case 200:
					var xml = response.body();
					var docBuilderFactory = DocumentBuilderFactory.newInstance();
					var docBuilder = docBuilderFactory.newDocumentBuilder();
					var doc = docBuilder.parse(xml);

					var els = doc.getDocumentElement().getElementsByTagName("snapshotVersion");
					for(int i = 0 ; i < els.getLength(); i++) {
						var varEl = els.item(i);
						if(varEl instanceof Element) {
							var el = (Element)varEl;
							var sclassifierEl = el.getElementsByTagName("classifier").item(0);
							var sclassifier = sclassifierEl == null ? "" : sclassifierEl.getTextContent();
							var stypeEl = el.getElementsByTagName("extension").item(0);
							var stype = stypeEl == null ? "" : stypeEl.getTextContent();
							var sval = el.getElementsByTagName("value").item(0).getTextContent();
							if(sclassifier.equals(gav.classifierOr().orElse("")) &&
							   stype.equals(gav.typeOr().orElse("jar"))) {
								var newGav = gav.toResolvedSnapshotVersion(sval);
								LOG.info("Resolved {} as {}", gav, newGav);
								gav = newGav;
							}
						}
					}
					break;
				case 404:
					throw new NoSuchFileException(uri.toString());
				default:
					throw new IOException("Unexpected status " + response.statusCode());
				}
			} catch (InterruptedException | ParserConfigurationException | SAXException e) {
				throw new IllegalStateException(e);
			} catch(IOException ioe) {
				throw new UncheckedIOException(ioe);
			}
			
		}

		var uri = URI.create(root.toString() + '/' + dottedToPath(gav.groupId()) + '/'
				+ gav.artifactId() + '/' + gav.version() + '/' + gav.artifactId() + "-" + 
				gav.resolvedVersion() + gav.classifierOr().map(c -> "-" + c).orElse("") + "." + (gav.typeOr().orElse("jar")));
		
		return Optional.of(ResolutionResult.of(uri));
	}

	static String dottedToPath(String dotted) {
		return dotted.replace('.', File.separatorChar);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public InputStream download(HttpClientFactory httpClientFactory, GAV gav, ResolutionResult result, Optional<ResolutionMonitor> monitor) throws IOException {
		var httpClient = httpClientFactory.get().build();
		var uri = result.uri();
		var request = HttpRequest.newBuilder().GET().uri(uri).build();
		var handler = HttpResponse.BodyHandlers.ofInputStream();
		try {
			var response = httpClient.send(request, handler);
			switch (response.statusCode()) {
			case 200:
				monitor.ifPresent(m -> m.found(gav, uri, this, Http.contentLength(response)));
				return response.body();
			case 404:
				throw new NoSuchFileException(uri.toString());
			default:
				throw new IOException("Unexpected status " + response.statusCode() + " for " + uri);
			}
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String id() {
		return id;
	}
}
