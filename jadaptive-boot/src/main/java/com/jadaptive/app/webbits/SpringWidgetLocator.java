package com.jadaptive.app.webbits;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.Context;
import com.codesmith.webbits.Widget;
import com.codesmith.webbits.WidgetLocator;
import com.jadaptive.api.db.ClassLoaderService;

@Component
public class SpringWidgetLocator implements WidgetLocator {

	static Logger log = LoggerFactory.getLogger(SpringViewLocator.class);
	
	@Autowired
    private ClassLoaderService classloaderService; 
    
    private Map<String, Class<?>> widgetsByClassifier = new HashMap<>();
    private Map<String, Class<?>> widgetsById = new HashMap<>();

    @Override
    public Class<?> locate(String path) {
    	buildWidgetLists();
    	return widgetsById.get(path);
    }

    @Override
    public Class<?> locateByClassifier(String classifier) {
    	
    	buildWidgetLists();
    	return widgetsByClassifier.get(classifier);
    }

    private void buildWidgetLists() {

    	if(widgetsByClassifier.isEmpty()) {
    		for(Class<?> clazz : classloaderService.resolveAnnotatedClasses(Widget.class)) {
    			add(clazz);
    		}
    	}
    }

    protected void add(Class<? extends Object> clazz) {

		Widget widget = clazz.getAnnotation(Widget.class);
		if (widget == null)
		    return;
	
		String classifier = widget.classifier();
		if (StringUtils.isBlank(classifier)) {
		    classifier = clazz.getSimpleName();
		}
	
		if (widgetsByClassifier.containsKey(classifier))
		    throw new IllegalStateException(
			    String.format("Widget %s has a classifier of %s, which already exists.", clazz, classifier));
		widgetsByClassifier.put(classifier, clazz);
		log.info("Loaded Widget {}", clazz.getSimpleName());
		String id = widget.id();
		if (StringUtils.isBlank(id)) {
		    id = clazz.getName();
		}
		if (widgetsById.containsKey(id)) {
		    if (StringUtils.isBlank(widget.classifier())) {
		    	throw new IllegalStateException(
		    			String.format("Widget %s has a classifier of %s, which already exists.", clazz, classifier));
		    }
		} else
		    widgetsById.put(id, clazz);

    }

    @Override
    public void open(Context context) throws IOException {
    }

}
