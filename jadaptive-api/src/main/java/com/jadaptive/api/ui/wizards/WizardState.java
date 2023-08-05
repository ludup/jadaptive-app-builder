package com.jadaptive.api.ui.wizards;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.Page;

public class WizardState {

	Integer currentStep = 1;
	List<WizardSection> pages = new ArrayList<>();
	WizardSection startPage;
	WizardSection finishPage;
	WizardFlow flow;

	Map<String,UUIDEntity> stateObjects = new HashMap<>(); 
	Map<String,Object> stateParameters = new HashMap<>();
	UUIDEntity completedObject;
	
	private boolean finished;;
	
	CompletionCallback onFinish = null;
	
	public WizardState(WizardFlow flow) {
		this.flow = flow;
		currentStep = Objects.nonNull(getStartPage()) ? 0 : 1;
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
	
	public void completed() {
		if(Objects.nonNull(onFinish)) {
			onFinish.finish(flow.getState(Request.get()));
		}
	}

	public boolean isStartPage() {
		return currentStep == 0;
	}

	public WizardFlow getFlow() {
		return flow;
	}

	public UUIDEntity getCurrentObject() {
		return stateObjects.get(pages.get(getCurrentStep()-1).getName());
	}
	
	public void setCurrentObject(UUIDEntity obj) {
		stateObjects.put(pages.get(getCurrentStep()-1).getName(), obj);
	}

	public void saveObject(UUIDEntity object) {
		stateObjects.put(object.getUuid(), object);
	}

	public Collection<WizardSection> getSections() {
		return pages;
	}

	public UUIDEntity getObject(WizardSection section) {
		return stateObjects.get(section.getName());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends UUIDEntity> T getObject(Class<T> type) {
		for(UUIDEntity e : stateObjects.values()) {
			if(e.getClass().equals(type)) {
				return (T)e;
			}
		}
		throw new IllegalStateException("No object of type " + type.getSimpleName());
	}

	public void insertNextPage(WizardSection setupSection) {

		removePage(setupSection.getClass());
		ApplicationServiceImpl.getInstance().autowire(setupSection);
		pages.add(getCurrentStep(), setupSection);
		
	}
	
	public void removePage(Class<? extends WizardSection> clz) {
		int idx = pageIndex(clz);
		if(idx > -1) {
			pages.remove(idx);
		}
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

	public void setCompletedObject(UUIDEntity completedObject) {
		this.completedObject = completedObject;
	}
	
	public UUIDEntity getCompletedObject() {
		return completedObject;
	}

	public String getBundle() {
		return flow.getBundle();
	}
	
	public void onComplete(CompletionCallback onFinish) {
		this.onFinish = onFinish;
	}
	
	@FunctionalInterface
	public interface CompletionCallback {
		void finish(WizardState state);
	}

	public boolean hasObject(Class<? extends UUIDEntity> type) {
		for(UUIDEntity e : stateObjects.values()) {
			if(e.getClass().equals(type)) {
				return true;
			}
		}
		return false;
	}
}
