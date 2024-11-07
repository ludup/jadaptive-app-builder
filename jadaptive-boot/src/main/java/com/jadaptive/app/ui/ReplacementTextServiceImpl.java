package com.jadaptive.app.ui;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.I18N;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.ui.editor.ReplaceTextContentEdit;

@Service
public class ReplacementTextServiceImpl implements ReplacementTextService, StartupAware {

	@Autowired
	private EventService eventService;
	
	@Autowired
	private SystemOnlyObjectDatabase<ReplaceTextContentEdit> replacementDatabase;

	@Override
	public void onApplicationStartup() {
		
		for(ReplaceTextContentEdit replacement : replacementDatabase.list(ReplaceTextContentEdit.class)) {
			addReplacement(replacement);
		}
		
		eventService.saved(ReplaceTextContentEdit.class, (e)->{
			addReplacement(e.getObject());
		});
		
		eventService.deleted(ReplaceTextContentEdit.class, (e)->{
			deleteReplacement(e.getObject());
		});
	}

	private void deleteReplacement(ReplaceTextContentEdit replacement) {
		Locale locale = Locale.getDefault();
		if(StringUtils.isNotBlank(replacement.getLocale())) {
			locale = Locale.forLanguageTag(replacement.getLocale());
		}
		I18N.removeI18n(locale, replacement.getBundle(), replacement.getKey());
	}

	private void addReplacement(ReplaceTextContentEdit replacement) {
		Locale locale = Locale.getDefault();
		if(StringUtils.isNotBlank(replacement.getLocale())) {
			locale = Locale.forLanguageTag(replacement.getLocale());
		}
		I18N.addI18n(locale, replacement.getBundle(), replacement.getKey(), replacement.getReplacementText());
		
	} 
	
	
	
}
