package com.jadaptive.api.ui.pages.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;

public abstract class ConfigurationPage extends AuthenticatedPage {

	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private ClassLoaderService classService;
	
	List<ConfigurationPageItem> annotatedItems = null;
	
	protected abstract boolean isSystem();
	
	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		
		Element el;
		document.selectFirst("#optionPages").appendChild(el = Html.div("row", "text-center"));
		
		var items = new ArrayList<>(applicationService.getBeans(ConfigurationPageItem.class));
		items.addAll(getDynamicConfigurationItems());
		
		for(ConfigurationPageItem optionPage : items) {
			
			if(optionPage.isSystem() == isSystem()) {
				el.appendChild(Html.div("col-md-3", "mt-5")
						.appendChild(Html.div().appendChild(Html.i(optionPage.getIconGroup(), "fa-2x", optionPage.getIcon())))
						.appendChild(new Element("a")
							.attr("href", optionPage.getPath())
							.appendChild(Html.i18n(optionPage.getBundle(), optionPage.getResourceKey() + ".name")))
						.appendChild(new Element("p")
								.addClass("text-muted")
								.appendChild(Html.i18n(optionPage.getBundle(), optionPage.getResourceKey() + ".desc"))));
			}
		}
	}

	private Collection<ConfigurationPageItem> getDynamicConfigurationItems() {
		if(Objects.isNull(annotatedItems)) {
			
			annotatedItems = new ArrayList<>();
			for(Class<?> clz : classService.resolveAnnotatedClasses(ConfigurationItem.class)) {
				ConfigurationItem m = clz.getAnnotation(ConfigurationItem.class);
				if(Objects.nonNull(m)) {		
					if(m.system() == isSystem()) {
						String path = m.path();
						String bundle = m.bundle();
						String resourceKey = m.resourceKey();
						
						if(UUIDEntity.class.isAssignableFrom(clz)) {
							try {
								UUIDEntity e = (UUIDEntity) clz.getConstructor().newInstance();
								if(StringUtils.isBlank(resourceKey)) {
									resourceKey = e.getResourceKey();
								}
								if(StringUtils.isBlank(path)) {
									path = String.format("/app/ui/%s/", isSystem() ? "system" : "config") + resourceKey;
								}
								if(StringUtils.isBlank(bundle)) {
									bundle = e.getResourceKey();
								}
								
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
									| InvocationTargetException | NoSuchMethodException | SecurityException e) {
							}
						}
						annotatedItems.add(new DynamicConfigurationItem(m, resourceKey, path, bundle, isSystem()));
					}
				}
			}
		}
		return annotatedItems;
	}

}
