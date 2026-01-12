package com.jadaptive.app.plugins;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.plugins.GAV;
import com.jadaptive.api.plugins.PluginManagerService;
import com.jadaptive.app.plugins.maven.Http.HttpClientFactory;
import com.jadaptive.app.plugins.maven.MavenRemoteRepositoryImpl.RemoteRepositoryBuilder;

@Service
public class PluginManagerServiceImpl implements PluginManagerService {

	static Logger LOG = LoggerFactory.getLogger(PluginManagerServiceImpl.class);

	private String repositoryUrl = "https://artifactory.jadaptive.com/libs-snapshots-local";
	private String repositoryUsername;
	private char[] repositoryPassword;

	public PluginManagerServiceImpl() {
	}

	@Override
	public Path installOrUpdate(String groupId, String artifactId) throws IOException {

		var gav = gav(groupId, artifactId);
		var repoBldr = new RemoteRepositoryBuilder().withRoot(repositoryUrl);
		var repo = repoBldr.build();
		var fact = httpClientFactory();

		var resolvedResult = repo.resolve(fact, gav);
		if (resolvedResult.isPresent()) {

			var resolved = resolvedResult.get();
			var pluginsDir = Paths.get("plugins");
			Files.createDirectories(pluginsDir);
			var pluginZip = pluginsDir.resolve(resolved.filename());
			if (Files.exists(pluginZip))
				throw new IllegalStateException(gav + " is already installed.");

			LOG.info("Downloading from {}", resolved.uri());
			
			/* Download new extension, but only delete old ones the download
			 * fails for any reason
			 */
			try {
				var in = repo.download(fact, gav, resolved, Optional.empty());
				try (var out = Files.newOutputStream(pluginZip)) {
					in.transferTo(out);
				}
				
				try(var str = Files.newDirectoryStream(pluginsDir, extensionFilter())) {
					for(var pdir : str) {
						if(!pdir.getFileName().toString().equals(resolved.filename()) &&
							isFileForArtifact(artifactId, pdir)) {

							LOG.info("This is an upgrade, deleting other version {}", pdir);
							recursiveDelete(pdir);
							var dir = PluginManagerService.expandedDirectoryForZipFile(pdir);
							if(Files.exists(dir)) {
								recursiveDelete(dir);
							}
						}
					}
				}
				

				var dir = PluginManagerService.expandedDirectoryForZipFile(pluginZip);
				if(Files.exists(dir)) {
					recursiveDelete(dir);
				}
			}
			catch(Exception ex) {
				Files.delete(pluginZip);
				if(ex instanceof RuntimeException re) {
					throw re;
				}
				else if(ex instanceof IOException ioe) {
					throw ioe;
				}
				else {
					throw new IOException("Failed to download.", ex);
				}
			}

			return pluginZip;

		} else {
			throw new IllegalArgumentException("No results found for " + groupId + ":" + artifactId);
		}
	}

	@Override
	public boolean installed(String groupId, String artifactId) throws IOException {
		var pluginsDir = Paths.get("plugins");
		if(Files.exists(pluginsDir)) {
			return findArtifact(artifactId, pluginsDir).isPresent();
		}
		return false;
	}

	@Override
	public boolean isUpdateable(String groupId, String artifactId) throws IOException {

		var gav = gav(groupId, artifactId);
		var repoBldr = new RemoteRepositoryBuilder().withRoot(repositoryUrl);
		var repo = repoBldr.build();
		var fact = httpClientFactory();

		var resolvedResult = repo.resolve(fact, gav);
		if (resolvedResult.isPresent()) {
			var resolved = resolvedResult.get();
			var pluginsDir = Paths.get("plugins");
			Files.createDirectories(pluginsDir);
			var pluginZip = pluginsDir.resolve(resolved.filename());
			if (Files.exists(pluginZip)) {
				return false;
			}
			else {
				return findArtifact(artifactId, pluginsDir).isPresent();
			}
		}
		else
			throw new IllegalArgumentException("No results found for " + groupId + ":" + artifactId);
	}

	@Override
	public void setRepository(String url, String username, char[] password) {
		LOG.info("Setting Maven repository URL for extensions to {}", url);
		this.repositoryUrl = url;
		this.repositoryUsername = username;
		this.repositoryPassword = password;
	}

	private HttpClientFactory httpClientFactory() {
		HttpClientFactory fact = new HttpClientFactory() {
			@Override
			public Builder get() {
				return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
						.authenticator(new Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								/* TODO need to replace this */
								if (repositoryUsername == null || repositoryPassword == null)
									throw new IllegalStateException(
											"No username or password set for maven repository authentication.");
								return new PasswordAuthentication(repositoryUsername, repositoryPassword);
							}
						}).version(HttpClient.Version.HTTP_1_1);
			}
		};
		return fact;
	}

	private Optional<Path> findArtifact(String artifactId, Path pluginsDir) throws IOException {
		try(var str = Files.newDirectoryStream(pluginsDir, extensionFilter())) {
			for(var f : str) {
				if(isFileForArtifact(artifactId, f)) {
					return Optional.of(f);
				}
			}
		}
		return Optional.empty();
	}

	private boolean isFileForArtifact(String artifactId, Path f) {
		return f.getFileName().toString().matches(Pattern.quote(artifactId + "-") + "[0-9]+\\..*");
	}

	private Filter<? super Path> extensionFilter() {
		return f -> !Files.isDirectory(f) && f.getFileName().toString().toLowerCase().endsWith("-jadx.zip");
	}

	private String calcVersion(boolean snapshot) {
		var version = ApplicationVersion.getVersion("jadaptive-boot");
		var idx = version.lastIndexOf('-');
		return idx == -1 ? version : version.substring(0, idx) + "-SNAPSHOT";
	}

	private GAV gav(String groupId, String artifactId) {
		var gavBldr = new GAV.Builder();
		if (isBlank(groupId)) {
			gavBldr.withGroupId("com.jadaptive");
		} else {
			gavBldr.withGroupId(groupId);
		}
		gavBldr.withArtifactId(artifactId);
		gavBldr.withClassifier("jadx");
		gavBldr.withType("zip");
		gavBldr.withVersion(calcVersion(true));
		var gav = gavBldr.build();
		return gav;
	}
	
	private static void recursiveDelete(Path fileOrDirectory, FileVisitOption... options) {
		try (var walk = Files.walk(fileOrDirectory, options)) {
			walk.sorted(Comparator.reverseOrder()).forEach(p -> {
				try {
					Files.delete(p);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} 
		catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
}
