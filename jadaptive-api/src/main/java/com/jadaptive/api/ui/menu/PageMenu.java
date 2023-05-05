package com.jadaptive.api.ui.menu;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

@Target(TYPE)
public @interface PageMenu {

	Class<? extends ApplicationMenu> parent();
}
