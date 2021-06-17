package com.jadaptive.plugins.web.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jadaptive.api.ui.AbstractPageExtension;
import com.jadaptive.plugins.web.objects.CreateAccount;
import com.jadaptive.plugins.web.objects.CreateInterface;

public class WizardState {

	Integer currentStep = 0;
	List<AbstractPageExtension> pages = new ArrayList<>();
	AbstractPageExtension startPage;
	AbstractPageExtension finishPage;
	WizardFlow flow;
	
	CreateAccount account;
	CreateInterface listeningInterface;
	
	public WizardState(WizardFlow flow) {
		this.flow = flow;
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
		
	}

	public boolean isStartPage() {
		return currentStep == 0;
	}

	public WizardFlow getFlow() {
		return flow;
	}

	public CreateAccount getAccount() {
		return account;
	}

	public void setAccount(CreateAccount account) {
		this.account = account;
	}

	public void setInterface(CreateInterface listeningInterface) {
		this.listeningInterface = listeningInterface;
	}

	public CreateInterface getInterface() {
		return listeningInterface;
	}
	
	
}
