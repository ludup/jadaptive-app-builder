package com.jadaptive.plugins.web.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.HtmlPage;
import com.jadaptive.api.ui.HtmlPageExtender;
import com.jadaptive.api.ui.UserInterfaceService;

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
			if(extender.isExtending(htmlPage)) {
				exts.add(extender);
			}
		}
		return exts;
	}

}
