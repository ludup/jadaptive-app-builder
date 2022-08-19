package com.jadaptive.app;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationSameSiteConfiguration {
  @Bean
  public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
  return CookieSameSiteSupplier.ofLax();
  }
}