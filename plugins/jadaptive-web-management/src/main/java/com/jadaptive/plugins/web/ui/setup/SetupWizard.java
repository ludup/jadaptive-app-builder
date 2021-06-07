package com.jadaptive.plugins.web.ui.setup;

import java.io.FileNotFoundException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.plugins.web.wizard.WizardFlow;
import com.jadaptive.plugins.web.wizard.WizardState;

@Extension
public class SetupWizard implements WizardFlow {

	public static final String RESOURCE_KEY = "setup";

	private static final String STATE_ATTR = "setupState";
	
	@Autowired
	ApplicationService applicationService; 
	
//	@Autowired
//	private PageCache pageCache;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

//	private Page resolvePage(Class<? extends Page> page, WizardState state) throws FileNotFoundException {
//		Page obj = pageCache.resolvePage(page);
//		Map<String,Object> params = new HashMap<>();
//		params.put("state", state);
//		pageCache.populateFields(obj, params);
//		return obj;
//	}
	
	@Override
	public WizardState getState(HttpServletRequest request) throws FileNotFoundException {
		WizardState state = (WizardState) request.getSession().getAttribute(STATE_ATTR);
		if(Objects.isNull(state)) {
			state = new WizardState(RESOURCE_KEY);
			state.init(applicationService.getBean(StartSetup.class), 
					applicationService.getBean(FinishSetup.class), 
					applicationService.getBean(EULA.class), 
					applicationService.getBean(AdminCredentials.class)); 
			request.getSession().setAttribute(STATE_ATTR, state);
		}
		return state;
	}

}
