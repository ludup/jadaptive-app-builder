package com.jadaptive.plugins.keys;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jadaptive.api.ui.AuthenticatedPage;
import com.jadaptive.api.ui.renderers.DropdownInput;
import com.jadaptive.api.user.User;

public abstract class GeneratePublicKey extends AuthenticatedPage {
	
	@Override
	public String getResource() {
		return "GeneratePublicKey.html";
	}

	@Override
	protected void generateAuthenticatedContent(Document document) {
		
		document.selectFirst("#generateForm").attr("action", getAction());
		document.selectFirst("form").appendChild(new Element("input")
				.attr("type", "hidden")
				.attr("name", "uuid")
				.attr("value", getUser().getUuid()));
		
		document.selectFirst("#nameGroup").after(new Element("div")
				.attr("id", "typeGroup")
				.addClass("form-group")
				.appendChild(new Element("label")
						.attr("for", "type")
						.addClass("col-form-label")
						.attr("jad:bundle", "authorizedKeys")
						.attr("jad:i18n", "type.name"))
				.appendChild(new Element("div")
						.attr("id", "typeInput"))
				.appendChild(new Element("small")
						.addClass("form-text text-muted")
						.attr("jad:bundle", "authorizedKeys")
						.attr("jad:i18n", "type.desc")));
		
		DropdownInput type = new DropdownInput("type", AuthorizedKeyService.RESOURCE_BUNDLE);
		document.selectFirst("#typeInput").after(type.renderInput());
		type.renderValues(PublicKeyType.values(), PublicKeyType.ED25519.name(), false);

	}

	protected abstract User getUser();

	protected abstract String getAction();
	
}
