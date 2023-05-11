package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jline.reader.Candidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.tasks.TriggerMapping;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.utils.Utils;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.util.IOUtils;
import com.sshtools.server.vsession.VirtualConsole;

@Component
public class ConsoleHelper {

	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private I18nService i18n; 
	
	public String promptString(VirtualConsole console,
			String prompt) {
		return promptString(console, prompt, null, null, null);
	}
	
	public String promptPassword(VirtualConsole console,
			String prompt) {
		return promptString(console, prompt, null, null, '*');
	}
	
	public String promptString(VirtualConsole console,
			String prompt,
			String defaultValue) {
		return promptString(console, prompt, defaultValue, null, null);
	}
	
	public String promptString(VirtualConsole console,
			String prompt,
			String defaultValue,
			String regex,
			Character mask) {
		
		String value;
		
		while(true) {
			if(Objects.nonNull(mask)) {
				value = console.readLine(prompt, mask);
			} else {
				value = console.readLine(prompt);
			}
			if(StringUtils.isBlank(value)) {
				if(Objects.nonNull(defaultValue)) {
					return defaultValue;
				} else {
					console.println("A non-empty value is required");
				}
			} else {
				if(Objects.isNull(regex) || value.matches(regex)) {
					return value;
				}
				console.println("Value does not match required input");
			}
		}
	}
	
	public AbstractFile promptFile(VirtualConsole console,
			String prompt,
			AbstractFile defaultValue,
			AbstractFile folder,
			boolean requireValue, 
			boolean allowNonExisting) throws IOException, PermissionDeniedException {
		
		String value;
		
		while(true) {
			value = console.readLine(prompt);
			
			if(StringUtils.isBlank(value)) {
				if(Objects.nonNull(defaultValue)) {
					return defaultValue;
				} else if(requireValue){
					console.println("A non-empty value is required");
				} else {
					return null;
				}
			} else {
				AbstractFile file = folder.resolveFile(value);
				if(file.exists() || allowNonExisting) {
					return file;
				} else {
					console.println(String.format("%s is not a valid file", value));
				}
			}
		}
	}
	
	public int promptInt(VirtualConsole console, 
			String prompt, 
			Integer defaultValue, 
			int min, 
			int max) {
		
		int value;
		
		while(true) {
			String v = console.readLine(prompt);
			if(StringUtils.isBlank(v)) {
				if(Objects.nonNull(defaultValue)) {
					return defaultValue;
				} else {
					console.println(String.format("An integer value is required between %d and %d", min, max));
					continue;
				}
			}
			try {
				value = Integer.parseInt(v);
				if(value < min || value > max) {
					console.println(String.format("Value must be between %d and %d", min, max));
					continue;
				}
				break;
			} catch(NumberFormatException e) {
				console.println(String.format("%s is not a valid entry", v));
			}
		}
		return value;
	}

	public Map<String, Object> promptTemplate(VirtualConsole console,
			Map<String, Object> obj,
			ObjectTemplate template, 
			List<TriggerMapping> mappings,
			String objectType) throws ParseException, PermissionDeniedException, IOException {
		
		obj.put("_clz", objectType);
		for(FieldTemplate field : template.getFields()) {
			promptField(console, field, obj, template, mappings, objectType);
		}
			
		return obj;	
		
	}

	public void promptField(VirtualConsole console,
			FieldTemplate field,
			Map<String, Object> obj,
			ObjectTemplate template, 
			List<TriggerMapping> mappings,
			String objectType) throws ParseException, PermissionDeniedException, IOException {
		
		switch(field.getFieldType()) {
		case OBJECT_EMBEDDED:
			ObjectTemplate objectTemplate = templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
			console.println(objectTemplate.getName());
			obj.put(field.getResourceKey(), promptTemplate(console, new HashMap<>(),
					objectTemplate, 
					mappings,
					field.getValidationValue(ValidationType.OBJECT_TYPE)));
			break;
		case PASSWORD:
		{
			String val = console.getLineReader().readLine(String.format("%s: ",
							getFieldName(template, field)), '*');
			if(processMapping(val, field.getResourceKey(), mappings)) {
				return;
			}
			obj.put(field.getResourceKey(), val);
			break;
		}
		case TEXT:
		{
			String val = console.readLine(
					String.format("%s: ", getFieldName(template, field)));
			if(processMapping(val, field.getResourceKey(), mappings)) {
				return;
			}
			obj.put(field.getResourceKey(), val);
			break;
		}
		case TEXT_AREA:
		{
			while(true) {
				console.println("Enter path to ".concat(getFieldName(template, field)));
				String val = console.readLine("Path: ");
				if(processMapping(val, field.getResourceKey(), mappings)) {
					continue;
				}
				if(StringUtils.isNotBlank(val)) {
					AbstractFile file = console.getCurrentDirectory().resolveFile(val);
					
					if(!file.exists())  {
						console.println(String.format("%s does not exist", val));
					}
					
					obj.put(field.getResourceKey(), IOUtils.readUTF8StringFromStream(file.getInputStream()));
					break;
				} else if(field.isRequired()) {
					console.println(String.format("%s is required", getFieldName(template, field)));
				} else {
					break;
				}
			}

			break;
		}
		case DECIMAL:
		{
			String val; 
			while(true) {
				val = console.readLine(String.format("%s: ", getFieldName(template, field)));
				if(processMapping(val, field.getResourceKey(), mappings)) {
					continue;
				}
				try {
					Double.parseDouble(val);
					break;
				} catch(NumberFormatException e) {
					continue;
				}
			}
			obj.put(field.getResourceKey(), val);
			break;
		}
		case BOOL:
		{
			String val; 
			Set<String> validAnswers = new HashSet<>(Arrays.asList("y", "n", "yes", "no"));
			do {
				val = console.readLine(String.format("%s (y/n): ", getFieldName(template, field)));	

			} while(!val.startsWith(":") && !validAnswers.contains(val.toLowerCase()));
			if(processMapping(val, field.getResourceKey(), mappings)) {
				return;
			}
			obj.put(field.getResourceKey(), val.toLowerCase().startsWith("y"));
			break;
		}
		case ENUM:
		{
			console.println("Select ".concat(getFieldName(template, field)).concat(" (press tab to cycle through values)"));
			String enumType = field.getValidationValue(ValidationType.OBJECT_TYPE);
			
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Enum<?>> clz = (Class<? extends Enum<?>>) applicationService.resolveClass(enumType);
				
				Enum<?>[] constants = clz.getEnumConstants();
				List<String> values = new ArrayList<>();
				List<Candidate> completions = new ArrayList<>();
				for(Enum<?> e : constants) {
					values.add(e.name().toUpperCase());
					completions.add(new Candidate(e.name()));
				}
				console.getEnvironment().put("_COMPLETIONS", completions);
				console.println();
				String val;
				while(true) {
					val = console.readLine(String.format("%s: ", getFieldName(template, field))).trim();
					if(values.contains(val.toUpperCase())) {
						break;
					} else if(val.startsWith(":")) {
						break;
					}
					console.println("Invalid value. Try again.");
				}
				console.getEnvironment().remove("_COMPLETIONS");
				if(processMapping(val, field.getResourceKey(), mappings)) {
					return;
				}
				obj.put(field.getResourceKey(), val);
			} catch (ClassNotFoundException e) {
				throw new IOException(e.getMessage(), e);
			}
			
			break;
		}
		case LONG:
		{
			String val; 
			while(true) {
				val = console.readLine(String.format("%s: ", getFieldName(template, field)));
				if(processMapping(val, field.getResourceKey(), mappings)) {
					break;
				}
				try {
					obj.put(field.getResourceKey(), Long.parseLong(val));
					break;
				} catch(NumberFormatException e) {
					console.println(String.format("Invalid entry: %s expecting long value but got %s instead", 
							getFieldName(template, field), val));
				}
			}
			break;
		}
		case INTEGER:
		{
			String val; 
			while(true) {
				val = console.readLine(String.format("%s: ", getFieldName(template, field)));
				if(processMapping(val, field.getResourceKey(), mappings)) {
					break;
				}
				try {
					obj.put(field.getResourceKey(), Integer.parseInt(val));
					break;
				} catch(NumberFormatException e) {
					console.println(String.format("Invalid entry: %s expecting int value but got %s instead",
							getFieldName(template, field), val));
				}
			}
			break;
		}
		case TIMESTAMP:
		{
			String val; 
			while(true) {
				val = console.readLine(String.format("%s: ", getFieldName(template, field)));
				if(processMapping(val, field.getResourceKey(), mappings)) {
					break;
				}
				try {
					obj.put(field.getResourceKey(), Utils.parseDate(val, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
					break;
				} catch(IllegalStateException e) {
					throw new IOException(String.format("%s is a timestamp field and %s is not a timestamp", getFieldName(template, field), val));
				}
			}
			break;
		}
		default:
			
		}
		
	}

	private String getFieldName(ObjectTemplate template, FieldTemplate field) {
		return i18n.getFieldName(template, field);
	}
	
	@SuppressWarnings("unused")
	private String getFieldDesc(ObjectTemplate template, FieldTemplate field) {
		return i18n.getFieldDesc(template, field);
	}

	private boolean processMapping(String val, String resourceKey, List<TriggerMapping> mappings) {
		
		if(Objects.nonNull(mappings)) {
			if(val.startsWith(":")) {
				mappings.add(new TriggerMapping(resourceKey, val));
				return true;
			}
		}
		return false;
	}

	public void displayTemplate(VirtualConsole console, AbstractObject e, ObjectTemplate template) {
		
		for(FieldTemplate field : template.getFields()) {
			switch(field.getFieldType()) {
			case OBJECT_EMBEDDED:
				ObjectTemplate objectTemplate = templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
				console.println(objectTemplate.getName());
				displayTemplate(console, 
						e.getChild(field),
						template);
				break;
			case PASSWORD:
			{
				console.println(String.format("%-25s: %s", getFieldName(template, field), "**********"));
				break;
			}
			case TEXT:
			case TEXT_AREA:
			case DECIMAL:
			case ENUM:
			case LONG:
			case INTEGER:
			case TIMESTAMP:
			case BOOL:
			{
				console.println(String.format("%-25s: %s", getFieldName(template, field), e.getValue(field).toString()));
				break;
			}
			default:
				
			}
		}
	}

	public boolean promptYesNo(VirtualConsole console, String str) {
		
		Collection<String> validAnswers = Arrays.asList("y", "n", "yes", "no", "Y", "N", "YES", "NO", "Yes", "No");
		String answer;
		do {
			answer = promptString(console, str);
		} while(!validAnswers.contains(answer));
		
		return answer.toLowerCase().startsWith("y");
	}
}
