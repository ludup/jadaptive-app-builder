package com.jadaptive.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
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

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.plugins.PluginManagerService;
import com.jadaptive.app.json.upload.UploadServlet;
import com.jadaptive.utils.FileUtils;

@Configuration
@ComponentScan({ "com.jadaptive.app.**", "com.jadaptive.api.**" })
@ServletComponentScan("com.jadaptive.app.**")
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

	static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

	private SpringPluginManager pluginManager;
	private Path repoBase = null;
	private Set<String> disabledPlugins;
	private Map<String, Collection<String>> repositories = null;
	private File repositoriesFile = new File("repositories");
	private Path pluginRoot = Paths.get("./plugins");
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private PluginManagerService pluginManagerService;

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
					path = Paths.get(repositories.get("AppBuilder").iterator().next()).toAbsolutePath().toString();
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
		
		pluginManager = new SpringPluginManager(pluginRoot) {

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
					
					Collection<String> mavenRepositories = repositories.getOrDefault("MavenRepository", Collections.emptyList());
					for(var maven : mavenRepositories) {
						var parts = maven.split("\\s+");
						pluginManagerService.setRepository(
							parts[0].trim(), 
							parts.length > 1 ? parts[1] : null, 
							parts.length > 2 ? parts[2].trim().toCharArray() : null
						);
						break;
					}

					Collection<String> installed = new ArrayList<String>();
					for (var path : repositories.getOrDefault("Install", Collections.emptyList())) {
						var parts = path.split(":");
						
						String group, artifact;
						
						if(parts.length == 1) {
							artifact = parts[0];
							if(artifact.startsWith("logonbox-")) {
								group = "com.logonbox";
							}
							else if(artifact.startsWith("jadaptive-")) {
								group = "com.jadaptive";
							}
							else if(artifact.startsWith("sshtools-")) {
								group = "com.sshtools";
							}
							else {
								throw new IllegalArgumentException("Extension `" + path + "` specifier invalid. Use <groupdId>:<artifactId>");
							}
						}
						else if(parts.length == 2) {
							group = parts[0];
							artifact = parts[1];
						}
						else {
							throw new IllegalArgumentException("Extension `" + path + "` specifier invalid. Use <groupdId>:<artifactId>");
						}
						
						try {
							if(!pluginManagerService.installed(group, artifact)) {
								pluginManagerService.install(group, artifact);
							}
							installed.add(artifact);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}

					Collection<String> enabled = 
							Stream.concat(repositories.getOrDefault("Enable", Collections.emptyList()).stream(), 
							installed.stream()).toList();
					
					for (String path : repositories.getOrDefault("GitPlugins", Collections.emptyList())) {
						
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
					
					log.info("Disabled plugins: {}", String.join(", ", disabledPlugins));
	
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

				pluginRepository.add(new JarPluginRepository(getPluginsRoot()), () -> {
					return isNotDevelopment() || Boolean.getBoolean("jadaptive.loadPluginArchives");
				});
				pluginRepository.add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);

				return pluginRepository;
			}
		};

		return pluginManager;
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