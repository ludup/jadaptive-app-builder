package com.jadaptive.plugins.ssh.management.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.util.FileUtils;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Security extends AbstractTenantAwareCommand {

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	SecurityPropertyService securityService; 
	
	public Security() {
		super("security", "Administration", UsageHelper.build("security [option] <path> <key> <value>",
				"-r, --resolve          Resolve security properties for URI path",
				"-o, --override         Show overridden properties at the URI path only (not resolved)",
				"-e, --edit             Change or override a property for a URI path",
				"-d, --delete           Delete a property for a URI path",
				"-s, --shared           Apply to shared URI paths",
				"-p, --private          Apply to system private URI paths",
				"-t, --tenant           Apply to tenant URI paths"),
				"Manage webapp security properties");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length > 2 && (CliHelper.hasShortOption(args, 'r') || CliHelper.hasLongOption(args, "resolve"))) {	
			printProperties(false);
		} else if(args.length >= 2 && (CliHelper.hasShortOption(args, 'o') || CliHelper.hasLongOption(args, "override"))) {	
			printProperties(true);
		} else if(args.length >= 5 && (CliHelper.hasShortOption(args, 'e') || CliHelper.hasLongOption(args, "edit"))) {	
			saveProperty();
		} else if(args.length >= 4 && (CliHelper.hasShortOption(args, 'd') || CliHelper.hasLongOption(args, "delete"))) {	
			deleteProperty();
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	
	

	private void deleteProperty() throws IOException {
		
		String path = FileUtils.checkEndsWithSlash(args[args.length-2]);
		String key = args[args.length-1];
		
		SecurityScope scope = SecurityScope.SHARED;
		if(CliHelper.hasShortOption(args, 'p') || CliHelper.hasLongOption(args, "private")) {
			scope = SecurityScope.PRIVATE;
		} else if(CliHelper.hasShortOption(args, 't') || CliHelper.hasLongOption(args, "tenant")) {
			scope = SecurityScope.TENANT;
		}
	
		securityService.deleteProperty(scope, path, key);
		console.println(String.format("Deleted %s property in %s scope for uri %s", 
					key, scope.name().toLowerCase(), path));
		
	}

	private void saveProperty() throws IOException {
		
		String path = FileUtils.checkEndsWithSlash(args[args.length-3]);
		String key = args[args.length-2];
		String value = args[args.length-1];
		
		SecurityScope scope = SecurityScope.SHARED;
		if(CliHelper.hasShortOption(args, 'p') || CliHelper.hasLongOption(args, "private")) {
			scope = SecurityScope.PRIVATE;
		} else if(CliHelper.hasShortOption(args, 't') || CliHelper.hasLongOption(args, "tenant")) {
			scope = SecurityScope.TENANT;
		}
	
		securityService.saveProperty(scope, path, key, value);
		console.println(String.format("Saved property in %s scope for uri %s", 
					scope.name().toLowerCase(), path));
		
	}

	private void printProperties(boolean overrides) throws IOException {
		
		
		Properties properties;
		String uri = FileUtils.checkEndsWithSlash(args[args.length-1]);
		
		if(!overrides) {
			properties = securityService.resolveSecurityProperties(uri);
			console.println(String.format("Resolved properties that apply to uri %s", uri));
		} else {
			SecurityScope scope = SecurityScope.SHARED;
			if(CliHelper.hasShortOption(args, 's') || CliHelper.hasLongOption(args, "private")) {
				scope = SecurityScope.PRIVATE;
			} else if(CliHelper.hasShortOption(args, 't') || CliHelper.hasLongOption(args, "tenant")) {
				scope = SecurityScope.TENANT;
			}
			
			properties = securityService.getOverrideProperties(scope, uri);
			console.println(String.format("Override properties in %s scope for uri %s", 
					scope.name().toLowerCase(), uri));
		}
		
		for(String key : properties.stringPropertyNames()) {
			console.println(String.format("%s=%s", key, properties.get(key)));
		}
	}
}
