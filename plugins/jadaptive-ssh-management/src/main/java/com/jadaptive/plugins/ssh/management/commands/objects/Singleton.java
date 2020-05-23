package com.jadaptive.plugins.ssh.management.commands.objects;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.plugins.ssh.management.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Singleton extends AbstractTenantAwareCommand {
	
	@Autowired
	private EntityTemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper;
	 
	@Autowired
	private ClassLoaderService classLoader;
	
	public Singleton() {
		super("singleton", "Object Management", UsageHelper.build("singleton <template> [<field> <value>]"),
				"Configure a singleton object");
	}
	
	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		if(args.length < 2) {
			throw new UsageException("Not enough arguments!");
		}
		
		EntityTemplate template = templateService.get(args[1]);
		Map<String,Object> obj = new HashMap<>();
		
		try {
			consoleHelper.promptTemplate(console, obj, template, null, template.getTemplateClass());
			@SuppressWarnings("unused")
			Class<?> baseClass = classLoader.resolveClass(template.getTemplateClass());
			
		} catch (ParseException | PermissionDeniedException | IOException | ClassNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		
		switch(line.wordIndex()) {
		case 1:
			for(EntityTemplate t : templateService.singletons()) {
				candidates.add(new Candidate(t.getResourceKey()));
			}
			break;
		default:
			break;
		}
	}
}
