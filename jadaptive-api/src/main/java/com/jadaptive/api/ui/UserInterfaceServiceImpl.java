package com.jadaptive.api.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.template.ObjectTemplate;

@Service
public class UserInterfaceServiceImpl implements UserInterfaceService {

	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public boolean canCreate(ObjectTemplate template) {
	
		for(UserInterfaceAdapter adapter : applicationService.getBeans(UserInterfaceAdapter.class)) {
			if(adapter.isController(template)) {
				return adapter.canCreate(template);
			}
		}
		return template.isCreatable();
	}

	@Override
	public boolean canUpdate(ObjectTemplate template) {
		for(UserInterfaceAdapter adapter : applicationService.getBeans(UserInterfaceAdapter.class)) {
			if(adapter.isController(template)) {
				return adapter.canUpdate(template);
			}
		}
		return template.isUpdatable();
	}
	
	@Override
	public Collection<HtmlPageExtender> getExtenders(HtmlPage htmlPage) {
		
		List<HtmlPageExtender> exts = new ArrayList<>();
		for(HtmlPageExtender extender : applicationService.getBeans(HtmlPageExtender.class)) {
			if(extender.isExtending(htmlPage, Request.get().getRequestURI())) {
				exts.add(extender);
			}
		}
		return exts;
	}

}
