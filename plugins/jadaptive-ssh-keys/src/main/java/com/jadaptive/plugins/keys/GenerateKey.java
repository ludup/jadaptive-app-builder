package com.jadaptive.plugins.keys;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.codesmith.webbits.Bound;
import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.codesmith.webbits.bindable.FormBindable.AutoCompleteMode;
import com.codesmith.webbits.bindable.FormBindable.FormField;
import com.codesmith.webbits.bindable.FormBindable.InputRestriction;
import com.codesmith.webbits.widgets.Feedback;
import com.jadaptive.api.session.SessionTimeoutException;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.ui.AuthenticatedPage;
import com.sshtools.common.publickey.InvalidPassphraseException;

@Page
@View(contentType = "text/html", paths = { "generateKey", "generateKey/{uuid}" })
@ClasspathResource
public class GenerateKey extends AuthenticatedPage {

	static Logger log = LoggerFactory.getLogger(GenerateKey.class);

	public interface GenerateForm {
		@FormField
		@InputRestriction(required = true)
		String getName();

		@FormField(type ="password", autoComplete = AutoCompleteMode.DEFEAT)
		String getPassphrase();

		@FormField(type = "select")
		@InputRestriction
		String getType();

		Feedback getFeedback();
	}

	String uuid;
	
	@Autowired
	AuthorizedKeyService keyService; 

	@Bound
	private GenerateForm generateForm;

	public String getUuid() {
		return uuid;
	}
	
	@Bound
	public String[] getTypes() {
		return new String[] { "rsa", "dsa", "ecdsa", "ed25519" };
	}

	@Bound
	public void generateForm()
			throws IOException, InvalidPassphraseException, SessionTimeoutException, UnauthorizedException, InterruptedException {

		Thread.sleep(10000);
		generateForm.getFeedback().warning("Generate is not yet implemented.");

	}

	@Out(methods = HTTPMethod.GET)
	public Document serviceAnonymous(@In Document contents) throws IOException {

		if (StringUtils.isBlank(uuid)) {
			uuid = getCurrentUser().getUuid();
		}

		contents.selectFirst("form").prependElement("input").attr("type", "hidden").attr("name", "uuid").attr("value");
		return contents;
	}
}
