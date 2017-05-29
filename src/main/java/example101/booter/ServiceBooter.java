/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.booter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author SAMBATH
 *
 */
@SpringBootApplication(scanBasePackages = { "example101.booter", "example101.config", "example101.service" })
@EnableSwagger2
public class ServiceBooter extends SpringBootServletInitializer {
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ServiceBooter.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ServiceBooter.class, args);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
	}

	@LoadBalanced
	@Bean
	public RestTemplate getRestTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}