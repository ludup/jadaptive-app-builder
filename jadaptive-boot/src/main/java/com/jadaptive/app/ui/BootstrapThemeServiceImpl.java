package com.jadaptive.app.ui;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.pages.ext.BootstrapTheme;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeResolver;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeService;
import com.jadaptive.api.ui.pages.ext.BootstrapThemeSet;
import com.jadaptive.api.ui.pages.ext.Theme;
import com.jadaptive.utils.Utils;

import jakarta.servlet.http.Cookie;

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
				orElseGet(() -> getThemeFromCookie(BootstrapTheme.DEFAULT));
	}

	@Override
	public Set<Enum<? extends Theme>[]> getAllThemes() {
		var t = new LinkedHashSet<Enum<? extends Theme>[]>();
		applicationService.getBeans(BootstrapThemeSet.class).forEach(s -> t.add(s.getThemes()));
		t.add(BootstrapTheme.values());
		return t;
	}

	@Override
	public Theme getThemeFromCookie(Theme defaultValue) {
		
		Cookie[] cookies = Request.get().getCookies();
		if(Objects.nonNull(cookies)) {
			for(Cookie c : cookies) {
				if("userTheme".equals(c.getName())) {
					if(NumberUtils.isNumber(c.getValue())) {
						return BootstrapTheme.values()[Utils.parseIntOrDefault(c.getValue(), 0)];
					} else {
						for(var t : getAllThemes()) {
							for(var e : t) {
								if(e.name().equals(c.getValue()))
									return (Theme)e;
							}
						}
					}
				}
			}
		}
		return defaultValue;
	}
}
