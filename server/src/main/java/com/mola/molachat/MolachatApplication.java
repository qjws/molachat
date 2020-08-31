package com.mola.molachat;

import com.mola.molachat.config.MyTomcatServletWebServerFactory;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MolachatApplication {
	public static void main(String[] args) {
		SpringApplication.run(MolachatApplication.class, args);
	}

	/**
	 * http重定向到https
	 * @return
	 */
	@Bean
	public Connector connector(){
		Connector connector=new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(8080);
		connector.setSecure(false);
		connector.setRedirectPort(8550);
		return connector;
	}

	@Bean
	public TomcatServletWebServerFactory tomcatServletWebServerFactory(Connector connector){
		TomcatServletWebServerFactory tomcat=new MyTomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(connector);
		return tomcat;
	}
}
