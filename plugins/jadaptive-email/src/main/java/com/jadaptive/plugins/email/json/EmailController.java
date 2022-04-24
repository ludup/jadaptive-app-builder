package com.jadaptive.plugins.email.json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.json.ResourceStatus;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.plugins.email.EmailVerificationService;
import com.jadaptive.plugins.email.HTMLTemplate;

@Controller
@Extension
public class EmailController extends AuthenticatedController {

	static Logger log = LoggerFactory.getLogger(EmailController.class);
	
	@Autowired
	private EmailVerificationService verificationService; 
	
	@Autowired
	private TenantAwareObjectDatabase<HTMLTemplate> htmlService; 
	
	static String LORUM_TEXT = "<h1>Lorum ipsum H1</h1>\n"
			+ "<h2>Lorum ipsum H2</h2>\n"
			+ "<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse vehicula ex a lectus scelerisque viverra. Vestibulum placerat lacinia mattis. Etiam magna mi, posuere ut nunc a, egestas tincidunt ligula. Mauris pulvinar eleifend mollis. Proin ultricies arcu risus, in pharetra nisi placerat sit amet. Praesent porttitor lacinia enim sit amet ultricies. Cras non aliquet nunc. Donec et elit ipsum. Curabitur non turpis orci. Maecenas vel congue quam. Suspendisse facilisis elit non dolor dignissim vestibulum quis ac nunc. Cras vel eleifend nibh. Pellentesque congue nec diam ac sollicitudin.</p>\n"
			+ "<h3>Lorum ipsum H3</h3>\n"
			+ "<p>Nullam purus mauris, tristique eget nulla et, ornare vulputate arcu. Aliquam sit amet viverra enim, condimentum placerat ligula. Aenean non enim pretium, sollicitudin diam in, pretium nunc. Praesent nunc nisi, aliquet non risus at, pharetra tristique diam. Nullam at nunc eu dui vehicula molestie. Proin venenatis fermentum mauris at blandit. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nam pulvinar auctor lacinia. Praesent bibendum, tellus vitae facilisis bibendum, nisi arcu semper magna, a venenatis tellus enim fringilla risus.</p>\n"
			+ "<h4>Lorum ipsum H4</h4>\n"
			+ "<p>Pellentesque et imperdiet ex. Mauris maximus tincidunt maximus. Maecenas at lacinia eros. Sed gravida aliquet turpis, vitae volutpat elit vestibulum ac. Proin tortor lectus, tincidunt et sapien eu, aliquam finibus ex. Mauris lorem ex, congue id odio at, laoreet aliquam mi. Suspendisse sed est feugiat felis volutpat feugiat. Fusce ipsum leo, vestibulum pulvinar nunc sed, tristique eleifend leo.</p>";
			
	@RequestMapping(value = "/app/api/registration/verifyEmail", method = { RequestMethod.POST, RequestMethod.GET }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> verifyEmail(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String email) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			return new ResourceStatus<Boolean>(true, verificationService.verifyEmail(email), "");
		} catch(Throwable t) {
			log.error("api/registration/verifyEmail", t);
			return new ResourceStatus<Boolean>(false, t.getMessage());
		} finally {
			clearUserContext();
		}
	}
	
	@RequestMapping(value = "/app/api/registration/assertCode", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Boolean> verifyCode(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam String email,
			@RequestParam String code) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			return new ResourceStatus<Boolean>(Boolean.valueOf(verificationService.assertCode(email, code)));
		} catch(Throwable t) {
			log.error("api/registration/assertCode", t);
			return new ResourceStatus<Boolean>(false, t.getMessage());
		} finally {
			clearUserContext();
		}

	}
	
	@RequestMapping(value = "/app/api/html/preview/{uuid}", method = { RequestMethod.GET }, produces = { "text/html" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public String previewTemplate (
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable String uuid) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupSystemContext();
		
		try {
			HTMLTemplate template = htmlService.get(uuid, HTMLTemplate.class);
			Document doc = Jsoup.parse(template.getHtml());
			Elements elements = doc.select(template.getContentSelector());
			elements.first().append(LORUM_TEXT);
			return doc.toString();
		} catch(Throwable t) {
			log.error("api/html/preview", t);
			throw t;
		} finally {
			clearUserContext();
		}
	}
}
