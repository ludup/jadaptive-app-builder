package com.jadaptive.app.ui;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Resource;
import com.codesmith.webbits.View;
import com.codesmith.webbits.extensions.Bind;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPage;

@Page(value = Bind.class)
@View(contentType = "text/html", paths = { "/winlogin/success"})
@Resource
public class WinSuccess extends AbstractPage {

	
    public static final String AES_KEY = "aes.key";
    public static final String AES_IV = "aes.iv";
	
    public static final String ENCRYPTED_USERNAME = "encrypted.username";
    public static final String ENCRYPTED_PASSWORD = "encrypted.password";
    
    
	@Out(methods = HTTPMethod.GET)
    Document service(@In Document content) {
			
		Element form = content.getElementsByTag("form").first();
		
		String username = (String) Request.get().getSession().getAttribute(ENCRYPTED_USERNAME);
		String password = (String) Request.get().getSession().getAttribute(ENCRYPTED_PASSWORD);
		
		if(StringUtils.isNoneBlank(username, password)) {
			form.append("<input type=\"hidden\" id=\"username\" value=\"" +  username + "\">");
			form.append("<input type=\"hidden\" id=\"password\" value=\"" + password + "\">");
		}
		Request.get().getSession().getAttribute(ENCRYPTED_USERNAME);
		Request.get().getSession().getAttribute(ENCRYPTED_PASSWORD);
    	return content;
    }
}
