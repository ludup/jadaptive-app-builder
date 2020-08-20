package com.jadaptive.plugins.sshd;

import org.pf4j.PluginWrapper;

import com.jadaptive.api.spring.AbstractSpringPlugin;
import com.sshtools.common.logger.DefaultLoggerContext;
import com.sshtools.common.logger.Log;

public class SSHDServerPlugin extends AbstractSpringPlugin {
	
	public SSHDServerPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void stop() {
		Log.getDefaultContext().shutdown();
		super.stop();
	}
	
	
}
