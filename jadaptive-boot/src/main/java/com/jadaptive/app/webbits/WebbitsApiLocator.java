package com.jadaptive.app.webbits;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.ApiLocator;
import com.codesmith.webbits.ClassMatch;
import com.codesmith.webbits.Context;
import com.codesmith.webbits.api.Api;
import com.codesmith.webbits.api.ApiPaths;
import com.codesmith.webbits.util.Annotations;
import com.jadaptive.api.db.ClassLoaderService;

@Component
public class WebbitsApiLocator implements ApiLocator {

	static Logger log = LoggerFactory.getLogger(WebbitsApiLocator.class);

	@Autowired
	private ClassLoaderService classloaderService;

	private ApiPaths vp = null;

	private synchronized void buildViewLists() {

		if (Objects.isNull(vp)) {

			vp = new ApiPaths();
			for (Class<?> clazz : classloaderService.resolveAnnotatedClasses(Api.class)) {
				if (Annotations.hasAnyClassAnnotations(clazz, Api.class)) {
					vp.put(clazz);
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
}
