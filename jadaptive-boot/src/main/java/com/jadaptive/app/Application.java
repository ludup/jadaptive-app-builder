package com.jadaptive.app;

import java.io.PrintStream;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import com.jadaptive.api.app.ApplicationVersion;

@ComponentScan({"com.jadaptive.app.**"})
@ServletComponentScan
@SpringBootApplication
public class Application {

	static Logger log = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		 
		 PropertyConfigurator.configure("conf/app-logging.properties");
		 
		 SpringApplication app = new SpringApplication(Application.class);
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
		 app.run(args);
		 
		
	}
}