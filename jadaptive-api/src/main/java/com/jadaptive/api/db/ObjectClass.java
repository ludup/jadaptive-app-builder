package com.jadaptive.api.db;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.jadaptive.api.repository.UUIDEntity;

@Retention(RUNTIME)
@Target(TYPE_USE)
public @interface ObjectClass {

	Class<? extends UUIDEntity> resourceClass();
}
