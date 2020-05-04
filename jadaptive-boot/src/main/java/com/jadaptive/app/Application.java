package com.jadaptive.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

import com.codesmith.webbits.bootstrap.Bootstrap;
import com.codesmith.webbits.fontawesome.FontAwesome;
import com.codesmith.webbits.jquery.JQuery;
import com.codesmith.webbits.spring.WebbitsComponentScan;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.x509.MismatchedCertificateException;
import com.jadaptive.api.x509.X509CertificateUtils;
import com.jadaptive.app.ui.JadaptiveApp;

@SpringBootApplication
@ImportResource({ "classpath*:webbits.xml" })
@WebbitsComponentScan(basePackageClasses = {
	JadaptiveApp.class,
	Bootstrap.class,
	JQuery.class,
	FontAwesome.class })
public class Application {

	static Logger log = LoggerFactory.getLogger(Application.class);
	
	static int exitCode = 0;
	static SpringApplication app;
	static boolean running = true;
	
	public static void shutdown() {
		running = false;
		synchronized (app) {
			app.notify();
		}
	}

	public static void restart() {
		exitCode = 99;
		shutdown();
	}
	
	@Bean
    public ExitCodeGenerator exitCodeGenerator() {
        return () -> exitCode;
    }
	
	public static void main(String[] args) {
		 
		 PropertyConfigurator.configure("conf/app-logging.properties");
		 

		 try {
			checkDefaultCertificate();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| MismatchedCertificateException e) {
			log.error("Failed to setup default SSL certificate", e);
			return;
		}
		 
		 app = new SpringApplication(Application.class);
		 app.setBanner(new Banner() {

			@Override
			public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
				 out.println();
				 out.println("   _           _             _   _           ");
				 out.println("  (_) __ _  __| | __ _ _ __ | |_(_)_   _____ ");
				 out.println("  | |/ _` |/ _` |/ _` | '_ \\| __| \\ \\ / / _ \\");
				 out.println("  | | (_| | (_| | (_| | |_) | |_| |\\ V /  __/");
				 out.println(" _/ |\\__,_|\\__,_|\\__,_| .__/ \\__|_| \\_/ \\___|");
				 out.println("|__/                  |_|                    ");
				 out.println("==============================================");
				 out.println(String.format(":: ApplicationVersion %s ::", ApplicationVersion.getVersion()));
			}
			 
		 });
		 app.setBannerMode(Banner.Mode.LOG);
		 
		 synchronized(app) {
			 ApplicationContext context = app.run(args);
			 while(running) {
				 try {
					app.wait(5000);
				} catch (InterruptedException e) {
				}
			 }
			 
			 log.info("Application shutting down with exitCode={}", exitCode);
			 exitCode = SpringApplication.exit(context, () -> exitCode);
			 log.info("System exit being called with exitCode={}", exitCode);
			 System.exit(exitCode);
		 }
	}
	
	private static void checkDefaultCertificate() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, MismatchedCertificateException {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Generating keystore"));
		}
		
		Properties properties = ApplicationProperties.loadPropertiesFile(new File("application.properties"));
		
		File certFile = new File(properties.getProperty("server.ssl.key-store", "conf/cert.p12"));
		
		if(!certFile.exists()) {
			KeyPair kp = X509CertificateUtils.generatePrivateKey("RSA", 2048);
			 X509Certificate cert = X509CertificateUtils.generateSelfSignedCertificate("localhost", 
					 "JADAPTIVE Appplication", 
					 "JADAPTIVE Limited", 
					 "Penzance", "Cornwall", "GB", kp, "SHA256WithRSAEncryption");
			 KeyStore ks = X509CertificateUtils.createPKCS12Keystore(kp, 
					 new X509Certificate[] { cert },
					 properties.getProperty("server.ssl.key-alias", "server"),
					 properties.getProperty("server.ssl.key-store-password", "changeit").toCharArray());
			 
			 try (OutputStream fout = new FileOutputStream(certFile)) {
				ks.store(fout, properties.getProperty(
						"server.ssl.key-store-password", 
							"changeit").toCharArray());
			}
		}
	}
}