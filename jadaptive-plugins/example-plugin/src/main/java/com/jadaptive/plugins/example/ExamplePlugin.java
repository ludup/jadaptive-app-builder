package com.jadaptive.plugins.example;

import org.pf4j.PluginWrapper;

import com.jadaptive.api.spring.AbstractSpringPlugin;

public class ExamplePlugin extends AbstractSpringPlugin {

	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	protected void doStart() {
		System.out.println("Starting Example Plugin");
	}
}
