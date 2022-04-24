package com.jadaptive.plugins.email;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
public class EmailNotificationServiceImpl extends AuthenticatedService implements EmailNotificationService {
	
	private final static List<String> NO_REPLY_ADDRESSES = Arrays.asList("noreply", "no.reply", "no-reply", "no_reply", "do_not_reply", "do.not.reply", "do_not_reply");
	
	@Autowired
	private SingletonObjectDatabase<SMTPConfiguration> smtpDatabase;
	
	@Autowired
	private EncryptionService encryptionService; 
	
	@Autowired
	private UserService userService; 
	
	static Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	public static final String OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX = "OGIAU";

	
	@Override
	@SafeVarargs
	public final void sendEmail(String subject, String html, RecipientHolder[] recipients, boolean archive, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(subject, html, "", "", recipients, archive, attachments);
	}

	private SMTPConfiguration getConfig() {
		return smtpDatabase.getObject(SMTPConfiguration.class);
	}
	
	Mailer createMailer() {
		SMTPConfiguration config = getConfig();
		

		Properties properties = new Properties();
	    properties.put("mail.smtp.ssl.trust", "*");
	    
		Mailer mail;
		
		if(StringUtils.isNotBlank(config.getUsername())) {
			mail = MailerBuilder.withSMTPServer(config.getHostname(), config.getPort())
					.withSMTPServerUsername(config.getUsername())
					.withSMTPServerPassword(encryptionService.decrypt(config.getPassword()))
					.withTransportStrategy(config.getProtocol())
					.withProperties(properties)
					.trustingAllHosts(true)
					.withSessionTimeout(60000)
					.buildMailer();
		} else {
			mail = MailerBuilder.withSMTPServer(config.getHostname(), config.getPort())
					.withTransportStrategy(config.getProtocol())
					.withProperties(properties)
					.trustingAllHosts(true)
					.withSessionTimeout(60000)
					.buildMailer();
		}
		
		return mail;
	}
	@Override
	@SafeVarargs
	public final void sendEmail(
			String recipeintSubject, 
			String receipientHtml, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder[] recipients, 
			boolean archive,
			EmailAttachment... attachments) throws MailException, ValidationException, AccessDeniedException {
		
		SMTPConfiguration config = getConfig();
		
		if(!isEnabled()) {
			log.warn("This system has globally disabled email. Remove the -Djadaptive.disableEmail=true sysytem property to re-enable");
			return;
		}
		
		if(!config.getEnabled()) {
			log.warn("Sending messages is disabled. Check the Enabled option under SMTP Configuration to allow sending of emails");
			return;
		}
		
		Mailer mail = createMailer();
		
		boolean noNoReply = true;

		for(RecipientHolder r : recipients) {
			
			if(StringUtils.isBlank(r.getEmail())) {
				log.warn(String.format("Missing email address for %s", r.getName()));
				continue;
			}
			
			if(noNoReply && isNoReply(StringUtils.left(r.getEmail(), r.getEmail().indexOf('@')))) {
				log.warn(String.format("Skipping no reply email address for %s", r.getEmail()));
				continue;
			}
			
			
//			ServerResolver serverResolver = new ServerResolver(realm);
			
			String messageSubject = processDefaultReplacements(recipeintSubject, r);

			if(StringUtils.isNotBlank(receipientHtml)) {
			
				/**
				 * Send a HTML email and generate tracking if required. Make sure
				 * only the recipients get a tracking email. We don't want to 
				 * track the archive emails.
				 */
//				String trackingImage = "";
//				String nonTrackingUri = "";
				
				String messageHtml = processDefaultReplacements(receipientHtml, r);
//				messageHtml = messageHtml.replace("${htmlTitle}", messageSubject);
//				
//				String archiveRecipientHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
//
//				if(track && StringUtils.isNotBlank(trackingImage)) {
//					String trackingUri = "";
//					messageHtml = messageHtml.replace("__trackingImage__", trackingUri);
//				} else {
//					messageHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
//				}

				send(mail, 
						messageSubject, 
						messageHtml, 
						replyToName, 
						replyToEmail, 
						r, 
						archive,
						attachments);
				
//				for(RecipientHolder recipient : archiveRecipients) {
//					send(mail, messageSubject, messageText, archiveRecipientHtml, 
//							replyToName, replyToEmail, recipient, delay, context, attachments);
//				}
				
			} else {
			
				/**
				 * Send plain email without any tracking
				 */
				send(mail, 
						messageSubject, 
						"", 
						replyToName, 
						replyToEmail, 
						r, 
						archive,
						attachments);
				
//				for(RecipientHolder recipient : archiveRecipients) {
//					send(mail, messageSubject, messageText, "", 
//							replyToName, replyToEmail, recipient, delay, context, attachments);
//				}
			}
		}
	}
	
	private boolean isNoReply(String addr) {
		for(String a : NO_REPLY_ADDRESSES) {
			if(addr.startsWith(a))
				return true;
		}
		return false;
	}
	
	private String processDefaultReplacements(String str, RecipientHolder r) {
		str = str.replace("${email}", r.getEmail());
		str = str.replace("${firstName}", r.getFirstName());
		str = str.replace("${fullName}", r.getName());
		str = str.replace("${principalId}", r.getPrincipalId());
		
		return str;
	}

	
	public final boolean isEnabled() {
		return !Boolean.getBoolean("jadaptive.disableEmail");
	}
	
	private void send(Mailer mail,
			String subject,  
			String htmlText, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder r, 
			boolean archive,
			EmailAttachment... attachments) throws AccessDeniedException, ValidationException {
		
		if(!isEnabled()) {
			log.debug("Not sending email because the system property jadaptive.disableEmail is enabled (true).");
			return;
		}
		
		@SuppressWarnings("unused")
		User p = null;
		SMTPConfiguration config = getConfig();
		
		if(r.hasUserObject()) {
			p = r.getUser();
		} else {
			try {
				p = userService.getUserByEmail(r.getEmail());
			} catch (ObjectNotFoundException e) {
			}
		}
		
//		if(p!=null) {
//			if(realmService.getUserPropertyBoolean(p, "user.bannedEmail")) {
//				if(log.isInfoEnabled()) {
//					log.info("Email to principal {} is banned", r.getEmail());
//				}
//				return;
//			}
//		}
		
		String fromAddress = config.getFromAddress();
		if(r.getEmail().equalsIgnoreCase(fromAddress)) {
			if(log.isInfoEnabled()) {
				log.info("Email loopback detected. The from address {} is the same as the destination", fromAddress, r.getEmail());
			}
			return;
		}
		
		EmailPopulatingBuilder builder = EmailBuilder.startingBlank();
		builder.from(config.getFromName(), fromAddress);
		
		String archiveAddress = config.getArchiveAddress();
		if(archive && StringUtils.isNotBlank(archiveAddress)) {
			builder.bcc("", archiveAddress);
		}
		
//		List<String> blocked = Arrays.asList(configurationService.getValues(realm, "email.blocked"));
//		for(String emailAddress : blocked) {
//			if(r.getEmail().equalsIgnoreCase(emailAddress)) {
//				if(log.isInfoEnabled()) {
//					log.info("Email blocked. The destination address {} is blocked", emailAddress);
//				}
//				return;
//			}
//		}
		

		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			builder.withReplyTo(archiveAddress, fromAddress);
		} else if(StringUtils.isNotBlank(config.getReplyToName())
				&& StringUtils.isNotBlank(config.getReplyToAddress())) {
			builder.withReplyTo(config.getReplyToName(), config.getReplyToAddress());
		}
		
		builder.to(r.getName(), r.getEmail());
		
		builder.withSubject(subject);

		Document doc = Jsoup.parse(htmlText);
		try {
			for (Element el : doc.select("img")) {
				String src = el.attr("src");
				if(src != null && src.startsWith("data:")) {
					int idx = src.indexOf(':');
					src = src.substring(idx + 1);
					idx = src.indexOf(';');
					String mime = src.substring(0, idx);
					src = src.substring(idx + 1);
					idx = src.indexOf(',');
					String enc = src.substring(0, idx);
					String data = src.substring(idx + 1);
					if(!"base64".equals(enc)) {
						throw new UnsupportedOperationException(String.format("%s is not supported for embedded images.", enc));
					}
					byte[] bytes = Base64.decodeBase64(data);
					UUID cid = UUID.randomUUID();
					el.attr("src", "cid:" + OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid);
					builder.withEmbeddedImage(OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid.toString(), bytes, mime);
				}
			}
		}
		catch(Exception e) {
			log.error(String.format("Failed to parse embedded images in email %s to %s.", subject, r.getEmail()), e);
		}
		
		builder.withHTMLText(doc.toString());
		
		
		if(attachments!=null) {
			for(EmailAttachment attachment : attachments) {
				builder.withAttachment(attachment.getName(), attachment);
			}
		}
		
		try {
			
			mail.sendMail(builder.buildEmail(), true).whenComplete((result, ex) -> {
//				if (ex != null) {
//					eventService.publishEvent(new EmailEvent(this, e, realm, subject, plainText, r.getEmail(), context));
//				} else {
//					eventService.publishEvent(new EmailEvent(this, realm, subject, plainText, r.getEmail(), context));
//				}
			});
			

		} catch (MailException e) {
			throw e;
		}

	}
	
	@Override
	public boolean validateEmailAddresses(String[] emails) {
		for(String email : emails) {
			if(!validateEmailAddress(email)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean validateEmailAddress(String val) {
		
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return true;
			} else {
				return false;
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return true;
		}

		// Not an email address? Is this a principal of the realm?
		User user = userService.getUser(val);
		
		
		if(user!=null) {
			return StringUtils.isNotBlank(user.getEmail());
		}
		
		return false;
	}
	
	private void populateEmail(String val, List<RecipientHolder> recipients) throws ValidationException {

		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);
		User user = null;
		
		if (m.find()) {
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (!Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				throw new ValidationException(email
						+ " is not a valid email address");
			}
			
			name = WordUtils.capitalize(name.replace('.',  ' ').replace('_', ' '));
			
			try {
				user = userService.getUserByEmail(email);
			} catch(ObjectNotFoundException e) { }
		} else {

			// Not an email address? Is this a principal of the realm?
			try {
				user = userService.getUser(val);
			} catch(ObjectNotFoundException e) { }
			
		}
		
		if(user==null) {
			try {
				user = userService.getUserByEmail(val);
			} catch (ObjectNotFoundException e) {
			}
		}
		
//		if(principal==null) {
//			try {
//				principal = realmService.getPrincipalById(getCurrentRealm(), Long.parseLong(val), PrincipalType.USER);
//			} catch(AccessDeniedException | NumberFormatException e) { };
//		}
		
		if(user!=null) {
			String email = user.getEmail();
			if(StringUtils.isNotBlank(email)) {
				recipients.add(new RecipientHolder(user, email));
			}
			
		}
		
		if(Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			recipients.add(new RecipientHolder("", val));
			return;
		}
		throw new ValidationException(val
				+ " is not a valid email address");
	}
	
	@Override
	public String populateEmailList(String[] emails, 
			List<RecipientHolder> recipients,
			RecipientType type)
			throws ValidationException {

		StringBuffer ret = new StringBuffer();

		for (String email : emails) {

			if (ret.length() > 0) {
				ret.append(", ");
			}
			ret.append(email);
			populateEmail(email, recipients);
		}

		return ret.toString();
	}
	
	@Override
	public String getEmailName(String val) {
		
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			return m.group(1).replaceAll("[\\n\\r]+", "");
		}
		
		return "";
	}
	
	@Override
	public String getEmailAddress(String val) throws ValidationException {
		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);

		if (m.find()) {
			@SuppressWarnings("unused")
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				return email;
			} else {
				throw new ValidationException();
			}
		}

		if (Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, val)) {
			return val;
		}
		
		throw new ValidationException();
	}
}
