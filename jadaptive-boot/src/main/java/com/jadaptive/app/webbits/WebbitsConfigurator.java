package com.jadaptive.app.webbits;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.Configurator;
import com.codesmith.webbits.Context;
import com.codesmith.webbits.ExtensionLocator;
import com.codesmith.webbits.ObjectCreator;
import com.codesmith.webbits.ViewLocator;
import com.codesmith.webbits.WidgetLocator;

@Component
public class WebbitsConfigurator extends Configurator {

    @Autowired
    private WebbitsPostProcessor postProcessor;

    @Autowired
    private SpringViewLocator viewLocator;

    @Autowired
    private SpringExtensionLocator extensionLocator;

    @Autowired
    private SpringWidgetLocator widgetLocator;

    @Autowired
    private SpringObjectCreator springObjectCreator;

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
    protected void onInit(Context context) {
	postProcessor.setContext(context);
    }

}
