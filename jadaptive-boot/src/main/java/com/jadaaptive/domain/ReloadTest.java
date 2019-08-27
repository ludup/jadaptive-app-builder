package com.jadaaptive.domain;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

public class ReloadTest {

	public static void main(String[] args) throws InterruptedException {
		
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		
		Map<String, byte[]> types = new HashMap<>();
		
		types.put("com.jadaptive.domain.Customer", CustomerInterfaceDefinition.makeV1().getBytes());
		
		// PASS simply load customer V1 (name) and act on it
		ByteArrayClassLoader loader1 = new ByteArrayClassLoader(contextClassLoader, types);
		
		ThreadExecution one = new ThreadExecution();
		one.setContextClassLoader(loader1);
		one.version = "1";
		
		one.start();
		
		
		Map<String, byte[]> types2 = new HashMap<>();
		
		types2.put("com.jadaptive.domain.Customer", CustomerInterfaceDefinition.makeV2().getBytes());
		
		// PASS simply load customer V2 (name, age) and act on it
		ByteArrayClassLoader loader2 = new ByteArrayClassLoader(contextClassLoader, types2);
		
		ThreadExecution two = new ThreadExecution();
		two.setContextClassLoader(loader2);
		
		two.start();
		
		// ERROR Customer is not defined with property age, but in logic we are calling it.
		ByteArrayClassLoader loader3 = new ByteArrayClassLoader(contextClassLoader, types);
		
		ThreadExecution three = new ThreadExecution();
		three.setContextClassLoader(loader3);
		// three.version = "1"; Will fall to version 2 logic executing call to age.
		
		three.start();
		
		loader1 = null;
		
		loader2 = null;
		
		loader3 = null;
	}
	
	static class ThreadExecution extends Thread {
		
		public String version;
		
		@Override
		public void run() {
			
			try {
				Class<?> klass = Thread.currentThread().getContextClassLoader().loadClass("com.jadaptive.domain.Customer");
				System.out.println(klass);
				
				Method name = klass.getDeclaredMethod("getName", new Class<?>[0]);
				
				System.out.println(name);
				
				if (!"1".equals(version)) {
				
					Method age = klass.getDeclaredMethod("getAge", new Class<?>[0]);
				
					System.out.println(age);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
