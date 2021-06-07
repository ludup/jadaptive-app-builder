package com.jadaptive.plugins.web.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jadaptive.api.ui.AbstractPageExtension;

public class WizardState {

	String resourceKey;
	Integer currentStep = 0;
	List<AbstractPageExtension> pages = new ArrayList<>();
	AbstractPageExtension startPage;
	AbstractPageExtension finishPage;
	
	public WizardState(String resourceKey) {
		this.resourceKey = resourceKey;
	} 
	
	public void init(AbstractPageExtension startPage, AbstractPageExtension finishPage, AbstractPageExtension...pages) {
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
	
	public AbstractPageExtension getStartPage() {
		return startPage;
	}
	
	public AbstractPageExtension getFinishPage() {
		return finishPage;
	}
	
	public AbstractPageExtension moveNext() {
		if(isFinishPage()) {
			return getFinishPage();
		}
		currentStep++;
		return getCurrentPage();
	}
	
	public AbstractPageExtension moveBack() {
		if(currentStep > 1) {
			currentStep--;
		}
		return getCurrentPage();
	}
	
	public AbstractPageExtension getCurrentPage() {
		if(currentStep==0) {
			return getStartPage();
		}
		if(isFinishPage()) {
			return getFinishPage();
		}
		return pages.get(currentStep-1);
	}

	public boolean hasBackButton() {
		return currentStep > 1 && !isFinishPage();
	}

	public boolean hasNextButton() {
		return !isFinishPage() && currentStep > 0;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void start() {
		currentStep = 1;
	}

	public void finish() {
		
	}

	public boolean isStartPage() {
		return currentStep == 0;
	}
}
