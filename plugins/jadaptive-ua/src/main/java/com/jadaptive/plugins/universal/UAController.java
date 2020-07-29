package com.jadaptive.plugins.universal;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.servlet.PluginController;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.sshtools.universal.UniversalAuthenticatorClient;

@Extension
@Controller
public class UAController extends AuthenticatedController implements PluginController {

	static Logger log = LoggerFactory.getLogger(UAController.class);
	
	@Autowired
	private UAService uaService;
	
	@RequestMapping(value = "/ua-register", method = { RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void getResources(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, IOException {

		try {
			String principalName = request.getParameter("principalName");
			String email = request.getParameter("email");
			String hostname = request.getParameter("hostname");
			int port = Integer.parseInt(request.getParameter("port"));
			boolean strictSSL = Boolean.parseBoolean(request.getParameter("strictSSL"));
			
			UniversalAuthenticatorClient uac = new UniversalAuthenticatorClient();
			uac.register(email, request.getServerName(), hostname, port, strictSSL, true);
			
			Properties properties = uac.getProperties();
			
			uaService.saveRegistration(principalName, properties);
			
			response.setStatus(200);
		} catch (NumberFormatException | IOException e) {
			log.error("UA registration failed", e);
			response.sendError(500);
		}
	}
}
