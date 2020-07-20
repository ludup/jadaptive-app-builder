package com.jadaptive.app.ui;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.codesmith.webbits.ClasspathResource;
import com.codesmith.webbits.Form;
import com.codesmith.webbits.HTTPMethod;
import com.codesmith.webbits.In;
import com.codesmith.webbits.Out;
import com.codesmith.webbits.Page;
import com.codesmith.webbits.Redirect;
import com.codesmith.webbits.View;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.ui.AbstractPage;
import com.jadaptive.api.user.UserService;

@Page
@View(contentType = "text/html", paths = { "/winlogin"})
@ClasspathResource
public class WinLogin extends AbstractPage {
	
	static Logger log = LoggerFactory.getLogger(WinLogin.class);
	
	@Autowired
	private UserService userService; 
	
	public WinLogin() {
		
	}
    @Out(methods = HTTPMethod.POST)
    Document service(@In Document content, @Form LoginForm form) {
	
    	try {
    		
			if(!Boolean.getBoolean("jadaptive.webUI")) {
				throw new AccessDeniedException("Web UI is currently disabled. Login to manage your account via the SSH CLI");
			}
			
	    	if(userService.verifyPassword(userService.getUser(form.getUsername()),
	    			form.getPassword().toCharArray())) {
	    		
	    		if(StringUtils.isNotBlank(form.getKey()) && StringUtils.isNotBlank(form.getIv())) {
		    		Request.get().getSession().setAttribute(WinSuccess.ENCRYPTED_USERNAME, 
		    				encryptString(form.getUsername(), form.getKey(), form.getIv()));
		    		Request.get().getSession().setAttribute(WinSuccess.ENCRYPTED_PASSWORD, 
		    				encryptString(form.getPassword(), form.getKey(), form.getIv()));
	    		}
	    		
	    		
	    		throw new Redirect(WinSuccess.class);
	    	}
	    	
	    	Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Bad username or password</div>");
    		return content;

    	} catch(AccessDeniedException e) {
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">" + e.getMessage() + "</div>");
    		return content;
    	} catch(Redirect e) {
    		throw e;
    	} catch(Throwable e) {
    		log.error("Windows Login failed with error", e);
    		Request.response().setStatus(HttpStatus.FORBIDDEN.value());
    		content.selectFirst("#feedback").append("<div class=\"alert alert-danger\">Internal error</div>");
    		return content;
    	}
    }

    private String encryptString(String value, String key, String iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		
    	byte[] rawkey = Base64.getDecoder().decode(key);
    	byte[] rawiv = Base64.getDecoder().decode(iv);
    	
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	
    	SecretKeySpec kspec = new SecretKeySpec(rawkey, "AES");
    	cipher.init(Cipher.ENCRYPT_MODE, kspec, new IvParameterSpec(rawiv));
    	
		return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes("UTF-8")));
	}

	public interface LoginForm {
		String getUsername();
		String getPassword();
		String getKey();
		String getIv();
    }
}
