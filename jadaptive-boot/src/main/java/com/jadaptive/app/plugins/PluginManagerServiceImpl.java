package com.jadaptive.app.plugins;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

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
	public Path install(String groupId, String artifactId) throws IOException {

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
			var in = repo.download(fact, gav, resolved, Optional.empty());
			try (var out = Files.newOutputStream(pluginZip)) {
				in.transferTo(out);
			}

			return pluginZip;

		} else {
			throw new IllegalArgumentException("No results found.");
		}
	}

	@Override
	public boolean installed(String groupId, String artifactId) throws IOException {

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
				return true;

		}

		return false;
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
}
