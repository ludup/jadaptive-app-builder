package com.jadaptive.api.wizards;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.tenant.TenantService;

public abstract class AbstractWizard<T extends WizardSection> implements WizardFlow, FormHandler {

	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private TenantService tenantService; 
	
	protected abstract Class<? extends T> getSectionClass();
	
	protected abstract String getStateAttribute();
	
	@SuppressWarnings("unchecked")
	@Override
	public WizardState getState(HttpServletRequest request) {
		WizardState state = (WizardState) request.getSession().getAttribute(getStateAttribute());
		if(Objects.isNull(state)) {
			state = new WizardState(this);
			
			List<T> sections = new ArrayList<>();
			sections.addAll(getDefaultSections());
			sections.addAll(applicationService.getBeans(getSectionClass()));
			
			Collections.sort(sections, new Comparator<T>() {

				@Override
				public int compare(T o1, T o2) {
					return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
				}
				
			});
			
			state.init(getStartSection(),
					getFinishSection(), 
					sections.toArray((T[]) Array.newInstance(getSectionClass(), 0))); 
			request.getSession().setAttribute(getStateAttribute(), state);
		}
		return state;
	}
	
	@Override
	public void finish() {
		
		WizardState state = (WizardState) Request.get().getSession().getAttribute(getStateAttribute());
		if(Objects.isNull(state)) {
			throw new IllegalStateException();
		}
		
		Integer sectionIndex = 1;
		for(WizardSection section : state.getSections()) {
			section.finish(state, sectionIndex++);
		}
		
		state.setFinished(true);
		tenantService.completeSetup();
	}
	
	@Override
	public void processReview(Document document, WizardState state) {
		
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

	protected abstract T getFinishSection();

	protected abstract T getStartSection();

	protected abstract Collection<T> getDefaultSections();
}
