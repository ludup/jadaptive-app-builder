package com.jadaaptive.domain;

import javassist.Modifier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Unloaded;

public class CustomerInterfaceDefinition {
	
	public static Unloaded<?> makeV1() {
		return new ByteBuddy()
				.makeInterface()
		  .name("com.jadaptive.domain.Customer")
		  .defineMethod("setName", Void.class, Modifier.PUBLIC).withParameter(String.class)
		  .withoutCode()
		  .defineMethod("getName", String.class, Modifier.PUBLIC)
		  .withoutCode()
		  .make();
	}
	
	public static Unloaded<?> makeV2() {
		return new ByteBuddy()
				.makeInterface()
		  .name("com.jadaptive.domain.Customer")
		  .defineMethod("setName", Void.class, Modifier.PUBLIC).withParameter(String.class)
		  .withoutCode()
		  .defineMethod("getName", String.class, Modifier.PUBLIC)
		  .withoutCode()
		  .defineMethod("setAge", Void.class, Modifier.PUBLIC).withParameter(int.class)
		  .withoutCode()
		  .defineMethod("getAge", int.class, Modifier.PUBLIC)
		  .withoutCode()
		  .make();
	}
}
