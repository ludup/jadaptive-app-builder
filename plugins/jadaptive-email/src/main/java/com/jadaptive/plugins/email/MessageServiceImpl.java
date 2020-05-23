package com.jadaptive.plugins.email;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
			
			Template subjectTemplate = freeMarker.createTemplate("message.subject." + message.getUuid(),
					message.getSubject(), message.getLastModified().getTime());
			StringWriter subjectWriter = new StringWriter();
			subjectTemplate.process(data, subjectWriter);

			Template bodyTemplate = freeMarker.createTemplate("message.body." + message.getUuid(),
					message.getPlainText(), message.getLastModified().getTime());
			StringWriter bodyWriter = new StringWriter();
			bodyTemplate.process(data, bodyWriter);

			String receipientHtml = "";

			if (StringUtils.isNotBlank(message.getHtml())) {
				if (message.getHtmlTemplate() != null) {
					Document doc = Jsoup.parse(message.getHtmlTemplate().getHtml());
					Elements elements = doc.select(message.getHtmlTemplate().getContentSelector());
					if (elements.isEmpty()) {
						throw new IllegalStateException(String.format("Invalid content selector %s",
								message.getHtmlTemplate().getContentSelector()));
					}
					elements.first().append(message.getHtml());
					receipientHtml = doc.toString();
				} else {
					receipientHtml = message.getHtml();
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

				emailService.sendEmail(subjectWriter.toString(), bodyWriter.toString(),
						htmlWriter.toString(), message.getReplyToName(), message.getReplyToEmail(),
						new RecipientHolder[] { recipient }, message.isArchive(), false, 50,
						"", emailAttachments.toArray(new EmailAttachment[0]));

//			}
			

			} catch (MailException e) {
				// Will be logged by mail API
			} catch (Throwable e) {
				log.error("Failed to send email", e);
			}
		}
	}

}
