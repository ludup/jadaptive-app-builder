package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.csv.CsvImportService;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class ImportCsv extends AbstractTenantAwareCommand {
	
	@Autowired
	private CsvImportService importService; 
	
	@Autowired
	private EntityTemplateService templateService; 
	
	public ImportCsv() {
		super("import-csv", "Object Management", UsageHelper.build("import-csv [options]",
				"-t, --template                       The template to import into",
				"-f, --file                           The file to import",
				"-d, --delim                          The delimiter character (default ',')",
				"-q, --quote-char                     The quote character (default '\"'",
				"-h, --headers-present                The first row contains headers",
				"-c, --skip-comments                  Skip comments",
				"-m, --max-lines-per-row              Maximum number of lines that make up a row",
				"-s, --spaces-needs-quotes            Surrounding spaces needs quotes",
				"-i, --ignore-empty-lines             Ignore empty lines"),
				"Import a CSV file");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} 
		
		char quoteChar = CliHelper.getValue(args, 'q', "quote-char", "\"").charAt(0);
		char delimiterChar = CliHelper.getValue(args, 'd', "delim", ",").charAt(0);
		boolean ignoreEmptyLines = CliHelper.hasOption(args, 'i', "ignore-empty-lines");
		int maxLinesPerRow = Integer.parseInt(CliHelper.getValue(args, 'm', "max-lines-per-row", "0"));
		boolean surroundingSpacesNeedQuotes = CliHelper.hasOption(args, 's', "spaces-needs-quotes");
		boolean skipComments = CliHelper.hasOption(args, 'c', "skip-comments");
		boolean containsHeader = CliHelper.hasOption(args, 'h', "headers-present");
		
		String resourceKey = CliHelper.getValue(args, 't', "template");
		String filename = CliHelper.getValue(args, 'f', "file");
		
		EntityTemplate template = templateService.get(resourceKey);
		
		String[] orderedFields = args[args.length-1].split(",");
		
		AbstractFileFactory<?> factory = console.getConnection().getContext().getPolicy(
				FileSystemPolicy.class).getFileFactory(console.getConnection());
		AbstractFile f = factory.getFile(filename, console.getConnection());
		if(!f.exists()) {
			throw new IOException(String.format("%s does not exist!", filename));
		}
		InputStream in = f.getInputStream();
		
		/**
		 * TODO: No session for SSH yet.
		 */
		
		importService.prepareCallback((long count, String... values) -> {
			if((count % 50)==0) {
				console.print('.');
			}
			if((count % 1000)==0) {
				console.println(String.format(". %d", count));
			}
		});
		
		console.println();
		
		long count = importService.importCsv(template, 
				in, quoteChar, delimiterChar, 
				ignoreEmptyLines, maxLinesPerRow, 
				surroundingSpacesNeedQuotes, skipComments, 
				containsHeader, orderedFields);
		
		console.println(String.format("Imported %d objects", count));
	}
}
