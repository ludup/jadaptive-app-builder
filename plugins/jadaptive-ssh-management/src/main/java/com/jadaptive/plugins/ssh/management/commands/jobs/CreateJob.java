package com.jadaptive.plugins.ssh.management.commands.jobs;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tasks.TaskService;
import com.jadaptive.api.tasks.TriggerMapping;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.plugins.ssh.management.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateJob extends AbstractTenantAwareCommand {

	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper; 
	
	@Autowired
	private JobService jobService; 
	
	@Autowired
	TaskService taskService; 
	
	public CreateJob() {
		super("create-job", "Automation", 
				UsageHelper.build("create-job <name> <task>"), 
				"Create a job");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		if(args.length < 3) {
			throw new UsageException("You must provide a name and template for this Job");
		}
		
		String name = args[args.length-2];
		String template = args[args.length-1];
		
		Job job = new Job();
		
		try {
			jobService.getJobByName(name);
			console.println(String.format("A job already exists called %s", name));
			return;
		} catch(ObjectNotFoundException e) { }
		job.setName(name);
		
		ObjectTemplate taskTemplate = templateService.get(template);
		
		if(StringUtils.isBlank(taskTemplate.getTemplateClass())) {
			throw new UsageException(
					String.format("%s template does not have a concrete class", 
							taskTemplate.getTemplateClass()));
		}

		Map<String,Object> doc = new HashMap<>();
		try {
			
			List<TriggerMapping> mappings = new ArrayList<>();
			consoleHelper.promptTemplate(console, doc, taskTemplate, mappings, taskTemplate.getTemplateClass());
			
			Task task = templateService.createObject(doc, Task.class);
			
			job.setTask(task);
			jobService.createJob(job);
			console.println(String.format("New job created with ID %d", job.getShortId()));
		} catch (ParseException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		if(line.wordIndex()==2) {
			for(String resourceKey : taskService.getTaskResourceKeys()) {
				candidates.add(new Candidate(resourceKey));
			}
		}
	}
}
