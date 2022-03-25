package com.jadaptive.plugins.email;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.codemonkey.simplejavamail.MailException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.ITokenResolver;

import freemarker.template.Template;

@Service
public class MessageServiceImpl extends AuthenticatedService implements MessageService {

	static Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
	
	@Autowired
	private TenantAwareObjectDatabase<Message> repository; 
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private EmailNotificationService emailService; 
	
	@Autowired
	private FreeMarkerService freeMarker;
	
	@Override
	public Set<String> getMessageVariables(Message message) {

		Set<String> vars = new TreeSet<String>();
		vars.addAll(message.getReplacementVariables());
		vars.addAll(Arrays.asList("trackingImage", "email", "firstName", "fullName", "userId"));
		return vars;
	}
//
//	@Override
//	public void sendMessage(String uuid, ITokenResolver tokenResolver, User... users) {
//		sendMessage(uuid, tokenResolver, Arrays.asList(users));
//	}
//
//	@Override
//	public void sendMessage(String uuid, ITokenResolver tokenResolver, RecipientHolder replyTo,
//			List<EmailAttachment> attachments, Iterator<Principal> principals, String context) {
//		sendMessage(uuid, tokenResolver, replyTo, attachments, principals,
//				Collections.<String>emptyList(), null);
//	}
//
//	@Override
//	public void sendMessageToEmailAddress(String uuid, ITokenResolver tokenResolver,
//			String... emailAddresses) {
//		sendMessageToEmailAddress(uuid, tokenResolver, Arrays.asList(emailAddresses), null, null);
//	}
//
//	@Override
//	public void sendMessageToEmailAddress(String uuid, ITokenResolver tokenResolver,
//			Collection<String> emailAddresses, List<EmailAttachment> attachments, String context) {
//
//		Message message = repository.get(uuid, Message.class);
//
//		sendMessage(message, realm, tokenResolver, null,
//				new TransformingIterator<String, RecipientHolder>(emailAddresses.iterator()) {
//					@Override
//					protected RecipientHolder transform(String email) {
//						return new RecipientHolder(ResourceUtils.getNamePairKey(email),
//								ResourceUtils.getNamePairValue(email));
//					}
//				}, attachments, context);
//	}
//
//	@Override
//	public void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
//			RecipientHolder replyTo, ITokenResolver tokenResolver, List<EmailAttachment> attachments, String context) {
//		Message message = repository.getMessageById(resourceKey, realm);
//
//		if (message == null) {
//			log.error(String.format("Invalid message id %s", resourceKey));
//			return;
//		}
//
//		sendMessage(message, realm, tokenResolver, replyTo, recipients.iterator(), attachments, context);
//
//	}
//
//	@Override
//	public void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
//			ITokenResolver tokenResolver) {
//		sendMessageToEmailAddress(resourceKey, realm, recipients, null, tokenResolver, null, null);
//	}
//
//	@Override
//	public void sendMessageNow(String resourceKey, Realm realm, ITokenResolver tokenResolver,
//			Collection<Principal> principals) {
//		sendMessage(resourceKey, realm, tokenResolver, principals.iterator(), Collections.<String>emptyList(), null);
//	}
//
//	@Override
//	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
//			List<EmailAttachment> attachments, Iterator<Principal> principals, Collection<String> emails, String context) {
//		sendMessage(resourceKey, realm, tokenResolver, replyTo, principals, emails, new Date(), attachments);
//	}
//
//	@Override
//	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
//			Collection<Principal> principals) throws ResourceException {
//		sendMessage(resourceKey, realm, tokenResolver, principals.iterator(), Collections.<String>emptyList(),
//				new Date());
//	}
//
//	@Override
//	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
//			Iterator<Principal> principals) throws ResourceException {
//		sendMessage(resourceKey, realm, tokenResolver, principals, Collections.<String>emptyList(), new Date());
//	}
//
//	@Override
//	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
//			Iterator<Principal> principals, Collection<String> emails, Date schedule) {
//		sendMessage(resourceKey, realm, tokenResolver, null, principals, emails, schedule, null);
//	}
//
//	@Override
//	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
//			Iterator<Principal> principals, Collection<String> emails, Date schedule,
//			List<EmailAttachment> attachments) {
//
//		Message message = repository.getMessageById(resourceKey, realm);
//
//		if (message == null) {
//			log.error(String.format("Invalid message id %s", resourceKey));
//			return;
//		}
//
//		sendMessage(message, realm, tokenResolver, replyTo, principals, emails, schedule, attachments, null);
//	}

	@Override
	public void sendMessage(String uuid, ITokenResolver tokenResolver, 
			String emailAddress,
			EmailAttachment... attachments) {
		
		if(log.isInfoEnabled()) {
			log.info("Sending email template {} to {}", uuid, emailAddress);
		}
		RecipientHolder holder = new RecipientHolder(emailAddress);
		sendMessage(uuid, tokenResolver, 
				new ArrayList<RecipientHolder>(Arrays.asList(holder)).iterator(), 
				attachments);
	}
	
	@Override
	public void sendMessage(String uuid, ITokenResolver tokenResolver, 
//			RecipientHolder replyTo,
			Iterator<RecipientHolder> recipients, 
//			Date schedule,
			EmailAttachment... attachments) {
//		if(schedule == null)
//			schedule = new Date();

		Message message = repository.get(uuid, Message.class);
		
		if (!message.isEnabled()) {
			log.info(String.format("Message template %s has been disabled", message.getName()));
			return;
		}
		/**
		 * LDP - Final guard against a user receiving duplicate messages 
		 */
		Set<String> processedEmails = new HashSet<>();
		
		while(recipients.hasNext()) {
			
		
		try {
			
			Locale locale = Locale.getDefault();
			
			RecipientHolder recipient = recipients.next();

			if(StringUtils.isBlank(recipient.getEmail())) {
				log.warn("Detected empty email in a RecipientHolder! Skipping");
				continue;
			}
			
			if(processedEmails.contains(recipient.getEmail().toLowerCase())) {
				log.info("Skipping {} because we already sent this message to that address", recipient.getEmail());
				continue;
			}
			
			processedEmails.add(recipient.getEmail().toLowerCase());
			
			Map<String, Object> data = tokenResolver.getData();
			data.put("email", recipient.getEmail());
			data.put("firstName", recipient.getFirstName());
			data.put("fullName", recipient.getName());
			data.put("userId", recipient.getPrincipalId());

			if(recipient.hasUserObject()) {
				/* 
				 * #V1HT82 - Issue Title is not showing in outgoing email
				 * 
				 * Don't overwrite variables provided by the token resolver, 
				 * they should have higher priority.
				 */
				final Map<String, String> userProps = userService.getUserProperties(recipient.getUser());
				for(Map.Entry<String, String> en : userProps.entrySet()) {
					if(!data.containsKey(en.getKey()))
						data.put(en.getKey(), en.getValue());
				}
			}
			
			MessageContent defaultContent = null;
			MessageContent localeContent = null;
			for(MessageContent content : message.getContent()) {
				if(StringUtils.isBlank(content.getLocale())) {
					defaultContent = content;
				}
				if(localeContent == null) {
					if(content.getLocale().equals(locale.getLanguage()) || content.getLocale().equals(locale.toString())) {
						localeContent = content;
					}
				}
			}
			
			if(Objects.isNull(localeContent)) {
				if(Objects.nonNull(defaultContent)) {
					localeContent = defaultContent;
				} else if(message.getContent().size() > 0){
					localeContent = message.getContent().iterator().next();
				}
			} 
			
			if(Objects.isNull(localeContent)) {
				log.warn("Message content could not be determined for locale {}", locale.toString());
				return;
			}
			
			Template subjectTemplate = freeMarker.createTemplate("message.subject." + message.getUuid(),
					localeContent.getSubject(), message.getLastModified().getTime());
			StringWriter subjectWriter = new StringWriter();
			subjectTemplate.process(data, subjectWriter);

			Template bodyTemplate = freeMarker.createTemplate("message.body." + message.getUuid(),
					localeContent.getPlainText(), message.getLastModified().getTime());
			StringWriter bodyWriter = new StringWriter();
			bodyTemplate.process(data, bodyWriter);

			String receipientHtml = "";

			if (StringUtils.isNotBlank(localeContent.getHtmlText())) {
				if (localeContent.getHtmlTemplate() != null) {
					Document doc = Jsoup.parse(localeContent.getHtmlTemplate().getHtml());
					Elements elements = doc.select(localeContent.getHtmlTemplate().getContentSelector());
					if (elements.isEmpty()) {
						throw new IllegalStateException(String.format("Invalid content selector %s",
								localeContent.getHtmlTemplate().getContentSelector()));
					}
					elements.first().append(localeContent.getHtmlText());
					receipientHtml = doc.toString();
				} else {
					receipientHtml = localeContent.getHtmlText();
				}
			}

			Template htmlTemplate = freeMarker.createTemplate("message.html." + message.getUuid(),
					receipientHtml, message.getLastModified().getTime());

			data.put("htmlTitle", subjectWriter.toString());

			StringWriter htmlWriter = new StringWriter();
			htmlTemplate.process(data, htmlWriter);

//			String attachmentsListString = message.getAttachments();
//			List<String> attachmentUUIDs = new ArrayList<>(
//					Arrays.asList(ResourceUtils.explodeValues(attachmentsListString)));
//
//			if (tokenResolver instanceof ResolverWithAttachments) {
//				attachmentUUIDs.addAll(((ResolverWithAttachments) tokenResolver).getAttachmentUUIDS());
//			}

//			if (attachments != null) {
//				for (EmailAttachment attachment : attachments) {
//					attachmentUUIDs.add(attachment.getName());
//				}
//			}

//			if (schedule != null) {

//				attachmentsListString = ResourceUtils.implodeValues(attachmentUUIDs);

//				batchService.scheduleEmail(realm, subjectWriter.toString(), bodyWriter.toString(),
//						htmlWriter.toString(), replyTo != null ? replyTo.getName() : message.getReplyToName(),
//						replyTo != null ? replyTo.getEmail() : message.getReplyToEmail(), recipient.getName(),
//						recipient.getEmail(), message.isArchive(), false, attachmentsListString, schedule, context);
//
//			} else {
				List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
				if (attachments != null) {
					emailAttachments.addAll(Arrays.asList(attachments));
				}
//				for (String uuid : attachmentUUIDs) {
//					try {
//						FileUpload upload = uploadService.getFileUpload(uuid);
//						emailAttachments
//								.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid)) {
//									@Override
//									public InputStream getInputStream() throws IOException {
//										return uploadService.getInputStream(getName());
//									}
//								});
//					} catch (ResourceNotFoundException | IOException e) {
//						log.error(String.format("Unable to locate upload %s", uuid), e);
//					}
//				}

				if(log.isInfoEnabled()) {
					log.info("Sending \"{}\" email to {}", subjectWriter.toString(), recipient.getEmail());
				}
				
				emailService.sendEmail(subjectWriter.toString(), bodyWriter.toString(),
						htmlWriter.toString(), message.getReplyToName(), message.getReplyToEmail(),
						new RecipientHolder[] { recipient }, message.isArchive(),
						emailAttachments.toArray(new EmailAttachment[0]));

//			}
			

			} catch (MailException e) {
				// Will be logged by mail API
				log.error("Failed to send email", e);
			} catch (Throwable e) {
				log.error("Failed to send email", e);
			}
		}
	}

	@Override
	public Iterable<Message> allMessages() {
		return repository.list(Message.class);
	}

	@Override
	public Message getMessageByShortName(String name) {
		return repository.get(Message.class, SearchField.eq("shortName", name));
	}

	@Override
	public void saveMessage(Message message) {
		repository.saveOrUpdate(message);
	}

}
