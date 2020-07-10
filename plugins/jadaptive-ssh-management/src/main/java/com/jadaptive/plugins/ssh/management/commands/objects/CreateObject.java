package com.jadaptive.plugins.ssh.management.commands.objects;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.plugins.sshd.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateObject extends AbstractTenantAwareCommand {

	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper;
	
	@Autowired
	private ObjectService objectService; 
	
	@Autowired
	private I18nService i18n;
	
	public CreateObject() {
		super("create-object", "Objects", "create-object <resourceKey>", "Create an object from its template");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		if(args.length!=2) {
			throw new UsageException("Too few, or too many arguments");
		}
		
		String resourceKey = args[1];
		
		ObjectTemplate template = templateService.get(resourceKey);

		AbstractObject obj = objectService.createNew(template);
		try {
			consoleHelper.promptTemplate(console, obj.getDocument(), template, new ArrayList<>(), template.getTemplateClass());

			objectService.saveOrUpdate(obj);
			
			console.println(String.format("Created %s",
					i18n.format("i18n/" + template.getResourceKey(), Locale.getDefault(),
					String.format("%s.name", template.getResourceKey()))));
		
		} catch (ParseException | PermissionDeniedException e) {
			throw new IOException(e.getMessage(), e);
		}

		
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		switch(line.wordIndex()) {
		case 1:
			for(ObjectTemplate template : templateService.allCollectionTemplates()) {
				candidates.add(new Candidate(template.getResourceKey()));
			}
			break;
		default:
		}
	}

}
