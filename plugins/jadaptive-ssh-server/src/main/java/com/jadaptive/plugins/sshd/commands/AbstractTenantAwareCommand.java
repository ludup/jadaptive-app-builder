package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.Candidate;
import org.jline.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.SSHDService;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.Environment;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public abstract class AbstractTenantAwareCommand extends ShellCommand {

	static Logger log = LoggerFactory.getLogger(AbstractTenantAwareCommand.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	protected UserService userService; 
	
	protected VirtualConsole console;
	protected String[] args;
	protected User currentUser;
	
	public AbstractTenantAwareCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}

	@Override
	public void run(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		tenantService.setCurrentTenant(resolveTenant(console.getConnection(), console.getEnvironment()));
		
		this.console = console;
		this.args = args;
		this.currentUser = userService.getUser(console.getConnection().getUsername());
		
		try {
			doRun(args, console);
		} catch(UsageException e) { 
			console.println(e.getMessage());
		} catch(AccessDeniedException e) { 
			console.println("You do not have the permission to perform the requested action.");
		} catch(Throwable e) { 
			Log.error(e);
			console.println(StringUtils.defaultString(e.getMessage(),
					String.format("Unexpected error %s", e.getClass().getSimpleName())));
		} finally {
			tenantService.clearCurrentTenant();
		}
		
	}
	
	protected User getCurrentUser() {
		return this.currentUser; 
	}
	
	protected void printUsage() {
		console.println(getUsage());
	}

	protected abstract void doRun(String[] args, VirtualConsole console) 
				throws IOException, PermissionDeniedException, UsageException;

	protected Tenant resolveTenant(SshConnection connection, Environment environment) {
		return (Tenant) connection.getProperty(SSHDService.TENANT);
	}
	
	protected char[] promptForPassword(String prompt) {
		String str = console.getLineReader().readLine(prompt, '*');
		return str.toCharArray();
	}

	protected void assertAdministrationPermission() {
		
	}
	
	protected void fillCurrentDirectoryCandidates(List<Candidate> candidates) {
		try {
			for(AbstractFile file : console.getCurrentDirectory().getChildren()) {
				if(file.isFile()) {
					candidates.add(new Candidate(file.getName()));
				}
			}
		} catch (IOException | PermissionDeniedException e) {
			log.error("Failure iterating current directory files during auto-completion", e);
		}

	}

}
