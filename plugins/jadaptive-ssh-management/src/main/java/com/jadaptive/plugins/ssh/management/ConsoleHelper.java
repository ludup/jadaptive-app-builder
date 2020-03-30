package com.jadaptive.plugins.ssh.management;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.utils.Utils;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.util.IOUtils;
import com.sshtools.server.vsession.VirtualConsole;

@Component
public class ConsoleHelper {

	@Autowired
	private EntityTemplateService templateService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	public Map<String, Object> promptTemplate(VirtualConsole console,
			Map<String, Object> obj,
			EntityTemplate template, 
			String objectType) throws ParseException, PermissionDeniedException, IOException {
		
		obj.put("_clz", objectType);
		for(FieldTemplate field : template.getFields()) {
			switch(field.getFieldType()) {
			case OBJECT_EMBEDDED:
				EntityTemplate objectTemplate = templateService.get(field.getValidationValue(ValidationType.RESOURCE_KEY));
				console.println(objectTemplate.getName());
				obj.put(field.getResourceKey(), promptTemplate(console, new HashMap<>(),
						objectTemplate, 
						field.getValidationValue(ValidationType.OBJECT_TYPE)));
				break;
			case PASSWORD:
				obj.put(field.getResourceKey(), 
						console.getLineReader().readLine(
								String.format("%s: ", field.getName()), '*'));
				break;
			case TEXT:
				obj.put(field.getResourceKey(), console.readLine(
						String.format("%s: ", field.getName())));
				break;
			case TEXT_AREA:
			{
				while(true) {
					console.println("Enter path to ".concat(field.getName()));
					String filename = console.readLine("Path: ");
					
					if(StringUtils.isNotBlank(filename)) {
						AbstractFile file = console.getCurrentDirectory().resolveFile(filename);
						
						if(!file.exists())  {
							console.println(String.format("%s does not exist", filename));
						}
						
						obj.put(field.getResourceKey(), IOUtils.readUTF8StringFromStream(file.getInputStream()));
						break;
					} else if(field.isRequired()) {
						console.println(String.format("%s is required", field.getName()));
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
					val = console.readLine(String.format("%s: ", field.getName()));
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
					val = console.readLine(String.format("%s (y/n): ", field.getName()));		 
				} while(!validAnswers.contains(val.toLowerCase()));
				obj.put(field.getResourceKey(), val);
				break;
			}
			case ENUM:
			{
				console.println("Select ".concat(field.getName()).concat(" from the list (type name or index number)"));
				String enumType = field.getValidationValue(ValidationType.OBJECT_TYPE);
				try {
					@SuppressWarnings("unchecked")
					Class<? extends Enum<?>> clz = (Class<? extends Enum<?>>) applicationService.resolveClass(enumType);
					
					List<String> values = new ArrayList<>();
					Enum<?>[] constants = clz.getEnumConstants();
					int maximumSize = 0;
					for(Enum<?> e : constants) {
						values.add(e.name());
						maximumSize = Math.max(maximumSize, e.name().length());
					}
					
					int columns = console.getTerminal().getSize().getColumns();
					maximumSize += 8;
					int perLine = (columns / maximumSize) - 1;
					int i = 0;
					int y = 0;
					for(String name : values) {
						if(++y > perLine) {
							y = 1;
							console.println();
						}
						console.print(StringUtils.rightPad(String.format("%02d. %s ", ++i, name), maximumSize));
					}
					console.println();
					String val;
					while(true) {
						val = console.readLine(String.format("%s: ", field.getName()));
						if(NumberUtils.isNumber(val)) {
							int idx = Integer.parseInt(val);
							if(idx > 0 && idx <= values.size()) {
								val = values.get(idx-1);
								break;
							}
						} else if(values.contains(val)) {
							break;
						}
						console.println("Invalid value. Try again.");
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
					val = console.readLine(String.format("%s: ", field.getName()));
					try {
						obj.put(field.getResourceKey(), Long.parseLong(val));
						break;
					} catch(NumberFormatException e) {
						console.println(String.format("Invalid entry: %s expecting long value but got %s instead", field.getName(), val));
					}
				}
				break;
			}
			case INTEGER:
			{
				String val; 
				while(true) {
					val = console.readLine(String.format("%s: ", field.getName()));
					try {
						obj.put(field.getResourceKey(), Integer.parseInt(val));
						break;
					} catch(NumberFormatException e) {
						console.println(String.format("Invalid entry: %s expecting int value but got %s instead", field.getName(), val));
					}
				}
				break;
			}
			case TIMESTAMP:
			{
				String val; 
				while(true) {
					val = console.readLine(String.format("%s: ", field.getName()));
					try {
						obj.put(field.getResourceKey(), Utils.parseDate(val, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
						break;
					} catch(IllegalStateException e) {
						throw new IOException(String.format("%s is a timestamp field and %s is not a timestamp", field.getName(), val));
					}
				}
				break;
			}
			default:
				
			}
		}
			
		return obj;	
		
	}
}
