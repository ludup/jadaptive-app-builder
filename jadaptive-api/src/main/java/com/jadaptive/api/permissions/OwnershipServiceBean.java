package com.jadaptive.api.permissions;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface OwnershipServiceBean {

	Class<? extends OwnershipService> bean();
}
