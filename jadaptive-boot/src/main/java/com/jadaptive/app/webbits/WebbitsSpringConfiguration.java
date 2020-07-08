package com.jadaptive.app.webbits;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.codesmith.webbits.WebbitsServlet;
import com.codesmith.webbits.io.WebbitsIoServlet;

@Configuration
public class WebbitsSpringConfiguration {
    @Bean
    public ServerEndpointExporter endpointExporter() {
	ServerEndpointExporter sex = new ServerEndpointExporter();
	sex.setAnnotatedEndpointClasses(WebbitsServerEndpointJ.class, WebbitsIoEndpointJ.class);
	return sex;
    }
    
	@Bean
	public ServletRegistrationBean<?> webbitsServletBean() {
	    ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(
	      new WebbitsServlet(), "/app/ui/*");
	    bean.setLoadOnStartup(1);
	    bean.setAsyncSupported(true);
	    return bean;
	}
	
	@Bean
	public ServletRegistrationBean<?> webbitsIoServletBean() {
	    ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(
	      new WebbitsIoServlet(), "/app/ui/_webbits_io/*");
	    bean.setLoadOnStartup(1);
	    bean.setAsyncSupported(true);
	    return bean;
	}
}
