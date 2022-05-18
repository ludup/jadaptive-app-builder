package com.jadaptive.api.wizards;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.tenant.TenantService;

public abstract class AbstractWizard implements WizardFlow, FormHandler {

	public static final String EXISTING_UUID = "existingUUID";
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private TenantService tenantService; 
	
	protected abstract Class<? extends WizardSection> getSectionClass();
	
	protected abstract String getStateAttribute();
	
	public abstract void finish(WizardState state);
	
	@Override
	public WizardState getState(HttpServletRequest request) {
		
		
		WizardState state = (WizardState) request.getSession().getAttribute(getStateAttribute());
		boolean isSystem = tenantService.getCurrentTenant().isSystem();
		
		if(Objects.isNull(state)) {
			state = new WizardState(this);
			
			List<WizardSection> sections = new ArrayList<>();
			sections.addAll(getDefaultSections());
			
			for(WizardSection section : applicationService.getBeans(getSectionClass())) {
				if(!isSystem && section.isSystem()) {
					continue;
				}
				sections.add(section);
			}
						
			state.init(getStartSection(),
					getFinishSection(), 
					sections.toArray((WizardSection[]) Array.newInstance(getSectionClass(), 0))); 
			request.getSession().setAttribute(getStateAttribute(), state);
		}
		
		assertPermissions(state);
		return state;
	}
	
	protected abstract void assertPermissions(WizardState state);

	@Override
	public void clearState(HttpServletRequest request) {
		request.getSession().setAttribute(getStateAttribute(), null);
	}

	@Override
	public <U extends UUIDEntity> String saveObject(U object) {
		
		if(StringUtils.isBlank(object.getUuid())) {
			object.setUuid(UUID.randomUUID().toString());
		}
		WizardState state = getState(Request.get());
		
		state.getCurrentPage().validateAndSave(object, state);
	
		return object.getUuid();
	}

	protected abstract WizardSection getFinishSection();

	protected abstract WizardSection getStartSection();

	protected abstract Collection<? extends WizardSection> getDefaultSections();
}
