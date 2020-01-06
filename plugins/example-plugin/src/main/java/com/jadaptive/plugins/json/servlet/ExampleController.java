package com.jadaptive.plugins.json.servlet;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.pf4j.Extension;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.servlet.PluginController;

@Controller
@Extension
public class ExampleController extends PluginController {

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
