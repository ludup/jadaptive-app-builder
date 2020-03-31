package com.jadaptive.api.jobs;

import org.pf4j.ExtensionPoint;

public interface JobRunnerContext extends ExtensionPoint {

	void clearContext();

	void setupContext();

}
