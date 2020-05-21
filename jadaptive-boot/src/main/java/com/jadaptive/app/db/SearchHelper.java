package com.jadaptive.app.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.UserUtils;

@Component
public class SearchHelper extends AuthenticatedService {

	@Autowired
	private PermissionService permissionService; 
	
	public static final Pattern MATCH_EQUALS =  Pattern.compile("^([a-zA-Z]+)=(.+)$");
	public static final Pattern MATCH_LIKE =  Pattern.compile("^[a-zA-Z]\\*=(.*)$");
	public static final Pattern MATCH_IN =  Pattern.compile("^([a-zA-Z]+)\\s(in)\\((.*)\\)$");
	public static final Pattern VARIABLE = Pattern.compile("\\$\\{([a-zA-Z]*)\\}");
	
	public SearchField[] parseFilterField(String filter) {
		
		if(Objects.isNull(filter))
			return new SearchField[0];
		
		String[] searches = filter.split("&");
		List<SearchField> results = new ArrayList<>();
		for(String search : searches) {
			Matcher m = MATCH_EQUALS.matcher(search);
			if(m.matches()) {
				String searchField = m.group(1);
				String searchValue = m.group(2);
				results.add(SearchField.eq(searchField, searchValue));
				continue;
			}
			m = MATCH_LIKE.matcher(search);
			if(m.matches()) {
				String searchField = m.group(1);
				String searchValue = m.group(2);
				results.add(SearchField.like(searchField, searchValue));
				continue;
			}
			m = MATCH_IN.matcher(search);
			if(m.matches()) {
				String searchField = m.group(1);
				String searchValue = m.group(3);
				results.add(SearchField.in(searchField, expandValues(searchValue)));
				continue;
			}
		}
		
		return results.toArray(new SearchField[0]);
	}

	private Object[] expandValues(String values) {
		
		List<Object> tmp = new ArrayList<>();
		for(String s : values.split(",")) {
			 Matcher m = VARIABLE.matcher(s);
			 if(m.matches()) {
				 expandVariable(m.group(1), tmp);
			 } else {
				 tmp.add(s);
			 }
		}
		return tmp.toArray(new String[0]);
	}

	private void expandVariable(String name, List<Object> tmp) {
		
		switch(name) {
		case "permissions":
			tmp.addAll(permissionService.resolveCurrentPermissions());
			break;
		case "currentUser.uuid":
			tmp.add(getCurrentUser().getUuid());
			break;
		case "currentUser.username":
			tmp.add(getCurrentUser().getUsername());
			break;
		case "currentUser.email":
			tmp.add(UserUtils.getEmailAddress(getCurrentUser()));
			break;
		default:
			break;
		}
		
	}
}
