package com.jadaptive.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DependencyResolver.DependenciesNotFoundException;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.util.NameFileFilter;
import org.pf4j.util.OrFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.app.json.upload.UploadServlet;
import com.jadaptive.app.scheduler.LockableTaskScheduler;
import com.jadaptive.utils.FileUtils;

@Configuration
@ComponentScan({ "com.jadaptive.app.**", "com.jadaptive.api.**" })
@ServletComponentScan("com.jadaptive.app.**")
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

	@SuppressWarnings("serial")
	private final static class RetryException extends RuntimeException {}

	private final static String SNAPSHOT_EXTENSION_REPOSITORY = "https://artifactory.jadaptive.com/artifactory/libs-snapshots-local";
	private final static String RELEASE_EXTENSION_REPOSITORY = "https://artifactory.jadaptive.com/artifactory/libs-releases-local";
	
	private final class AppSpringPluginManager extends SpringPluginManager {
		private AppSpringPluginManager(Path[] pluginsRoots) {
			super(pluginsRoots);
		}

		@Override
		public void init() {
			try {
				super.init();
			}
			catch(DependenciesNotFoundException dnfe) {
				if(Boolean.getBoolean("jadaptive.development") || Boolean.getBoolean("jadaptive.autoDownloadMissingExtensions")) {
					downloadDeps(dnfe.getDependencies());
					throw new RetryException();
				}
				else
					throw dnfe;
			}
		}

		private void downloadDeps(Collection<String> dependencies) {

			var unobtainium = new HashSet<String>();
			var bootVersion = ArtifactVersion.getVersion("com.jadaptive", "jadaptive-boot");
			var repo = bootVersion.endsWith("-SNAPSHOT") ? SNAPSHOT_EXTENSION_REPOSITORY : RELEASE_EXTENSION_REPOSITORY;

			
			try {			
				/* TODO make a user for this, or make it public */
				var auth = new PasswordAuthentication("brett", "perissa9000-".toCharArray());
			
				for(var dep : dependencies) {
					var found = false;
					for(var groupId : new String[] { "com.logonbox", "com.jadaptive"}) {
						
						try {
							var filename = dep + "-" +  calcFullVersion(bootVersion, repo, groupId, dep, auth) + "-jadx.zip";
							var url = new URL(repo + "/" + 
									(groupId.replace('.', '/') + "/" + dep + 
									"/" + bootVersion + "/" + filename));

							var outf = Paths.get("plugins").resolve(filename);
							
							try {
								while(true) {
									var urlc = (HttpURLConnection)url.openConnection();
									System.out.format("Retrieve %s %s from %s%n", dep, bootVersion, url);
									urlc.setAuthenticator(new Authenticator() {
										@Override
										protected PasswordAuthentication getPasswordAuthentication() {
											return auth;
										}
									});
									urlc.setInstanceFollowRedirects(true);
									urlc.setAllowUserInteraction(true);
									
									var rc = urlc.getResponseCode();
									if(rc == 200) {
										try(var in = urlc.getInputStream()) {
											System.out.format("Downloading %s %s%n", dep, bootVersion);
											try(var out = Files.newOutputStream(outf)) {
												in.transferTo(out);
											}
										}
										break;
									}
									else if(rc == 301 || rc == 302) {
										try {
											url = new URL(urlc.getHeaderField("Location"));
										}
										catch(MalformedURLException m) {
											url = new URL(url, urlc.getHeaderField("Location"));
										}
									}
									else {
										throw new IOException("Unexpected response code " + rc);
									}
								}
								
								found = true;
								break;
							}
							catch(Exception e) {
								System.out.format("Cannot download %s-%s. %s%n", dep, bootVersion, e.getMessage());
							}
							
						}
						catch(MalformedURLException murle) {
							throw murle;
						}
						catch(Exception ioe) {
//							ioe.printStackTrace(System.out);
						}
					}
					
					if(found) {
						System.out.format("Downloaded %s-%s%n", dep, bootVersion);
					}
					else {
						System.out.format("Cannot download %s-%s. %n", dep, bootVersion);
						unobtainium.add(dep);
					}
				}
				
				if(unobtainium.size() > 0) {
					throw new DependenciesNotFoundException(new ArrayList<>(unobtainium));
				}
			}
			catch(MalformedURLException murle) {
				throw new IllegalStateException(murle);
			} 
		}

		private String calcFullVersion(String bootVersion, String repo, String groupId, String dep, PasswordAuthentication auth) throws IOException, ParserConfigurationException, SAXException {

			var metaurl = new URL(repo + "/" + 
					(groupId.replace('.', '/') + "/" + dep + 
					"/" + bootVersion + "/maven-metadata.xml"));
			var docBuilderFactory = DocumentBuilderFactory.newInstance();
			var docBuilder = docBuilderFactory.newDocumentBuilder();
			
			while(true) {
				var urlc = (HttpURLConnection)metaurl.openConnection();
				urlc.setAuthenticator(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return auth;
					}
				});
				urlc.setInstanceFollowRedirects(true);
				urlc.setAllowUserInteraction(true);
				var rc = urlc.getResponseCode();
				if(rc == 200) {
				
					try(var in = urlc.getInputStream()) {
						System.out.format("Downloading meta for %s %s%n", dep, bootVersion);
						var doc = docBuilder.parse(in);
						var snapshot = (Element)doc.getElementsByTagName("snapshot").item(0);
						var fullVersion = bootVersion.replace("-SNAPSHOT", "-" + 
								((Element)snapshot.getElementsByTagName("timestamp").item(0)).getTextContent() + 
								"-" + 
								((Element)snapshot.getElementsByTagName("buildNumber").item(0)).getTextContent()
								);
		
						System.out.format("Full version for %s %s is %s%n", dep, bootVersion, fullVersion);
						
						return fullVersion;
					}
				}
				else if(rc == 301 || rc == 302) {
					try {
						metaurl = new URL(urlc.getHeaderField("Location"));
					}
					catch(MalformedURLException m) {
						metaurl = new URL(metaurl, urlc.getHeaderField("Location"));
					}
				}
				else {
					throw new IOException("Unexpected response code " + rc);
				}
			}
		}

		@Override
		protected ExtensionFinder createExtensionFinder() {
			return new ScanningExtensionFinder(this);
		}

		@Override
		protected ExtensionFactory createExtensionFactory() {
			return new CustomSpringExtensionFactory(this, true);
		}

		@Override
		protected PluginRepository createPluginRepository() {

			CompoundPluginRepository pluginRepository = new CompoundPluginRepository();

			if(Boolean.getBoolean("jadaptive.development")) {
				if (repositories.containsKey("PluginPath")) {
					for (String pluginPath : repositories.get("PluginPath")) {
						pluginRepository.add(new SinglePluginRepository(Paths.get(pluginPath)));
					}
				}
				
				Collection<String> enabled = repositories.get("Enable");
				
				for (String path : repositories.get("GitPlugins")) {
					
					Path pluginsPath;
					if(Objects.nonNull(repoBase)) {
						pluginsPath = repoBase.resolve(path);
					} else {
						pluginsPath = Paths.get(path);
					}
					
					if(Objects.nonNull(enabled)) {
						try (DirectoryStream<Path> paths = Files.newDirectoryStream(pluginsPath, Files::isDirectory)) {
				            for (Path dir : paths) {
				                disabledPlugins.add(dir.getFileName().toString());
				            }
				        } catch (IOException e) {
				        }
						
						try (DirectoryStream<Path> paths = Files.newDirectoryStream(getPluginsRoot(), Files::isDirectory)) {
				            for (Path dir : paths) {
				                disabledPlugins.add(dir.getFileName().toString());
				            }
				        } catch (IOException e) {
				        }
						
						disabledPlugins.removeAll(enabled);
					}
					
					pluginRepository.add(new DevelopmentPluginRepository(pluginsPath) {
						protected FileFilter createHiddenPluginFilter() {
							OrFileFilter hiddenPluginFilter = (OrFileFilter) super.createHiddenPluginFilter();

							for (String id : disabledPlugins) {
								hiddenPluginFilter.addFileFilter(new NameFileFilter(id));
							}

							return hiddenPluginFilter;
						}
					});
				}

				pluginRepository.add(new DevelopmentPluginRepository(getPluginsRoot()) {
					protected FileFilter createHiddenPluginFilter() {
						OrFileFilter hiddenPluginFilter = (OrFileFilter) super.createHiddenPluginFilter();

						for (String id : disabledPlugins) {
							hiddenPluginFilter.addFileFilter(new NameFileFilter(id));
						}

						return hiddenPluginFilter;
					}
				}, this::isDevelopment);
			}

			pluginRepository.add(new JarPluginRepository(getPluginsRoot()), this::isNotDevelopment);
			pluginRepository.add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);

			return pluginRepository;
		}
	}

	static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

	SpringPluginManager pluginManager;
	Path repoBase = null;
	Set<String> disabledPlugins;
	Map<String, Collection<String>> repositories = null;
	File repositoriesFile = new File("repositories");
	Path pluginRoot = Paths.get("./plugins");
	
	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public SpringPluginManager pluginManager() {

		if(Boolean.getBoolean("jadaptive.development")) {
		
			try {
				repositories = getConfiguration(repositoriesFile);
				disabledPlugins = new HashSet<>();
				
				if (repositories.containsKey("Disable")) {
					disabledPlugins.addAll(repositories.get("Disable"));
				}
	
				if (!repositories.containsKey("AppBuilder") && !repositories.containsKey("GitBase")) {
					throw new IllegalStateException("Absolute path required in AppBuilder or GitBase configuration");
				}
	
				
				String path = "jadaptive-app-builder";
				if(repositories.containsKey("AppBuilder")) {
					path = repositories.get("AppBuilder").iterator().next();
				}
				
				pluginRoot = Paths.get(FileUtils.checkEndsWithSlash(path) + "plugins");
				
				if(repositories.containsKey("GitBase")) {
					repoBase = Paths.get(repositories.get("GitBase").iterator().next());
				}
				
				if(!pluginRoot.isAbsolute() && Objects.isNull(repoBase)) {
					throw new IllegalStateException("AppBuilder requires absolute path if no GitBase property has been provided");
				}
				if(!pluginRoot.isAbsolute()) {
					pluginRoot = repoBase.resolve(pluginRoot);
				} else if(Objects.isNull(repoBase) && pluginRoot.isAbsolute()) {
					repoBase = pluginRoot.getParent().getParent();
				}
				
			} catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		while(true) {
			try {
				pluginManager = new AppSpringPluginManager(new Path[] { pluginRoot });
				break;
			}
			catch(RetryException re) {}
		}

		return pluginManager;
	}

	@Bean
	public LockableTaskScheduler taskScheduler() {
		return new LockableTaskScheduler();
	}

	@PreDestroy
	public void cleanup() {
		pluginManager.stopPlugins();
	}

	@Bean
	public ServletRegistrationBean<?> uploadServletBean() {
		UploadServlet servlet = new UploadServlet();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(servlet);
		ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(servlet, "/upload/*");
		bean.setLoadOnStartup(1);
		return bean;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();

		pspc.setProperties(ApplicationProperties.getProperties());

		return pspc;
	}

	Map<String, Collection<String>> getConfiguration(File file) throws FileNotFoundException, IOException {

		var results = new HashMap<String, Collection<String>>();

		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String key = StringUtils.substringBefore(line, " ").trim();
					if (key.startsWith("#")) {
						continue;
					}
					String value = StringUtils.substringAfter(line, " ").trim();
					if (StringUtils.isNotBlank(value)) {
						if (!results.containsKey(key)) {
							results.put(key, new ArrayList<>());
						}
						results.get(key).add(value);
					}
				}
			}
		}

		return results;

	}

}