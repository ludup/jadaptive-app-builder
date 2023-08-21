package com.jadaptive.app.saml.idp.web;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jadaptive.api.servlet.PluginController;

@Controller
@Extension
public class IdentityProviderController implements PluginController  {
    
	
	static Logger log = LoggerFactory.getLogger(IdentityProviderController.class);
	
	@RequestMapping(value = {"/"})
    public String selectProvider() {
        log.info("Sample IDP Application - Select an SP to log into!");
        return "redirect:/saml/idp/select";
    }

}