package com.jadaptive.app.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.ui.pages.ext.BootstrapTheme;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeResolver;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeService;
import com.jadaptive.api.ui.pages.ext.Footer;

@Service
public class BootstrapThemeServiceImpl implements BootstrapThemeService {

	@Autowired(required = false)
	List<BootstrapThemeResolver> resolvers;
	
	@PostConstruct
	private void postConstruct() {
		if(Objects.nonNull(resolvers)) {
			Collections.sort(resolvers, Comparator.comparingInt(o -> o.weight()));
		}
	}
	
	@Override
	public BootstrapTheme getTheme() {
		
		if(Objects.isNull(resolvers)) {
			return Footer.getThemeFromCookie(BootstrapTheme.DEFAULT);
		}
		
		return resolvers.iterator().next().getTheme();
	}

}
