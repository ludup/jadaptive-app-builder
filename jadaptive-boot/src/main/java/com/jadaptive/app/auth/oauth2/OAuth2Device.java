package com.jadaptive.app.auth.oauth2;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.Strictness;
import com.jadaptive.api.db.SingletonObjectDatabase;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Extension
@ModalPage
@RequestPage(path = "oauth2-device")
@PageDependencies(extensions = { "jquery", "bootstrap", "fontawesome", "jadaptive-utils"} )
@PageProcessors(extensions = { "i18n"} )
@Component
public class OAuth2Device extends AuthenticatedPage implements FormProcessor<OAuth2Device.DeviceForm> {

	public interface DeviceForm {
		String getUserCode();
		boolean isApproved();
	}
	
	static Logger LOG = LoggerFactory.getLogger(OAuth2Device.class);
	
	@Autowired
	private OAuth2Service oAuth2Service; 
	
	@Autowired
	private PageCache pageCache;
	
	@Autowired
	private SingletonObjectDatabase<OAuth2Configuration> config;
	
	@Override
	public Class<DeviceForm> getFormClass() {
		return DeviceForm.class;
	}

	@Override
	public String getUri() {
		return "oauth2-device";
	}

	public boolean processForm(Document document, DeviceForm form) throws Exception {
		var userCode = form.getUserCode().toUpperCase();
		var pendingDevice = oAuth2Service.getPendingDevice(userCode);
		if(form.isApproved()) {
			if(pendingDevice == null) {
		    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
		    	Feedback.error("oauth2", "error.invalidUserCode");
		    	return false;
			}

			approve(userCode, pendingDevice);
			return false;
		}
		else {
			if(pendingDevice == null) {
				LOG.warn("User rejected device without any user code, client will still be waiting unless device approval URL is visited again.");
			}
			else {
				LOG.info("Reject OAuth device {} [{}]", pendingDevice.deviceCode(), pendingDevice.userCode());
				oAuth2Service.rejectUserCode(pendingDevice.userCode());
		    	Feedback.info("oauth2", "info.rejectedDevice");
			}
			throw new PageRedirect(pageCache.getHomePage());
		}
	}

	@Override
	protected void beforeProcess(String uri, HttpServletRequest request, HttpServletResponse response)
			throws FileNotFoundException {
		super.beforeProcess(uri, request, response);

		var uc = request.getParameter("user_code"); 
		if(StringUtils.isNotBlank(uc)) {
			var pendingDevice = oAuth2Service.getPendingDevice(uc);
			if(pendingDevice == null)
				throw new IllegalStateException("No such pending device.");
			var strictness = Strictness.forScopes(oAuth2Service.getScopes(pendingDevice.request().requestedScopes()));
			if(strictness != Strictness.STRICT) {
				approve(uc, pendingDevice); 
			}
		}
	}

	@Override
	protected void generateAuthenticatedContent(Document document) throws FileNotFoundException, IOException {
		var req = Request.get();
		var uc = req.getParameter("user_code"); 
		var ucEl = document.getElementById("userCode");
		if(uc != null) {
			ucEl.attr("value", uc);
		}
		
		var oauthConfig = config.getObject(OAuth2Configuration.class);
		ucEl.attr("style", "width: " + (oauthConfig.getDeviceCodePattern().length() + (int)((float)oauthConfig.getDeviceCodePattern().length() / 2.0f)) + "ch;");
		
		var oauth2Config = config.getObject(OAuth2Configuration.class);
		var len = oauth2Config.getDeviceCodePattern().length();
		if(len == 0)
			len = 6;
		ucEl.
			attr("size", String.valueOf(len)).
			attr("maxlength", String.valueOf(len)).
			attr("minlength", String.valueOf(len));
	}

	private void approve(String userCode, PendingDevice pendingDevice) throws FileNotFoundException {
		LOG.info("Approving OAuth device {}", pendingDevice.deviceCode());
		
		/* Update the oauth request in the session so that it has a redirect
		 * URI that points back to the home page when the approval is finished.
		 * 
		 * There will not be any existing redirect URI for device token type.
		 * 
		 * We also attach the user code so the pending device can be 
		 * removed after approval (or rejection)
		 */
		var newOauthRequest = new OAuth2Request.Builder().
				forOAuthRequest(pendingDevice.request()).
				withRedirectUri(PageCache.getPageURL(pageCache.getHomePage())).
				withUserCode(userCode).
				build();
		
		OAuth2Request.set(Request.get().getSession(), newOauthRequest);
		throw new PageRedirect(pageCache.getPage(OAuth2Approve.class));
	}

}
