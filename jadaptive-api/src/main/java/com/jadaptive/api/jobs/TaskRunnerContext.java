package com.jadaptive.api.jobs;

import org.pf4j.ExtensionPoint;

public interface TaskRunnerContext extends ExtensionPoint {

	void clearContext();

	void setupContext();

}
