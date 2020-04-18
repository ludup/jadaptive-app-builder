package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.jobs.Job;
import com.jadaptive.api.jobs.JobService;
import com.jadaptive.api.tasks.Task;
import com.jadaptive.api.tasks.TaskService;
import com.jadaptive.api.tasks.Trigger;
import com.jadaptive.api.tasks.TriggerType;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.plugins.ssh.management.ConsoleHelper;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class AppendTask extends AbstractTenantAwareCommand {

	@Autowired
	private EntityTemplateService templateService; 
	
	@Autowired
	private ConsoleHelper consoleHelper; 
	
	@Autowired
	private JobService jobService; 
	
	@Autowired
	TaskService taskService; 
	
	public AppendTask() {
		super("append-task", "Automation", 
				UsageHelper.build("append-task <name|id> <task> success|failure|any"), 
				"Append a new task to an existing job");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		if(args.length < 4) {
			throw new UsageException("You must provide a name and template for this Job");
		}
		
		String name = args[args.length-3];
		String template = args[args.length-2];
		TriggerType trigger = null;
		
		try {
			trigger = TriggerType.valueOf(args[args.length-1].toUpperCase());
		} catch(IllegalArgumentException t) {
			console.println(String.format("Incorrect trigger type %s. Use 'success', 'failure' or 'any'.", args[args.length-1]));
			return;
		}
		
		Job job; 
		
		try {
			job = jobService.getJob(name);
		} catch(EntityNotFoundException e) {
			console.println(String.format("There is no job with name or id %s", name));
			return;
		}

		Task task = selectTask(job);
		
		console.println(String.format("Appending to %s task", task.getResourceKey()));
		
		EntityTemplate sourceTemplate = templateService.get(job.getTask().getResourceKey());
		printSourceParameters(sourceTemplate);
		

	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		switch(line.wordIndex()) {
		case 3:
			for(TriggerType type : TriggerType.values()) {
				candidates.add(new Candidate(type.name()));
			}
			break;
		case 2:
			for(String resourceKey : taskService.getTaskResourceKeys()) {
				candidates.add(new Candidate(resourceKey));
			}
			break;
		default:
		}
	}

	private Task selectTask(Job job) {
		
		if(job.getTriggers().isEmpty()) {
			return job.getTask();
		}
		
		Map<Integer,Task> tasks = iterateTasks(job.getTask(), job.getTriggers(), new HashMap<>(), 0);
		while(true) {
			String val = console.readLine("Enter task number: ");
			if(NumberUtils.isNumber(val)) {
				Integer i = Integer.parseInt(val);
				if(tasks.containsKey(i)) {
					return tasks.get(i);
				}
				console.println("No such task, try again");
				continue;
			}
			console.println(String.format("Enter a number from 1 to %d", tasks.size()));
		}
	}

	private Map<Integer, Task> iterateTasks(Task task, List<Trigger> triggers, Map<Integer,Task> tasks, int level) {
		
		Integer i = tasks.size()+1;
		tasks.put(i, task);
		
		console.println(String.format("%02d %s %s", i, StringUtils.leftPad("", level*2), task.getResourceKey()));
		level++;
		
		for(Trigger trigger : triggers) {
			iterateTasks(trigger.getTask(), trigger.getTriggers(), tasks, level);
		}
		
		return tasks;
	}

//	private Trigger createTrigger(TriggerType type, Job job) {
//		
//		Trigger trigger = new Trigger();
//		trigger.setTyoe(type);
//		trigger.setMappings(mappings);
//		
//		return trigger;
//	}

	private void printSourceParameters(EntityTemplate entityTemplate) {
		console.println("You can use the following parameters to reference the output of previous task:");
		printSourceParameters(entityTemplate, "");
		printGlobalParameters();
	}
	
	private void printGlobalParameters() {
		console.println("You can also use the following global parameters:");

	}
	
	private void printSourceParameters(EntityTemplate entityTemplate, String obj) {
		
		for(FieldTemplate field : entityTemplate.getFields()) {
			
			if(StringUtils.isNotBlank(obj)) {
				console.println("${" + obj + "." + field.getResourceKey() + "}");
			} else {
				console.println("${" + field.getResourceKey() + "}");
			}
			
			switch(field.getFieldType()) {
			case OBJECT_EMBEDDED:
			case OBJECT_REFERENCE:
				
				printSourceParameters(templateService.get(
						field.getValidationValue(ValidationType.RESOURCE_KEY)), 
						field.getResourceKey());
				break;
			default:
				break;
			}
		}
	}

//	private Task createTask(String template) throws IOException, PermissionDeniedException, UsageException {
//		
//		EntityTemplate taskTemplate = templateService.get(template);
//		
//		if(StringUtils.isBlank(taskTemplate.getTemplateClass())) {
//			throw new UsageException(
//					String.format("%s template does not have a concrete class", 
//							taskTemplate.getTemplateClass()));
//		}
//
//		Map<String,Object> doc = new HashMap<>();
//		List<String> mappings = new ArrayList<>();
//		try {
//			consoleHelper.promptTemplate(console, doc, taskTemplate, mappings, taskTemplate.getTemplateClass());
//			
//			return templateService.createObject(doc, Task.class);
//
//		} catch (ParseException e) {
//			throw new IOException(e.getMessage(), e);
//		}
//		
//	}

}
