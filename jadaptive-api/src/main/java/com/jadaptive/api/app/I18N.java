/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.jadaptive.api.app;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.servlet.Request;

public class I18N {

	static Logger log = LoggerFactory.getLogger(I18N.class);
	
	static Map<String,ResourceBundle> cachedBundles = new HashMap<>();
	static Map<String,String> dynamic = new HashMap<>();
	
	private I18N() {
	}

	public static String getResource(String bundle, String key, Object...arguments) {
		
		Locale locale = Locale.getDefault();
		if(Request.isAvailable()) {
			locale = Request.get().getLocale();
		}
		return getResource(locale, bundle, key, arguments);
	}
	
	public static Set<String> getResourceKeys(Locale locale,
			String resourceBundle) {
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle");
		}
		
		Set<String> keys = new HashSet<String>();
		
		if(resourceBundle.equals("dynamic")) {
			keys.addAll(dynamic.keySet());
		} else {
			String bundle = resourceBundle;
			bundle = "i18n/" + resourceBundle;
	
			
			try {
				ResourceBundle rb = cachedBundles.get(bundle);
				if(Objects.isNull(rb)) {
					rb = ResourceBundle.getBundle(bundle, locale,
							I18N.class.getClassLoader());
				}
				keys.addAll(rb.keySet());
			} catch (MissingResourceException e) {
					
			}
		} 

		return keys;
	}
	
	public static void overrideMessage(Locale locale, String key, String value) {
		dynamic.put(key, value);
	}
	
	public static void removeOverrideMessage(Locale locale, String key) {		
		dynamic.remove(key);
	}

	public static String getResourceOrException(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}

		if(resourceBundle.equals("dynamic")) {
			
			String localizedString = dynamic.get(key);
			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
			
		} else {
		
			String bundlePath = resourceBundle;
			bundlePath = "i18n/" + resourceBundle;
			
			ResourceBundle resource = cachedBundles.get(bundlePath);
			
			if(Objects.isNull(resource)) {
				resource = ResourceBundle.getBundle(bundlePath,
						locale, ApplicationServiceImpl.getInstance().getBean(ClassLoaderService.class).getClassLoader());
			}
			
			String localizedString = resource.getString(key);
			if (arguments == null || arguments.length == 0) {
				return localizedString;
			}
	
			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
		}
	}

	public static String getResource(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		try {
			return getResourceOrException(locale, resourceBundle, key, arguments);
		} catch (MissingResourceException mre) {
			return "[i18n/" + resourceBundle + "/" + key + "]";
		}
	}
	
	public static String getResourceNoDefault(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		try {
			return getResourceOrException(locale, resourceBundle, key, arguments);
		} catch (MissingResourceException mre) {
			return "";
		}
	}
	
	public static String getResourceNoOveride(Locale locale, String resourceBundle,
			String key, Object... arguments) {
		if (key == null) {
			throw new IllegalArgumentException("You must specify a key!");
		}
		if (resourceBundle == null) {
			throw new IllegalArgumentException(
					"You must specify a resource bundle for key " + key);
		}
		
		resourceBundle = "i18n/" + resourceBundle;
		
		try {
			ResourceBundle resource = cachedBundles.get(resourceBundle);
			
			if(Objects.isNull(resource)) {
				resource = ResourceBundle.getBundle(resourceBundle,
						locale, I18N.class.getClassLoader());
			}
			
			String localizedString = resource.getString(key);
			if (arguments == null || arguments.length == 0) {
				return localizedString;
			}

			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
		} catch (MissingResourceException mre) {
			return "[i18n/" + resourceBundle + "/" + key + "]";
		}
	}

	private static Object[] formatParameters(Object... arguments) {
		Collection<Object> formatted = new ArrayList<Object>(arguments.length);
		for (Object arg : arguments) {
			if (arg instanceof Date) {
				formatted.add(DateFormat.getDateTimeInstance().format(arg));
			} else {
				formatted.add(arg);
			}
		}
		return formatted.toArray(new Object[formatted.size()]);
	}

}
