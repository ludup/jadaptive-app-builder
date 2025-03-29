package com.jadaptive.app.ui;

import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.ui.pages.ext.BootstrapTheme;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeResolver;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeService;
import com.jadaptive.api.ui.pages.ext.Footer;
import com.jadaptive.api.ui.pages.ext.Theme;

@Service
public class BootstrapThemeServiceImpl implements BootstrapThemeService {

	@Autowired
	private App applicationService;
	
	@Override
	public Theme getTheme() {
		return applicationService.getBeans(BootstrapThemeResolver.class).stream().
				sorted(Comparator.comparingInt(o -> o.weight())).
				findFirst().
				map(BootstrapThemeResolver::getTheme).
				orElseGet(() -> Footer.getThemeFromCookie(BootstrapTheme.DEFAULT));
	}

}
