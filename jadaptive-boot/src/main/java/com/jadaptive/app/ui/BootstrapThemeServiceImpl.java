package com.jadaptive.app.ui;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.ui.pages.ext.BootstrapTheme;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeResolver;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeService;
import com.jadaptive.api.ui.pages.ext.Footer;

@Service
public class BootstrapThemeServiceImpl implements BootstrapThemeService {

	@Autowired
	private ApplicationService applicationService;
	
	@Override
	public BootstrapTheme getTheme() {
		return applicationService.getBeans(BootstrapThemeResolver.class).stream().
				sorted(Comparator.comparingInt(o -> o.weight())).
				findFirst().
				map(BootstrapThemeResolver::getTheme).
				orElseGet(() -> Footer.getThemeFromCookie(BootstrapTheme.DEFAULT));
	}

}
