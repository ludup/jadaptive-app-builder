package com.jadaptive.plugins.web.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.ui.UserInterfaceService;
import com.jadaptive.api.ui.menu.ApplicationMenu;

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
	public boolean isEnabled(ApplicationMenu menu) {
		for(UserInterfaceAdapter adapter : applicationService.getBeans(UserInterfaceAdapter.class)) {
			if(adapter.isController(menu)) {
				return adapter.isEnabled(menu);
			}
		}
		return menu.isEnabled();
	}

//	@Override
//	public Feedback getFeedback(HttpServletRequest request, Page page) {
//		for(UserInterfaceAdapter adapter : applicationService.getBeans(UserInterfaceAdapter.class)) {
//			Feedback feedback = adapter.getFeedback(request, page);
//			if(Objects.nonNull(feedback)) {
//				return feedback;
//			}
//		}
//		return null;
//	}

}
