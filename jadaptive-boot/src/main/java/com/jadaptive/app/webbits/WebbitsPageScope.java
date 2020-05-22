package com.jadaptive.app.webbits;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.codesmith.webbits.ViewInfo;
import com.codesmith.webbits.ViewManager;

public class WebbitsPageScope implements Scope {
    public static final String ID = "page";

    final static String PAGE_SCOPE_BEANS = "pageScopeBeans";
    final static String PAGE_SCOPE_DESTRUCTION_CALLBACKS = "pageScopeDestructionCallbacks";

    private Map<String, Runnable> destructionCallbacks = Collections.synchronizedMap(new HashMap<String, Runnable>());

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
//		
	ViewInfo webbitsScope = ViewManager.get();
	Map<String, Object> scopedObjects = webbitsScope.getAttribute(PAGE_SCOPE_BEANS);
	if (scopedObjects == null) {
	    scopedObjects = Collections.synchronizedMap(new HashMap<String, Object>());
	    webbitsScope.setAttribute(PAGE_SCOPE_BEANS, scopedObjects);
	}
	if (!scopedObjects.containsKey(name)) {
	    scopedObjects.put(name, objectFactory.getObject());
	}
	return scopedObjects.get(name);
    }

    @Override
    public Object remove(String name) {
	destructionCallbacks.remove(name);
	ViewInfo webbitsScope = ViewManager.get();
	Map<String, Object> scopedObjects = webbitsScope.getAttribute(PAGE_SCOPE_BEANS);
	if (scopedObjects == null)
	    return null;
	else
	    return scopedObjects.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
	// TODO actually calll the callbacks when the page is destroyed
	ViewInfo webbitsScope = ViewManager.get();
	Map<String, Object> destructionCallbacks = webbitsScope.getAttribute(PAGE_SCOPE_DESTRUCTION_CALLBACKS);
	if (destructionCallbacks == null) {
	    destructionCallbacks = Collections.synchronizedMap(new HashMap<String, Object>());
	    webbitsScope.setAttribute(PAGE_SCOPE_DESTRUCTION_CALLBACKS, destructionCallbacks);
	}
	destructionCallbacks.put(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
	ViewInfo webbitsScope = ViewManager.get();
	while (webbitsScope != null) {
	    if (webbitsScope.getAttributes().containsKey(key))
		return webbitsScope.getAttribute(key);
	    webbitsScope = webbitsScope.parent();
	}
	return null;
    }

    @Override
    public String getConversationId() {
	return ViewManager.get().root().uuid().toString();
    }
}
