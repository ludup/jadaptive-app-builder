package com.jadaptive.plugins.ssh.management.messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.plugins.email.HTMLTemplate;
import com.jadaptive.plugins.email.HTMLTemplateService;
import com.jadaptive.plugins.email.Message;
import com.jadaptive.plugins.email.MessageService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.util.IOUtils;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class ExportHtmlTemplate extends AbstractTenantAwareCommand {
	
	@Autowired
	private ApplicationService applicationService; 
	
	public ExportHtmlTemplate() {
		super("export-html-template", "Message Templates",
					UsageHelper.build("export-html-template <shortName> <file>"), 
					"Export the HTML of a HTML template");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length != 3) {
			throw new UsageException("There are too many, or too few arguments");
		}
		
		HTMLTemplateService templateService = applicationService.getBean(HTMLTemplateService.class);
		
		String name = args[1];
		AbstractFile file = console.getCurrentDirectory().resolveFile(args[2]);
		
		HTMLTemplate template = templateService.getTemplateByShortName(name);
		
		try(OutputStream out = file.getOutputStream()) {
			IOUtils.writeStringToStream(out, template.getHtml(), "UTF-8");
		} 

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
