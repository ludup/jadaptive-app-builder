package com.jadaptive.api.wizards;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.setup.WizardSection;
import com.jadaptive.api.ui.Page;

public class WizardState {

	Integer currentStep = 0;
	List<WizardSection> pages = new ArrayList<>();
	WizardSection startPage;
	WizardSection finishPage;
	WizardFlow flow;

	Map<Class<?>,UUIDEntity> stateObjects = new HashMap<>(); 
	Map<String,Object> stateParameters = new HashMap<>();
	private boolean finished;;
	
	public WizardState(WizardFlow flow) {
		this.flow = flow;
	} 
	
	public void init(WizardSection startPage, WizardSection finishPage, WizardSection...pages) {
		this.startPage = startPage;
		this.finishPage = finishPage;
		this.pages.addAll(Arrays.asList(pages));
	}
	
	public Integer getCurrentStep() {
		return currentStep;
	}
	
	public boolean isFinishPage() {
		return currentStep > pages.size();
	}
	
	public WizardSection getStartPage() {
		return startPage;
	}
	
	public WizardSection getFinishPage() {
		return finishPage;
	}
	
	public WizardSection moveNext() {
		if(isFinishPage()) {
			return getFinishPage();
		}
		currentStep++;
		return getCurrentPage();
	}
	
	public WizardSection moveBack() {
		if(currentStep > 1) {
			currentStep--;
		}
		return getCurrentPage();
	}
	
	public WizardSection getCurrentPage() {
		if(currentStep==0) {
			return getStartPage();
		}
		if(isFinishPage()) {
			return getFinishPage();
		}
		return pages.get(currentStep-1);
	}

	public boolean hasBackButton() {
		return currentStep > 1;
	}

	public boolean hasNextButton() {
		return !isFinishPage() && currentStep > 0;
	}

	public String getResourceKey() {
		return flow.getResourceKey();
	}

	public void start() {
		currentStep = 1;
	}

	public void finish() {
		flow.finish(this);
		setFinished(true);
	}

	public boolean isStartPage() {
		return currentStep == 0;
	}

	public WizardFlow getFlow() {
		return flow;
	}

	public UUIDEntity getCurrentObject() {
		return stateObjects.get(pages.get(getCurrentStep()-1).getClass());
	}
	
	public void setCurrentObject(UUIDEntity obj) {
		stateObjects.put(pages.get(getCurrentStep()-1).getClass(), obj);
	}

	public void saveObject(UUIDEntity object) {
		stateObjects.put(pages.get(getCurrentStep()-1).getClass(), object);
	}

	public Collection<WizardSection> getSections() {
		return pages;
	}

	public UUIDEntity getObject(Class<? extends WizardSection> section) {
		return stateObjects.get(section);
	}

	public void insertNextPage(WizardSection setupSection) {
		pages.add(getCurrentStep(), setupSection);
	}
	
	public void removePage(Class<? extends WizardSection> clz) {
		pages.remove(pageIndex(clz));
	}

	public boolean containsPage(Class<? extends WizardSection> clz) {
		return pageIndex(clz) > -1;
	}
	
	public int pageIndex(Class<? extends WizardSection> clz) {
		int idx = 0;
		for(WizardSection page : pages) {
			if(clz.isAssignableFrom(page.getClass())) {
				return idx;
			}
			idx++;
		}
		return -1;
	}
	
	public Object getParameter(String name) {
		return stateParameters.get(name);
	}
	
	public void setParameter(String name, Object value) {
		stateParameters.put(name, value);
	}

	public Page getCompletePage() throws FileNotFoundException {
		return flow.getCompletePage();
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
