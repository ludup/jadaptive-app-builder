package com.jadaptive.app.webbits;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptEngineManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.context.WebApplicationContext;

import com.codesmith.webbits.CacheFactory;
import com.codesmith.webbits.ContentHandler;
import com.codesmith.webbits.Context;
import com.codesmith.webbits.DefaultCacheFactory;
import com.codesmith.webbits.Extension;
import com.codesmith.webbits.ExtensionLocator;
import com.codesmith.webbits.MimeService;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Request;
import com.codesmith.webbits.Response;
import com.codesmith.webbits.ViewConfigurationProvider;
import com.codesmith.webbits.ViewLocator;
import com.codesmith.webbits.ViewManager;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.WidgetLocator;
import com.codesmith.webbits.i18n.BundleResolver;
import com.codesmith.webbits.util.Invoker;

import io.socket.engineio.server.EngineIoServer;

public class WebbitsPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private final Set<String> packagesToScan;
    private final Set<Class<?>> components = new HashSet<>();

    private ApplicationContext applicationContext;

    private Context context;

    WebbitsPostProcessor(Set<String> packagesToScan) {
	this.packagesToScan = packagesToScan;
    }

    public Set<Class<?>> getComponents() {
	return components;
    }

    private void scanPackage(ClassPathScanningCandidateComponentProvider componentProvider, String packageToScan) {
	for (BeanDefinition candidate : componentProvider.findCandidateComponents(packageToScan)) {
	    if (candidate instanceof AnnotatedBeanDefinition) {
		AnnotatedBeanDefinition c = (AnnotatedBeanDefinition) candidate;

		try {
		    Class<?> clazz = Class.forName(c.getBeanClassName());
		    components.add(clazz);
		} catch (ClassNotFoundException e) {
		    throw new IllegalStateException(
			    String.format("Could not load webbits component %s while scanning %s", c.getBeanClassName(),
				    packageToScan),
			    e);
		}
	    }
	}
    }

    private boolean isRunningInEmbeddedWebServer() {
	return this.applicationContext instanceof WebApplicationContext
		&& ((WebApplicationContext) this.applicationContext).getServletContext() == null;
    }

    private ClassPathScanningCandidateComponentProvider createComponentProvider() {
	ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
		false);
	componentProvider.setEnvironment(this.applicationContext.getEnvironment());
	componentProvider.setResourceLoader(this.applicationContext);
	componentProvider.addIncludeFilter(new AnnotationTypeFilter(Page.class));
	componentProvider.addIncludeFilter(new AnnotationTypeFilter(Extension.class));
	componentProvider.addIncludeFilter(new AnnotationTypeFilter(Widget.class));
	return componentProvider;
    }

    Set<String> getPackagesToScan() {
	return Collections.unmodifiableSet(this.packagesToScan);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	beanFactory.registerScope(WebbitsPageScope.ID, new WebbitsPageScope());

	/*
	 * Create a proxy for the context, as it won't be set yet until the servlet
	 * starts up
	 */
	beanFactory.registerSingleton("context", new Context() {
	    @Override
	    public void reload() {
		context.reload();
	    }

	    @Override
	    public void refreshByResource(String resource) {
		context.refreshByResource(resource);
	    }

	    @Override
	    public void refreshById(String id) {
		context.refreshById(id);
	    }

	    @Override
	    public void refreshByClassifier(String classifier) {
		context.refreshByClassifier(classifier);
	    }

	    @Override
	    public void refresh(Object widget) {
		context.refresh(widget);
	    }

	    @Override
	    public void redirect(String targetPath) {
		context.redirect(targetPath);
	    }

	    @Override
	    public void redirect(Class<?> target) {
		context.redirect(target);
	    }

	    @Override
	    public void processDependencies(Request<?> request, Response<?> response, Object root, Invoker invoker,
		    Class<?>... dependencies) {
		context.processDependencies(request, response, root, invoker, dependencies);
	    }

	    @Override
	    public WidgetLocator getWidgetLocator() {
		return context.getWidgetLocator();
	    }

	    @Override
	    public ViewLocator getViewLocator() {
		return context.getViewLocator();
	    }

	    @Override
	    public ViewConfigurationProvider getViewConfigurationProvider() {
		return context.getViewConfigurationProvider();
	    }

	    @Override
	    public ScriptEngineManager getScriptEngineManager() {
		return context.getScriptEngineManager();
	    }

	    @Override
	    public ViewManager getViewManager() {
		return context.getViewManager();
	    }

	    @Override
	    public String getInitParameter(String initParameter) {
		return context.getInitParameter(initParameter);
	    }

	    @Override
	    public ExtensionLocator getExtensionLocator() {
		return context.getExtensionLocator();
	    }

	    @Override
	    public List<ContentHandler<?, ?>> getContentHandlers() {
		return context.getContentHandlers();
	    }

	    @Override
	    public BundleResolver getBundleResolver() {
		return context.getBundleResolver();
	    }

	    @Override
	    public Response<?> get(String requestPath, Object root) throws IOException {
		return context.get(requestPath, root);
	    }

	    @Override
	    public <T> T create(Class<T> clazz, Object root) {
		return context.create(clazz, root);
	    }

	    @Override
	    public <T> T create(Class<T> clazz) {
		return context.create(clazz);
	    }

	    @Override
	    public MimeService getMimeService() {
		return context.getMimeService();
	    }

	    @Override
	    public EngineIoServer getIo() {
		return context.getIo();
	    }

		@Override
		public CacheFactory getCacheFactory() {
			return new DefaultCacheFactory();
		}
	});
	if (isRunningInEmbeddedWebServer()) {
	    ClassPathScanningCandidateComponentProvider componentProvider = createComponentProvider();
	    for (String packageToScan : this.packagesToScan) {
		scanPackage(componentProvider, packageToScan);
	    }
	}
    }

    public void setContext(Context context) {
	this.context = context;
    }

}
