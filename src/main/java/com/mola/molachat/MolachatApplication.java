package com.mola.molachat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class MolachatApplication {

	public static void main(String[] args) {
		SpringApplication.run(MolachatApplication.class, args);
	}

//	@Bean
//	public ServletWebServerFactory tomcatEmbedded() {
//		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
//		tomcat.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
//			// connector other settings...
//			// configure maxSwallowSize
//			if ((connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>)) {
//				// -1 means unlimited, accept bytes
//				((AbstractHttp11Protocol<?>)
//						connector.getProtocolHandler()).setMaxSwallowSize(-1);
//			}
//		});
//		return tomcat;
//	}
}
