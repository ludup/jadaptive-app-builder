package com.jadaptive.plugins.ssh.management.commands.users;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.Candidate;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserImpl;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.ssh.management.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateUser extends AbstractTenantAwareCommand {
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private ConsoleHelper consoleHelper;
	
	@Autowired
	private TemplateService templateService; 
	
	public CreateUser() {
		super("create-user", "User Management", "create-user", "Create a builtin user account");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		try {
			createUser();
		} catch (ParseException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private void createUser() throws ParseException, PermissionDeniedException, IOException {
		
		permissionService.assertReadWrite(UserService.USER_RESOURCE_KEY);
		
		ObjectTemplate userTemplate = selectUserTemplate();
		
		Map<String,Object> doc = consoleHelper.promptTemplate(console, new HashMap<>(), userTemplate, null, userTemplate.getTemplateClass());
		User user = templateService.createObject(doc, UserImpl.class);
		
		String password;
		String confirmPassword;
		boolean identical;
		do {
			password = console.getLineReader().readLine("Password: ", '*');
			confirmPassword = console.getLineReader().readLine("Confirm Password: ", '*');
			identical = StringUtils.equals(password, confirmPassword);
			if(!identical) {
				console.println("Passwords do not match");
			}
		} while(!identical);
		
		userService.createUser(user, password.toCharArray(), false);
		
		console.println(String.format("Created user %s", user.getUsername()));
	}

	private ObjectTemplate selectUserTemplate() {
		
		Collection<ObjectTemplate> userTemplates = userService.getCreateUserTemplates();
		List<Candidate> candidates = new ArrayList<>();
		if(userTemplates.size() == 1) {
			return userTemplates.iterator().next();
		}
		
		Map<String,ObjectTemplate> templates = new HashMap<>();
		for(ObjectTemplate t : userTemplates) {
			candidates.add(new Candidate(t.getName()));
			templates.put(t.getName(), t);
		}
		console.getEnvironment().put("_COMPLETIONS", candidates);
		console.println("Select a user template. Use tab to cycle through the available templates.");
		
		ObjectTemplate val = null;
		do {
			String templateName = console.readLine("User Template: ").trim();
			val = templates.get(templateName);
		}
		while(val == null);
		
		console.getEnvironment().remove("_COMPLETIONS");
		return val;
		
		
	}
}
