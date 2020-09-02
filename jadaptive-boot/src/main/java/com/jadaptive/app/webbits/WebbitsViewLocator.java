package com.jadaptive.app.webbits;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.App;
import com.codesmith.webbits.ClassMatch;
import com.codesmith.webbits.Context;
import com.codesmith.webbits.ExceptionHandler;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.ViewLocator;
import com.codesmith.webbits.ViewPaths;
import com.codesmith.webbits.util.Annotations;
import com.jadaptive.api.db.ClassLoaderService;

@Component
public class WebbitsViewLocator implements ViewLocator {

	static Logger log = LoggerFactory.getLogger(WebbitsViewLocator.class);
	
	@Autowired
    private ClassLoaderService classloaderService; 
	
    private ViewPaths vp = null;
    private Set<Class<?>> exceptionHandlers = new LinkedHashSet<>();
    private App app;


    private synchronized void buildViewLists() {
    	
    	if(Objects.isNull(vp)) {
    
    		vp = new ViewPaths();
    		for(Class<?> clazz : classloaderService.resolveAnnotatedClasses(Page.class)) {
			    if (Annotations.hasAnyClassAnnotations(clazz, Page.class)) {
				vp.put(clazz);
				
				log.info("Loaded view {}", clazz.getSimpleName());
				ExceptionHandler eh = clazz.getAnnotation(ExceptionHandler.class);
				if (eh != null)
				    exceptionHandlers.add(clazz);
			    }
		
			    App app = clazz.getAnnotation(App.class);
			    if (app != null) {
				this.app = app;
			    }
			}
		}
    }

    @Override
    public void open(Context context) throws IOException {
    }

    @Override
    public ClassMatch locate(String path) {
    	buildViewLists();
    	return vp.locate(path);
    }

    @Override
    public Set<Class<?>> exceptionHandlers() {
	return exceptionHandlers;
    }

    @Override
    public App app() {
	return app;
    }
}
