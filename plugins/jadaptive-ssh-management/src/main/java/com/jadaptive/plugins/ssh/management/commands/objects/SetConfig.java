package com.jadaptive.plugins.ssh.management.commands.objects;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.plugins.sshd.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class SetConfig extends AbstractTenantAwareCommand {
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper;
	
	@Autowired
	private ObjectService objectService; 
	
	public SetConfig() {
		super("set-config", "Object Management", UsageHelper.build("set-config <template>"),
				"Configure a singleton (configuration) object");
	}
	
	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		if(args.length !=2 && args.length != 3) {
			throw new UsageException("Not enough arguments!");
		}
		
		ObjectTemplate template = templateService.get(args[1]);
		
		AbstractObject e = objectService.getSingleton(template.getResourceKey()); 
		
		if(args.length == 3) {
			String fieldName = args[2];
			
			try {
				consoleHelper.promptField(console, template.getField(fieldName), e.getDocument(), template, null, template.getTemplateClass());			
			} catch (ParseException | PermissionDeniedException | IOException ex) {
				throw new IOException(ex.getMessage(), ex);
			}
		} else {
			
			try {
				consoleHelper.promptTemplate(console, e.getDocument(), template, null, template.getTemplateClass());			
			} catch (ParseException | PermissionDeniedException | IOException ex) {
				throw new IOException(ex.getMessage(), ex);
			}
		}
		
		objectService.saveOrUpdate(e);
		
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		
		switch(line.wordIndex()) {
		case 1:
			for(ObjectTemplate t : templateService.singletons()) {
				candidates.add(new Candidate(t.getResourceKey()));
			}
			break;
		case 2:
			ObjectTemplate t = templateService.get(line.words().get(1));
			for(FieldTemplate d : t.getFields()) {
				candidates.add(new Candidate(d.getResourceKey()));
			}
		default:
			break;
		}
	}
}
