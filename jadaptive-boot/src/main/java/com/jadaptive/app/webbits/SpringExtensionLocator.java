package com.jadaptive.app.webbits;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.Context;
import com.codesmith.webbits.Extension;
import com.codesmith.webbits.ExtensionLocator;
import com.jadaptive.api.db.ClassLoaderService;

@Component
public class SpringExtensionLocator implements ExtensionLocator {

    private List<Class<?>> objs = new LinkedList<>();

    @Autowired
    private ClassLoaderService classloaderService; 
    
    @Override
    public Set<Class<?>> locate(String path) {
    	
    	buildObjectList();
    	
		Set<Class<?>> exts = new LinkedHashSet<>();
		if (path != null) {
		    for (Class<?> clazz : objs) {
			Extension pageAnnotation = clazz.getAnnotation(Extension.class);
	
			if (clazz.equals(pageAnnotation.extendsClass()))
			    exts.add(clazz);
			else {
			    String[] paths = pageAnnotation.extendsPatterns();
			    if (paths.length == 0) {
				paths = new String[] { "/" + clazz.getSimpleName() };
			    }
			    for (String pattern : paths) {
				if (path.matches(pattern)) {
				    exts.add(clazz);
				    break;
				}
			    }
			}
		    }
		}
		return exts;

    }

    private void buildObjectList() {
		if(objs.isEmpty()) {
			for(Class<?> clazz : classloaderService.resolveAnnotatedClasses(Extension.class)) {
				add(clazz);
			}
		}
		
	}

    protected void add(Class<? extends Object> clazz) {
		/*
		 * Determine if this is an extension, if it is, we ignore it on this pass.
		 */
		Extension pageAnnotation = clazz.getAnnotation(Extension.class);
		if (pageAnnotation != null) {
		    objs.add(clazz);
		}
    }

    @Override
    public Class<?> get(String path) {
		for (Class<?> clazz : objs)
		    if (clazz.getName().equals(path) || clazz.getSimpleName().equals(path))
			return clazz;
		return null;
    }

    @Override
    public void open(Context context) throws IOException {
    }

}
