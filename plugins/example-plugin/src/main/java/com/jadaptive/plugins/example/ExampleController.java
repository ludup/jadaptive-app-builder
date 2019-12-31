package com.jadaptive.plugins.example;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ExampleController {

	@PostConstruct
	private void postConstruct() {
		System.out.println("Created Example Spring Controller");
	}
	
	@RequestMapping(value="example-get", method = RequestMethod.GET, produces = {"application/json"})
	@ResponseBody
	@ResponseStatus(value=HttpStatus.OK)
	public String doGet(HttpServletRequest request) {
		return "Example GET completed";
	}
}
