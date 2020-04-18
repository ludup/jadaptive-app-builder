package com.jadaptive.plugins.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.Session;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;
import org.codemonkey.simplejavamail.email.Email;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.encrypt.EncryptionService;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
public class EmailNotificationServiceImpl extends AuthenticatedService implements EmailNotificationService {
	
	private final static List<String> NO_REPLY_ADDRESSES = Arrays.asList("noreply", "no.reply", "no-reply", "no_reply", "do_not_reply", "do.not.reply", "do_not_reply");

	
//	@Autowired
//	private EmailTrackerService trackerService; 
	
//	@Autowired
//	private EventService eventService;
	
	@Autowired
	private SingletonObjectDatabase<SMTPConfiguration> smtpDatabase;
	
	@Autowired
	private EncryptionService encryptionService; 
	
	@Autowired
	private UserService userService; 
	
	static Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

	public final static String SMTP_ENABLED = "smtp.enabled";
	public final static String SMTP_HOST = "smtp.host";
	public final static String SMTP_PORT = "smtp.port";
	public final static String SMTP_PROTOCOL = "smtp.protocol";
	public final static String SMTP_USERNAME = "smtp.username";
	public final static String SMTP_PASSWORD = "smtp.password";
	public final static String SMTP_FROM_ADDRESS = "smtp.fromAddress";
	public final static String SMTP_FROM_NAME = "smtp.fromName";
	public final static String SMTP_REPLY_ADDRESS = "smtp.replyAddress";
	public final static String SMTP_REPLY_NAME = "smtp.replyName";
	public final static String SMTP_DO_NOT_SEND_TO_NO_REPLY = "smtp.doNotSendToNoReply";
	
	public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
	
	public static final String EMAIL_NAME_PATTERN = "(.*?)<([^>]+)>\\s*,?";

	public static final String OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX = "OGIAU";

	@Override
	@SafeVarargs
	public final void sendEmail(String subject, String text, String html, RecipientHolder[] recipients, boolean archive, boolean track, int delay, String context, EmailAttachment... attachments) throws MailException, AccessDeniedException, ValidationException {
		sendEmail(subject, text, html, recipients, archive, track, delay, context, attachments);
	}

	private SMTPConfiguration getConfig() {
		return smtpDatabase.getObject(SMTPConfiguration.class);
	}
	
	private Session createSession() {
		
		SMTPConfiguration config = getConfig();
		
		Properties properties = new Properties();
	    properties.put("mail.smtp.auth", "false");
	    properties.put("mail.smtp.starttls.enable", config.getProtocol()==TransportStrategy.SMTP_PLAIN ? "false" : "true");
	    properties.put("mail.smtp.host", config.getHostname());
	    properties.put("mail.smtp.port", config.getPort());

	    return Session.getInstance(properties);
	}
	
	@Override
	@SafeVarargs
	public final void sendEmail(
			String recipeintSubject, 
			String receipientText, 
			String receipientHtml, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder[] recipients, 
			boolean archive,
			boolean track,
			int delay,
			String context, 
			EmailAttachment... attachments) throws MailException, ValidationException, AccessDeniedException {
		
		SMTPConfiguration config = getConfig();
		
		if(!isEnabled()) {
			if(!config.getEnabled()) {
				log.warn("This system is not allowed to send email messages.");
			} else {
				log.warn("Sending messages is disabled. Enable SMTP settings in System realm to allow sending of emails");
			}
			return;
		}
		
		Mailer mail;
		
		if(StringUtils.isNotBlank(config.getUsername())) {
			mail = new Mailer(config.getHostname(), 
					config.getPort(), 
					config.getUsername(),
					encryptionService.decrypt(config.getPassword()),
					config.getProtocol());
		} else {
			mail = new Mailer(createSession());
		}
		
		String archiveAddress = config.getArchiveAddress();
		List<RecipientHolder> archiveRecipients = new ArrayList<RecipientHolder>();

		if(archive && StringUtils.isNotBlank(archiveAddress)) {
			populateEmailList(new String[] {archiveAddress} , archiveRecipients, RecipientType.TO);
		}
		
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
			String messageText = processDefaultReplacements(receipientText, r);
			
			
			if(StringUtils.isNotBlank(receipientHtml)) {
			
				/**
				 * Send a HTML email and generate tracking if required. Make sure
				 * only the recipients get a tracking email. We don't want to 
				 * track the archive emails.
				 */
				String trackingImage = "";
				String nonTrackingUri = "";
				
				String messageHtml = processDefaultReplacements(receipientHtml, r);
				messageHtml = messageHtml.replace("${htmlTitle}", messageSubject);
				
				String archiveRecipientHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);

				if(track && StringUtils.isNotBlank(trackingImage)) {
					String trackingUri = "";
					messageHtml = messageHtml.replace("__trackingImage__", trackingUri);
				} else {
					messageHtml = messageHtml.replace("__trackingImage__", nonTrackingUri);
				}

				send(mail, 
						messageSubject, 
						messageText,
						messageHtml, 
						replyToName, 
						replyToEmail, 
						r, 
						delay,
						context, attachments);
				
				for(RecipientHolder recipient : archiveRecipients) {
					send(mail, messageSubject, messageText, archiveRecipientHtml, 
							replyToName, replyToEmail, recipient, delay, context, attachments);
				}
				
			} else {
			
				/**
				 * Send plain email without any tracking
				 */
				send(mail, 
						messageSubject, 
						messageText, 
						"", 
						replyToName, 
						replyToEmail, 
						r, 
						delay,
						context, attachments);
				
				for(RecipientHolder recipient : archiveRecipients) {
					send(mail, messageSubject, messageText, "", 
							replyToName, replyToEmail, recipient, delay, context, attachments);
				}
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
		return !"false".equals(System.getProperty("jadaptive.mail"));
	}
	
	private void send(Mailer mail,
			String subject, 
			String plainText, 
			String htmlText, 
			String replyToName, 
			String replyToEmail, 
			RecipientHolder r, 
			int delay,
			String context, EmailAttachment... attachments) throws AccessDeniedException {
		
		@SuppressWarnings("unused")
		User p = null;
		SMTPConfiguration config = getConfig();
		
		if(r.hasPrincipal()) {
			p = r.getPrincipal();
		} else {
			try {
				p = userService.getUserByEmail(r.getEmail());
			} catch (EntityNotFoundException e) {
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
		
//		List<String> blocked = Arrays.asList(configurationService.getValues(realm, "email.blocked"));
//		for(String emailAddress : blocked) {
//			if(r.getEmail().equalsIgnoreCase(emailAddress)) {
//				if(log.isInfoEnabled()) {
//					log.info("Email blocked. The destination address {} is blocked", emailAddress);
//				}
//				return;
//			}
//		}
		
		Email email = new Email();
		email.setFromAddress(config.getFromName(), fromAddress);
		
		if(StringUtils.isNotBlank(replyToName) && StringUtils.isNotBlank(replyToEmail)) {
			email.setReplyToAddress(replyToName, replyToEmail);
		} else if(StringUtils.isNotBlank(config.getReplyToName())
				&& StringUtils.isNotBlank(config.getReplyToAddress())) {
			email.setReplyToAddress(config.getReplyToName(), config.getReplyToAddress());
		}
		
		email.addRecipient(r.getName(), r.getEmail(), RecipientType.TO);
		
		email.setSubject(subject);

		if(StringUtils.isNotBlank(htmlText)) {
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
						email.addEmbeddedImage(OUTGOING_INLINE_ATTACHMENT_UUID_PREFIX + "-" + cid.toString(), bytes, mime);
					}
				}
			}
			catch(Exception e) {
				log.error(String.format("Failed to parse embedded images in email %s to %s.", subject, r.getEmail()), e);
			}
			email.setTextHTML(doc.toString());
		}
		
		email.setText(plainText);
		
		if(attachments!=null) {
			for(EmailAttachment attachment : attachments) {
				email.addAttachment(attachment.getName(), attachment);
			}
		}
		
		try {
			
			if(delay > 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				};
			}
			
			if("true".equals(System.getProperty("hypersocket.email", "true")))
				mail.sendMail(email);
			
//			eventService.publishEvent(new EmailEvent(this, realm, subject, plainText, r.getEmail(), context));
		} catch (MailException e) {
//			eventService.publishEvent(new EmailEvent(this, e, realm, subject, plainText, r.getEmail(), context));
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
		User principal = userService.getUser(val);
		
		if(principal!=null) {
			return StringUtils.isNotBlank(principal.getEmail());
		}
		return false;
	}
	
	private void populateEmail(String val, List<RecipientHolder> recipients) throws ValidationException {

		Pattern p = Pattern.compile(EmailNotificationServiceImpl.EMAIL_NAME_PATTERN);

		Matcher m = p.matcher(val);
		User principal = null;
		
		if (m.find()) {
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (!Pattern.matches(EmailNotificationServiceImpl.EMAIL_PATTERN, email)) {
				throw new ValidationException(email
						+ " is not a valid email address");
			}
			
			name = WordUtils.capitalize(name.replace('.',  ' ').replace('_', ' '));
			
			try {
				principal = userService.getUserByEmail(email);
			} catch(EntityNotFoundException e) { }
		} else {

			// Not an email address? Is this a principal of the realm?
			try {
				principal = userService.getUser(val);
			} catch(EntityNotFoundException e) { }
			
		}
		
		if(principal==null) {
			try {
				principal = userService.getUserByEmail(val);
			} catch (EntityNotFoundException e) {
			}
		}
		
//		if(principal==null) {
//			try {
//				principal = realmService.getPrincipalById(getCurrentRealm(), Long.parseLong(val), PrincipalType.USER);
//			} catch(AccessDeniedException | NumberFormatException e) { };
//		}
		
		if(principal!=null) {
			if(StringUtils.isNotBlank(principal.getEmail())) {
				recipients.add(new RecipientHolder(principal, principal.getEmail()));
				return;
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
