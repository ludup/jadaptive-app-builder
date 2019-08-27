package com.jadaaptive.domain;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

public class ReloadTest2 {

	public static void main(String[] args) throws InterruptedException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		
		Map<String, byte[]> types = new HashMap<>();
		
		types.put("com.jadaptive.domain.Customer", CustomerInterfaceDefinition.makeV1().getBytes());
		
		
		// PASS simply load customer V1 (name) and act on it
		ByteArrayClassLoader loader1 = new ByteArrayClassLoader(contextClassLoader, types);
		
		ThreadExecution one = new ThreadExecution();
		one.version = "1";
		one.setContextClassLoader(loader1);
		
		one.start();
		
		
		
		Map<String, byte[]> types2 = new HashMap<>();
		
		types2.put("com.jadaptive.domain.Customer", CustomerInterfaceDefinition.makeV2().getBytes());
		
		
		// PASS simply load customer V2 (name, age) and act on it
		ByteArrayClassLoader loader2 = new ByteArrayClassLoader(contextClassLoader, types2);
		
		ThreadExecution two = new ThreadExecution();
		two.setContextClassLoader(loader2);
		
		two.start();
		
		// ERROR case (Main thread does not knows about Customer)
		try {
			@SuppressWarnings("unused")
			Class<?> klass = contextClassLoader.loadClass("com.jadaptive.domain.Customer");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		one.join();
		
		two.join();
		
		loader1 = null;
		
		loader2 = null;
		
		
		// ERROR Customer is not defined with property age, but in script we are calling it.
		ByteArrayClassLoader loader3 = new ByteArrayClassLoader(contextClassLoader, types);
		
		ThreadExecution three = new ThreadExecution();
		// three.version = "1"; Will fall to version 2 script executing call to age.
		three.setContextClassLoader(loader3);
		
		three.start();
		
		loader3 = null;
	}
	
	static class ThreadExecution extends Thread {
		
		public String version;
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void run() {
			
			try {
				ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				
				Class<?> klass = contextClassLoader.loadClass("com.jadaptive.domain.Customer");
				System.out.println(klass);
				
				Method name = klass.getDeclaredMethod("getName", new Class<?>[0]);
				
				System.out.println(name);
				
				// without passing `contextClassLoader` also works but explicit.
				Object customer = EntityLocator.get(contextClassLoader, klass);
				
				Method setName = klass.getDeclaredMethod("setName", String.class);
				setName.invoke(customer, "Bob Corp.");
				
				// without passing `contextClassLoader` also works but explicit.
				ScriptEngineManager manager = new ScriptEngineManager(contextClassLoader); 
				
				manager.getEngineFactories().forEach( fac -> System.out.println(fac.getScriptEngine()));
				ScriptEngine engine = manager.getEngineByName("nashorn"); 
				
				final Compilable compilable = (Compilable) engine;
		        final Invocable invocable = (Invocable) engine;
		        
		        
		        String statement = null;
		        
		        if ("1".equals(version)) {
		        	statement =
			                "function fetch(customer) { print(\"Hi there from Java, \" + customer.getName()); "
			                + "customer.setName('JADAPTIVE Limited') ; "
			                + " print(\"Hi there from Java, \" + customer.getName());"
			                + "return { customer: customer, count: 1}; "
			                + "};";
		        } else {
		        	statement =
			                "function fetch(customer) { print(\"Hi there from Java, \" + customer.getName()); "
			                + "customer.setName('JADAPTIVE Limited') ; "
			                + "customer.setAge(5) ; "
			                + " print(\"Hi there from Java, \" + customer.getName());"
			                + "return { customer: customer, count: 1}; "
			                + "};";
		        }
		        
		        
		        final CompiledScript compiled = compilable.compile(statement);

		        compiled.eval();

		        Map<String, Object> updatedCustomer = (Map) invocable.invokeFunction("fetch", customer);
				
				System.out.println("updatedCustomer " + updatedCustomer);
				
				System.out.println("updatedCustomer " + updatedCustomer.entrySet());
				
				System.out.println("updatedCustomer " + updatedCustomer.get("customer"));
				
				
				
				String value = (String) name.invoke(updatedCustomer.get("customer"));
				
				System.out.println(value);
				
				if (!"1".equals(version)) {
					Method age = klass.getDeclaredMethod("getAge", new Class<?>[0]);
					
					int ageValue = (int) age.invoke(updatedCustomer.get("customer"));
					
					System.out.println(ageValue);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
}
