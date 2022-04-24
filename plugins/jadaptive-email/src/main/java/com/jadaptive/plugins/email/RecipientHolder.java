package com.jadaptive.plugins.email;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.jadaptive.api.user.User;

public class RecipientHolder {

	private String name;
	private String email;
	private User principal;
	
	static Set<String> salutations = new HashSet<String>(Arrays.asList("MR", "MS", "MRS", "DR", "PROF"));
	
	public RecipientHolder(String name) {
		
		Pattern depArrHours = Pattern.compile("(?:\"?([^\"]*)\"?\\s)?(?:<?(.+@[^>]+)>?)");
		Matcher matcher = depArrHours.matcher(name);
		if(matcher.find()) {
			this.name = StringUtils.defaultString(matcher.group(1));
			this.email = StringUtils.defaultString(matcher.group(2));
		} else {
			this.email = name;
		}	
	}
	
	public RecipientHolder(String name, String email) {
		this.name = name;
		this.email = email;
	}
	
	public RecipientHolder(User principal, String email) {
		this.name = principal.getName();
		this.email = email;
		this.principal = principal;
	}
	
	public RecipientHolder(User principal) {
		this.name = principal.getName();
		this.email = principal.getEmail();
		this.principal = principal;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name==null ? "" : name;
	}
	
	public String getFirstName() {
		String name = getName();
		if(StringUtils.isNotBlank(name)) {
			int idx = name.indexOf(',');
			if(idx != -1) {
				return name.substring(idx + 1).trim();
			}
			else {
				idx = name.indexOf(' ');
				if(idx > 0) {
					String firstName = name.substring(0,  idx);
					int idx2 = name.indexOf(' ', idx+1);
					if(salutations.contains(firstName.toUpperCase()) && idx2 > 0) {
						firstName = name.substring(idx+1, idx2);
					}
					return firstName;
				}
			}
			return name;
		}
		return "";
	}
	
	public boolean hasUserObject() {
		return !Objects.isNull(principal);
	}
	
	public User getUser() {
		return principal;
	}
	
	public String getPrincipalId() {
		if(principal==null) {
			return "";
		}
		return principal.getUuid();
	}

	@Override
	public String toString() {
		return String.format("%s <%s>", name, email);
	}
	
	public static void main(String[] srgs) {
		
		new RecipientHolder("Lee Painter <lee@javassh.com>");
		new RecipientHolder("Lee <lee@javassh.com>");
		new RecipientHolder("\"Lee Painter\" <lee@javassh.com>");
		new RecipientHolder("\"Lee Painter (Testing Account)\" <test@javassh.com>");
		new RecipientHolder("test@javassh.com");
		new RecipientHolder("");
	}
}
