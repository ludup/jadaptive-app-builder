package com.jadaptive.api.wizards;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;

@Service
public class WizardServiceImpl implements WizardService {

	@Autowired
	private ApplicationService applicationService; 
	
	Map<String,WizardFlow> wizards = new HashMap<>();
	
	@Override
	public WizardFlow getWizard(String resourceKey) {
		
		buildWizardCache();
		
		return wizards.get(resourceKey);
	}

	private void buildWizardCache() {
		if(wizards.isEmpty()) {
			for(WizardFlow wizard : applicationService.getBeans(WizardFlow.class)) {
				wizards.put(wizard.getResourceKey(), wizard);
			}
		}
	}
}
