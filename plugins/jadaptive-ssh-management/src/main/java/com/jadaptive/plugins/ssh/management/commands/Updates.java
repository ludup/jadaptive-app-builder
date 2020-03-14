package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.pf4j.update.PluginInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationUpdateManager;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Updates extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationUpdateManager updateService; 
	
	public Updates() {
		super("updates", "System Management", UsageHelper.build("updates [option]",
				"-c, --check                           Check for updates",
				"-i, --install                         Install available updates"),
				"Check for and install application updates");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasOption(args, 'c', "check")) {	
			check4Updates();
		} else if(CliHelper.hasOption(args, 'i', "install")) {
			installUpdates();
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private void installUpdates() {
		
		List<PluginInfo> updates = new ArrayList<>();
		List<PluginInfo> newInstalls = new ArrayList<>();
		
		int count = check4Updates(updates, newInstalls);
		
		if(count > 0) {
		
			String answer = console.readLine(String.format("Install %d updates? (y/n): ", count));
			if("yes".contains(answer.toLowerCase())) {
				try {
					updateService.installUpdates();

					console.println("The application is being restarted");
					
					console.getSessionChannel().close();
					console.getConnection().disconnect();
					
					updateService.restart();
					
				} catch (IOException e) {
					console.println("Installation failed!");
					console.println(e.getMessage());
				}
			}
		
		} 
	}

	private int check4Updates() {
		
		List<PluginInfo> updates = new ArrayList<>();
		List<PluginInfo> newInstalls = new ArrayList<>();
		
		return check4Updates(updates, newInstalls);
	}
	
	private int check4Updates(List<PluginInfo> updates, List<PluginInfo> newInstalls) {
		
		updateService.check4Updates(updates, newInstalls);
		
		if(updates.isEmpty() && newInstalls.isEmpty()) {
			console.println("There are no updates available");
			return 0;
		}
		
		if(!updates.isEmpty()) {
			console.println("Updatable plugins");
			printPlugins(updates);
		}
		if(!newInstalls.isEmpty()) {
			console.println("New plugins");
			printPlugins(newInstalls);
		}
		
		return updates.size() + newInstalls.size();
	}

	private void printPlugins(List<PluginInfo> infos) {
		for(PluginInfo info : infos) {
			console.println("  " + info.id);
		}
	}

}
