package com.jadaptive.plugins.example;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class TestComponent {

	@PostConstruct
	private void postConstruct() {
		System.out.println("Created Example Test Component");
	}
}
