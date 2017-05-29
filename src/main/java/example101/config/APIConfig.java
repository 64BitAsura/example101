package example101.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import example101.service.SmartCheckoutService;

@Component
public class APIConfig extends ResourceConfig {

	public APIConfig() {
		this.registerCheckoutEndPoint();
	}

	private void registerCheckoutEndPoint() {
		this.register(SmartCheckoutService.class);
	}
}
