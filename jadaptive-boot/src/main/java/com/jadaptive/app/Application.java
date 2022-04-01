package com.jadaptive.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.x509.FileFormatException;
import com.jadaptive.api.x509.InvalidPassphraseException;
import com.jadaptive.api.x509.MismatchedCertificateException;
import com.jadaptive.api.x509.X509CertificateUtils;

@SpringBootApplication
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
		 
		 
		 File pluginsFolder = new File("plugins");

		 if(pluginsFolder.exists()) {
			 File[] files = pluginsFolder.listFiles();
			 if(files!=null) {
			 for(File file : files) {
				 if(file.isFile() && file.getName().endsWith(".zip")) {
					 continue;
				 }
				 FileUtils.deleteQuietly(file);
			 }
			 }
		 }
		 
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
			 System.exit(exitCode);		 }
	}
	
	private static void checkDefaultCertificate() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, MismatchedCertificateException {
		
		KeyPair key = null;
		X509Certificate[] chain = null;
		X509Certificate cert = null;
		File keystoreFile = new File(ApplicationProperties.getValue("server.ssl.key-store", "conf/cert.p12"));
		File keyFile = new File(ApplicationProperties.getValue("server.ssl.private-key", "conf/key.pem"));
		
		if(keyFile.exists() && (!keystoreFile.exists() || keyFile.lastModified()!=keystoreFile.lastModified())) {
			
			if(log.isInfoEnabled()) {
				log.info("Loading PEM private key {}", keyFile.getName());
			}
			
			try(InputStream in = new FileInputStream(keyFile)) {
				key = X509CertificateUtils.loadKeyPairFromPEM(in, 
						ApplicationProperties.getValue("server.ssl.private-key-password", "").toCharArray());
			} catch (InvalidPassphraseException | FileFormatException e) {
				log.error("Failed to read PEM private key file", e);
			}
			
			File chainFile = new File(ApplicationProperties.getValue("server.ssl.ca-bundle", "conf/chain.pem"));
			if(chainFile.exists()) {
				try(InputStream cin = new FileInputStream(chainFile)) {
					chain = X509CertificateUtils.loadCertificateChainFromPEM(cin);
				} catch (FileFormatException e) {
					log.error("Failed to read PEM certificate chain file", e);
				}
			}
			
			File certFile = new File(ApplicationProperties.getValue("server.ssl.certificate", "conf/cert.pem"));
			if(certFile.exists()) {
				try(InputStream cin = new FileInputStream(certFile)) {
					cert = X509CertificateUtils.loadCertificateFromPEM(cin);
				} catch (FileFormatException e) {
					log.error("Failed to read PEM certificate file", e);
				}
			}
			
			if(Objects.nonNull(key) && Objects.nonNull(cert)) {
				
				List<X509Certificate> tmp = new ArrayList<>();
				tmp.add(cert);
				if(Objects.nonNull(chain)) {
					tmp.addAll(Arrays.asList(chain));
				}
				 
				KeyStore ks = X509CertificateUtils.createPKCS12Keystore(key, tmp.toArray(new X509Certificate[0]), 
						ApplicationProperties.getValue("server.ssl.key-alias", "server"), 
						ApplicationProperties.getValue("server.ssl.key-store-password", "changeit").toCharArray());
				
				
				 try (OutputStream fout = new FileOutputStream(keystoreFile)) {
					ks.store(fout, ApplicationProperties.getValue(
								"server.ssl.key-store-password", 
									"changeit").toCharArray());
				}
				 

			    keystoreFile.setLastModified(keyFile.lastModified());
				
			    if(log.isInfoEnabled()) {
					log.info(String.format("Converted PEM files to keystore"));
				}
					
				return;
			}
		}
		
		if(!keystoreFile.exists()) {
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Generating default certificate and keystore"));
			}
			
			KeyPair kp = X509CertificateUtils.generatePrivateKey("RSA", 2048);
			cert = X509CertificateUtils.generateSelfSignedCertificate("localhost", 
					 "JADAPTIVE Appplication", 
					 "JADAPTIVE Limited", 
					 "Penzance", "Cornwall", "GB", kp, "SHA256WithRSAEncryption");
			KeyStore ks = X509CertificateUtils.createPKCS12Keystore(kp, 
					 new X509Certificate[] { cert },
					 ApplicationProperties.getValue("server.ssl.key-alias", "server"),
					 ApplicationProperties.getValue("server.ssl.key-store-password", "changeit").toCharArray());
			 
			 try (OutputStream fout = new FileOutputStream(keystoreFile)) {
				ks.store(fout, ApplicationProperties.getValue(
						"server.ssl.key-store-password", 
							"changeit").toCharArray());
			}
		}
	}
}