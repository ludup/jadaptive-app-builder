package com.jadaptive.api.app;

import java.io.IOException;
import java.util.List;

import org.pf4j.update.PluginInfo;

public interface ApplicationUpdateManager {

	boolean installUpdates(List<PluginInfo> toUpdate, List<PluginInfo> toInstall) throws IOException;

	boolean installUpdates() throws IOException;

	void check4Updates(List<PluginInfo> toUpdate, List<PluginInfo> toInstall);


}
