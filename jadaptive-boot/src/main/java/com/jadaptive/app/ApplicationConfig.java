package com.jadaptive.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.jadaptive.app.json.upload.UploadServlet;
import com.jadaptive.app.scheduler.LockableTaskScheduler;
import com.jadaptive.utils.FileUtils;

@Configuration
@ComponentScan({ "com.jadaptive.app.**", "com.jadaptive.api.**" })
@ServletComponentScan("com.jadaptive.app.**")
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

	static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

	SpringPluginManager pluginManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public SpringPluginManager pluginManager() {

		Set<String> disabledPlugins;
		Map<String, Collection<String>> repositories;
		File repositoriesFile = new File("repositories");
		Path pluginRoot = Paths.get("./plugins");
		
		try {
			repositories = getConfiguration(repositoriesFile);
			disabledPlugins = new HashSet<>();
			if (repositories.containsKey("Disable")) {
				disabledPlugins.addAll(repositories.get("Disable"));
			}

			if (Boolean.getBoolean("jadaptive.development") && !repositories.containsKey("AppBuilder")) {
				throw new IllegalStateException(
						"AppBuilder directive required in repositories file with valid path to jadaptive-app-builder project");
			}

			if (repositories.containsKey("AppBuilder")) {
				String path = repositories.get("AppBuilder").iterator().next();
				pluginRoot = Paths.get(FileUtils.checkEndsWithSlash(path) + "plugins");
			}
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
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

				if (repositories.containsKey("PluginPath")) {
					for (String pluginPath : repositories.get("PluginPath")) {
						pluginRepository.add(new SinglePluginRepository(Paths.get(pluginPath)));
					}
				}

				for (String path : repositories.get("GitPlugins")) {
					pluginRepository.add(new DevelopmentPluginRepository(Paths.get(path)) {
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


				pluginRepository.add(new JarPluginRepository(getPluginsRoot()), this::isNotDevelopment);
				pluginRepository.add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);

				return pluginRepository;
			}
		};

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