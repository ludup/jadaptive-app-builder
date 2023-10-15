package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.FormHandler;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.UriRedirect;

public abstract class AbstractWizard implements WizardFlow, FormHandler {
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private TransactionService transactionService; 
	
	protected abstract Class<? extends WizardSection> getSectionClass();
	
	protected String getStateAttribute() {
		return String.format("%s-state", getResourceKey());
	}
	
	@Override
	public boolean requiresUserSession() {
		return true;
	}
	
	public void finish(WizardState state) {
		
		beforeTransaction(state);
		transactionService.executeTransaction(()-> {
			startTransaction(state);
			for(WizardSection section : state.getSections()) {
				section.finish(state);
			}
			finishTransaction(state);
			state.completed();
		});
		afterTransaction(state);
	}
	
	protected void beforeTransaction(WizardState state) { };

	protected void afterTransaction(WizardState state) { };
	
	protected void finishTransaction(WizardState state) { };

	protected void startTransaction(WizardState state) { };

	@Override
	public WizardState getState(HttpServletRequest request) {
		
		
		WizardState state = (WizardState) request.getSession().getAttribute(getStateAttribute());
		boolean isSystem = tenantService.getCurrentTenant().isSystem();
		
		if(Objects.isNull(state)) {
			state = new WizardState(this);
			
			List<WizardSection> sections = new ArrayList<>();
			
			for(WizardSection section : getDefaultSections()) {
				if(!isSystem && section.isSystem()) {
					continue;
				}
				if(section.isEnabled()) {
					sections.add(section);
				}
			}
			
			for(WizardSection section : applicationService.getBeans(getSectionClass())) {
				if(!isSystem && section.isSystem()) {
					continue;
				}
				if(section.isEnabled()) {
					sections.add(section);
				}
			}
			
			Collections.sort(sections, new Comparator<WizardSection>() {

				@Override
				public int compare(WizardSection o1, WizardSection o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
				
			});
						
			state.init(getStartSection(),
					getFinishSection(), 
					sections.toArray((new WizardSection[0]))); 
			request.getSession().setAttribute(getStateAttribute(), state);
		}
		
		assertPermissions(state);
		return state;
	}
	
	protected WizardSection getFinishSection() {
		return new DefaultWizardSection(getResourceKey(), "finishWizard", "/com/jadaptive/plugins/web/ui/FinishWizard.html", 0);
	}

	
	protected void assertPermissions(WizardState state) { };

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

	protected WizardSection getStartSection() {
		return new StartSection(getBundle(), getResourceKey());
	}

	@Override
	public Page getCompletePage() throws FileNotFoundException {
		throw new UriRedirect("/app/ui/wizard-complete/" + getResourceKey());
	}

	protected abstract Collection<? extends WizardSection> getDefaultSections();
}
