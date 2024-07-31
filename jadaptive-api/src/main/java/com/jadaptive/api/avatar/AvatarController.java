package com.jadaptive.api.avatar;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.permissions.AuthenticatedController;
import com.jadaptive.api.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AvatarController extends AuthenticatedController {

	@Autowired
	private AvatarService avatarService;

	@Autowired
	private UserService userService;
	
	@RequestMapping(value= { "/app/api/userLogo/fetch/{uuid}", "/app/api/userLogo/fetch/{uuid}/{size}" }, method = { RequestMethod.GET })
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext(system = true)
	public void legacyGravatar(WebRequest webRequest, HttpServletRequest request, HttpServletResponse response, 
			@PathVariable String uuid, @PathVariable Optional<Integer> size) throws Exception {
		
		var usr = userService.getObjectByUUID(uuid);
		var bldr = new AvatarRequest.Builder();
		bldr.withUsername(usr.getUsername());
		if(StringUtils.isNotBlank(usr.getEmail()))
			bldr.withEmail(usr.getEmail());
		if(StringUtils.isNotBlank(usr.getName()))
			bldr.withName(usr.getName());
		
		var av = avatarService.avatar(bldr.build());
		
		/* TODO: Bit of a hack. We should be able to 'render' to things other than an Element */
		var el = av.render();
		var img = el.selectFirst("img");
		if(img != null) {
			var href = img.attr("src");
			if(href != null && href.startsWith("/")) {
				request.getRequestDispatcher(href).forward(request, response);
				return;
			}
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return;
	}
}
