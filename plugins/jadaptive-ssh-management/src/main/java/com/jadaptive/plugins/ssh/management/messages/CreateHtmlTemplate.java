package com.jadaptive.plugins.ssh.management.messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.plugins.email.HTMLTemplate;
import com.jadaptive.plugins.email.HTMLTemplateService;
import com.jadaptive.plugins.sshd.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.util.IOUtils;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateHtmlTemplate extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private ConsoleHelper consoleHelper;
	
	public CreateHtmlTemplate() {
		super("create-html-template", "Message Templates",
					UsageHelper.build("create-html-template <name>"), 
					"Create a HTML template");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		String name = "";
		if(args.length < 2) {
			name = consoleHelper.promptString(console, "Name: ");
		} else {
			name = args[1];
		}
		
		HTMLTemplateService templateService = applicationService.getBean(HTMLTemplateService.class);
		
		AbstractFile file = consoleHelper.promptFile(console, "HTML File: ", null, console.getCurrentDirectory(), true, false);
		
		HTMLTemplate template = new HTMLTemplate();
		
		if(!file.exists()) {
			throw new FileNotFoundException(args[2] + " is not a valid file");
		}
		
		try(InputStream in = file.getInputStream()) {
			template.setHtml(IOUtils.readStringFromStream(in, "UTF-8"));
		} 
		
		String contentSelector = consoleHelper.promptString(console, "Content Selector: ");
		String shortName = consoleHelper.promptString(console, "Short Name: ");
		
		
		template.setName(name);
		template.setShortName(shortName);
		template.setContentSelector(contentSelector);
		templateService.saveTemplate(template);

	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		
		switch(line.wordIndex()) {
		case 1:
			for(HTMLTemplate template : applicationService.getBean(HTMLTemplateService.class).allTemplates()) {
				candidates.add(new Candidate(template.getShortName()));
			}
			break;
		case 2:
			fillCurrentDirectoryCandidates(candidates);
			break;
		default:
			break;
		}
	}

	
}
