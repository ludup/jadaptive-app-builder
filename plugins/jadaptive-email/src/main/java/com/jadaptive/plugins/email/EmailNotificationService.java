package com.jadaptive.plugins.email;

import java.util.List;

import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.MailException;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.template.ValidationException;

public interface EmailNotificationService {
	
	static final String RESOURCE_BUNDLE = "EmailService";
	
	boolean validateEmailAddress(String email);

	boolean validateEmailAddresses(String[] emails);

	String populateEmailList(String[] emails, List<RecipientHolder> recipients,
			RecipientType type) throws ValidationException;

	void sendEmail(String subject, String text, String html, RecipientHolder[] recipients,
			boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, AccessDeniedException, ValidationException;


	void sendEmail(String subject, String text, String html, String replyToName, String replyToEmail,
			RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments)
			throws MailException, ValidationException, AccessDeniedException;

	boolean isEnabled();

	String getEmailName(String val);

	String getEmailAddress(String val) throws ValidationException;

}
