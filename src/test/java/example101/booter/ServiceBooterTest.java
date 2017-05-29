/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.booter;

import java.util.Date;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author SAMBATH
 *
 */
@SpringBootApplication(scanBasePackages = { "example101.booter", "example101.config", "example101.service" })
public class ServiceBooterTest extends SpringBootServletInitializer {

	private MockRestServiceServer mockRestServiceServer;
	private RestTemplate restTemplate;
	{
		restTemplate = new RestTemplateBuilder().build();
		mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
		mock();
		// mock server can set response for one request at time, but scheduler in service will come more than one call
		new java.util.Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mock();
			}
		}, 200, 2000);
	}

	/**
	 * 
	 */
	private void mock() {
		try {
			mockRestServiceServer.reset();
			mockRestServiceServer.expect(MockRestRequestMatchers.requestTo("http://localhost:8000/api/discount/bycount")).andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
					.andRespond(MockRestResponseCreators.withSuccess(configDiscountApi().toString(), MediaType.APPLICATION_JSON));
		} catch (JSONException e) {
		}
	}

	private JSONObject configDiscountApi() throws JSONException {
		JSONArray discountByCountList = new JSONArray();
		int dindx = 0;
		for (int indx = 0; indx < 20; indx++) {
			final String itemId = indx + "";
			for (int d = 1; d < 4; d++) {
				int count = d;
				float price = 1 * 2 / (indx + 1) * (indx + 1);
				float discount = d == 1 ? 1 : (float) (Math.log10(d) * 2 / (d * 2.1));
				JSONObject discountByCount = new JSONObject();
				discountByCount.put("itemId", itemId);
				discountByCount.put("count", count);
				discountByCount.put("price", price);
				discountByCount.put("discount", discount);
				discountByCount.put("expiryTime", new Date(new Date().getTime() + 600000).getTime());
				discountByCountList.put(dindx++, discountByCount);
			}
		}
		JSONObject ds = new JSONObject();
		ds.put("discountByCountList", discountByCountList);
		return ds;
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ServiceBooterTest.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(ServiceBooterTest.class, args);
	}

	@Bean
	public RestTemplate getRestTemplate(RestTemplateBuilder builder) {
		return restTemplate;
	}

	@Bean
	public MockRestServiceServer getRestServer() {
		return mockRestServiceServer;
	}

}