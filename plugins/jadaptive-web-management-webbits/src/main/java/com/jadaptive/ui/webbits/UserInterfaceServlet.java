package com.jadaptive.ui.webbits;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.Extension;

import com.codesmith.webbits.WebbitsServlet;
import com.jadaptive.api.servlet.PluginServlet;

@Extension
@WebServlet(name="uiServlet", description="Servlet for the User Interface", urlPatterns = { "/ui/*" })
public class UserInterfaceServlet extends PluginServlet {

	private static final long serialVersionUID = -6665709724567362268L;
	
	WebbitsServlet webbits = new WebbitsServlet();
	
	@Override
	protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws ServletException, IOException {
		
		webbits.service(httpRequest, httpResponse);
	}

	

}
