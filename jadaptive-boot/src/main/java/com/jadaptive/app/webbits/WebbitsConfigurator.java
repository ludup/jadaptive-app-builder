package com.jadaptive.app.webbits;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.ClasspathLocator;
import com.codesmith.webbits.Configurator;
import com.codesmith.webbits.Context;
import com.codesmith.webbits.DefaultClasspathLocator;
import com.codesmith.webbits.ExtensionLocator;
import com.codesmith.webbits.ObjectCreator;
import com.codesmith.webbits.ViewLocator;
import com.codesmith.webbits.WidgetLocator;
import com.codesmith.webbits.i18n.BundleResolver;
import com.jadaptive.utils.FileUtils;

@Component
public class WebbitsConfigurator extends Configurator {

    @Autowired
    private WebbitsPostProcessor postProcessor;

    @Autowired
    private WebbitsViewLocator viewLocator;

    @Autowired
    private WebbitsExtensionLocator extensionLocator;

    @Autowired
    private WebbitsWidgetLocator widgetLocator;

    @Autowired
    private WebbitsObjectCreator springObjectCreator;

    @Autowired
    private WebbitsBundleResolver webbitsBundleResolver; 
    
    @Autowired
    private PluginManager pluginManager; 
    
    @Override
    public ViewLocator createViewLocator() {
	return viewLocator;
    }

    @Override
    public ExtensionLocator createExtensionLocator() {
	return extensionLocator;
    }

    @Override
    public ObjectCreator createObjectCreator() {
	return springObjectCreator;
    }

    @Override
    public WidgetLocator createWidgetLocator() {
	return widgetLocator;
    }

    
    @Override
	public BundleResolver createBundleResolver() {
		return webbitsBundleResolver;
	}

	@Override
    protected void onInit(Context context) {
	postProcessor.setContext(context);
    }
    
    @Override
    public ClasspathLocator createClasspathLocator() {
    	return new DefaultClasspathLocator() {

    		Map<String,ClassLoader> cachedLoaders = new HashMap<>();
    		
			@Override
			public URL getResource(String path) {
				
				String packageName = FileUtils.checkStartsWithNoSlash(
						FileUtils.checkEndsWithNoSlash(
								FileUtils.stripLastPathElement(path)));
				packageName = packageName.replace('/', '.');
				ClassLoader classLoader = cachedLoaders.get(packageName);
				URL url = null;
				if(Objects.isNull(classLoader)) {
					if(packageName.startsWith("com.jadaptive.plugins")) {
						for(PluginWrapper w : pluginManager.getPlugins()) {
							Plugin plugin = w.getPlugin();
							if(plugin!=null && packageName.startsWith(plugin.getClass().getPackage().getName())) {
								cachedLoaders.put(packageName, w.getPluginClassLoader());
								url=  w.getPluginClassLoader().getResource(path);
								break;
							}
						}
					}
				} else {
					url = classLoader.getResource(path);
				}
				
				if(Objects.isNull(url)) {
					url = super.getResource(path);
				}
				return url;
			}
    	};
    }

}
