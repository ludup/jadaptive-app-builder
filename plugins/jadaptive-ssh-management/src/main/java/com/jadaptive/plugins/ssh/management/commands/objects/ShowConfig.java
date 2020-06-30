package com.jadaptive.plugins.ssh.management.commands.objects;

import java.io.IOException;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.plugins.sshd.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class ShowConfig extends AbstractTenantAwareCommand {
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper;
	
	@Autowired
	private ObjectService objectService; 
	
	public ShowConfig() {
		super("show-config", "Object Management", UsageHelper.build("show-config <template>"),
				"Display the value a singleton (configuration) object");
	}
	
	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		if(args.length < 2) {
			throw new UsageException("Not enough arguments!");
		}
		
		ObjectTemplate template = templateService.get(args[1]);
		
		AbstractObject e = objectService.getSingleton(template.getResourceKey()); 
		consoleHelper.displayTemplate(console, e, template);
		
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		
		switch(line.wordIndex()) {
		case 1:
			for(ObjectTemplate t : templateService.singletons()) {
				candidates.add(new Candidate(t.getResourceKey()));
			}
			break;
		default:
			break;
		}
	}
}
