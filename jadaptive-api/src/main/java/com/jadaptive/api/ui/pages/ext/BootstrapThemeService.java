package com.jadaptive.api.ui.pages.ext;

import java.util.Set;

public interface BootstrapThemeService {

	Theme getTheme();

	Set<Enum<? extends Theme>[]> getAllThemes();

	Theme getThemeFromCookie(Theme defaultValue);
}
