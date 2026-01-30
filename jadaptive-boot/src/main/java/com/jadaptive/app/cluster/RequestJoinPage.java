package com.jadaptive.app.cluster;


import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService;
import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService.OAuth2Authorization;
import com.jadaptive.api.auth.oauth2.OAuth2CompleteController;
import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.cluster.ClusterNode;
import com.jadaptive.api.cluster.UnjoinedFilter;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.Feedback;
import com.jadaptive.api.ui.FormProcessor;
import com.jadaptive.api.ui.ModalPage;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageDependencies;
import com.jadaptive.api.ui.PageProcessors;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.RequestPage;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.menu.PageMenu;

@Extension
@ModalPage
@RequestPage(path = "request-join")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils" })
@PageProcessors(extensions = { "i18n" })
@PageMenu(feature = ClusterNode.RESOURCE_KEY, parent = ApplicationMenuService.CONFIGURATION_MENU_UUID, icon = "fa-object-union", 
	weight = 1000, path = "/app/ui/request-join", bundle = ClusterNode.RESOURCE_KEY, i18n = "joinCluster.name", withPermission = "system.read",
	filter = UnjoinedFilter.class)
public class RequestJoinPage extends AuthenticatedPage
		implements FormProcessor<RequestJoinPage.JoinForm> {
	final static Logger LOG = LoggerFactory.getLogger(RequestJoinPage.class);

	public interface JoinForm {
		String getAddress();
		
		boolean isInsecureSsl();
	}
	
	@Autowired
	private OAuth2AuthorizationService oAuth2AuthorizationService;

	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private ClusterManager clusterManager;

	@Override
	public String getUri() {
		return "request-join";
	}

	@Override
	public Class<JoinForm> getFormClass() {
		return JoinForm.class;
	}

	@SuppressWarnings("unused")
	public boolean processForm(Document document, JoinForm form) throws Exception {
		
		var redirUri = "https://" + Request.getThisHost(Request.get()) + OAuth2CompleteController.PATH_PREFIX;

		String baseUri;
		if(!form.getAddress().startsWith("https://") && !form.getAddress().startsWith("http://")) {
			baseUri = "https://" + form.getAddress();
		}
		else {
			baseUri = form.getAddress();
			
		}
		
		var insecureSsl = form.isInsecureSsl();
		if(baseUri == null) {
			throw new IllegalStateException("No address.");
		}
		
		var outhReq = new OAuth2Request.Builder().
				withPKE().
				withState().
				withResponseType("code").
				withBaseUri(baseUri).
				withRedirectUri(redirUri).
				build();
		
		oAuth2AuthorizationService.expectAuthorize(new OAuth2Authorization(PageCache.getPageURL(pageCache.getHomePage()), outhReq, (token, req, resp, authorization) -> {
			clusterManager.join(token.token(), token.refreshToken(), baseUri, insecureSsl);
		}, insecureSsl));
		
		throw new UriRedirect(outhReq.uri("joinCluster"));
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		if(clusterManager.isJoined()) {
			Feedback.error(ClusterNode.RESOURCE_KEY, "error.alreadyJoined");
			throw new PageRedirect(pageCache.getHomePage());
		}
		document.getElementById("address").html(StringUtils.defaultIfBlank(Request.get().getParameter("address"), ""));
	}

}
