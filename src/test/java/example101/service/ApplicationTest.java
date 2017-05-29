/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.service;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import example101.bean.Basket;
import example101.bean.DiscountByCount;
import example101.bean.Discounts;
import example101.bean.Item;
import example101.booter.ServiceBooterTest;

/**
 * @author SAMBATH
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ServiceBooterTest.class)
@TestPropertySource(locations = "classpath:test.yml")
@WebAppConfiguration
public class ApplicationTest {

	@Autowired
	WebApplicationContext webApplicationContext;

	@Autowired
	MockRestServiceServer mockRestServiceServer;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	SmartCheckoutService smartCheckoutService;

	@Value("${discountByCountUri}")
	private String discountByCountUri;

	private Discounts discounts;

	@Before
	public void setup() throws JSONException {
		mockDiscount();
		discounts = restTemplate.getForObject(discountByCountUri, Discounts.class);
		Timer repeater = new Timer();
		repeater.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mockDiscount();
			}
		}, 0, 2000);
	}

	/**
	 * 
	 */
	private void mockDiscount() {
		try {
			mockRestServiceServer.reset();
			mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(discountByCountUri)).andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
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

	@Test
	public void createBasketTest() {
		Response res = smartCheckoutService.startBasket("5454");
		Assert.assertNotNull(res);
		Assert.assertTrue(res.hasEntity());
		Assert.assertTrue(res.getEntity() instanceof Basket);
		Basket refresh = (Basket) res.getEntity();
		Assert.assertNotNull(refresh.getId());
		Assert.assertNotNull(refresh.getItems());
		Assert.assertTrue(refresh.getItems().isEmpty());
		Assert.assertTrue(refresh.getTotalPrice() == -1);
	}

	@Test
	public void addOneItem() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Assert.assertNotNull(res);
		Assert.assertTrue(res.hasEntity());
		Assert.assertTrue(res.getEntity() instanceof Basket);
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 1, -1));
		Assert.assertNotNull(res);
		Assert.assertTrue(res.hasEntity());
		Assert.assertTrue(res.getEntity() instanceof Basket);
		Basket basket = (Basket) res.getEntity();
		Assert.assertNotNull(basket);
		Assert.assertEquals(refresh.getId(), basket.getId());
		Assert.assertNotNull(basket.getItems());
		Assert.assertTrue(basket.getItems().size() == 1);
		Assert.assertNotNull(basket.getItems().get("0"));
		Assert.assertEquals(basket.getItems().get("0").getId(), "0");
		Assert.assertNotEquals(basket.getItems().get("0").getPrice(), -1);

		for (DiscountByCount discountByCount : discounts.getDiscountByCountList()) {
			if (discountByCount.itemId.equals(basket.getItems().get("0").getId()) && discountByCount.count == basket.getItems().get("0").getCount()) {
				Assert.assertTrue(discountByCount.price == basket.getItems().get("0").getPrice());
				Assert.assertTrue(discountByCount.price == basket.getTotalPrice());
				break;
			}
		}
	}

	@Test
	public void addSameItem() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 1, -1));

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 1, -1));

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 1, -1));

		Basket basket = (Basket) res.getEntity();

		for (DiscountByCount discountByCount : discounts.getDiscountByCountList()) {
			if (discountByCount.itemId.equals(basket.getItems().get("0").getId()) && discountByCount.count == basket.getItems().get("0").getCount()) {
				Assert.assertTrue(discountByCount.price == basket.getItems().get("0").getPrice());
				Assert.assertTrue(discountByCount.price == basket.getTotalPrice());
				break;
			}
		}
	}

	@Test
	public void removeItemTest() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 1, -1));

		Basket basket = (Basket) res.getEntity();

		res = smartCheckoutService.removeItem(basket.getId(), new Item("0", 2, -1));
		basket = (Basket) res.getEntity();

		Assert.assertFalse(basket.getItems().isEmpty());

		res = smartCheckoutService.removeItem(basket.getId(), new Item("0", 1, basket.getItems().get("0").getPrice()));
		basket = (Basket) res.getEntity();

		Assert.assertTrue(basket.getItems().isEmpty());
	}

	@Test
	public void addTwoSeparateItemTest() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 4, -1));
		Basket basket = (Basket) res.getEntity();
		DiscountByCount discountByCountForThreeItem0 = null;
		for (DiscountByCount discountByCount : discounts.getDiscountByCountList()) {
			if ("0".equals(discountByCount.itemId) && discountByCount.count == 3) {
				discountByCountForThreeItem0 = discountByCount;
			}
		}
		Assert.assertTrue(Math.round((discountByCountForThreeItem0.price + (discountByCountForThreeItem0.price * 3f * discountByCountForThreeItem0.discount)) * 100f) / 100f == basket.getItems()
				.get("0").getPrice());

		res = smartCheckoutService.addItem(refresh.getId(), new Item("2", 9, -1));
		basket = (Basket) res.getEntity();
		DiscountByCount discountByCountForThreeItem2 = null;
		for (DiscountByCount discountByCount : discounts.getDiscountByCountList()) {
			if ("2".equals(discountByCount.itemId) && discountByCount.count == 3) {
				discountByCountForThreeItem2 = discountByCount;
			}
		}
		Assert.assertTrue(Math.round((discountByCountForThreeItem2.price + (discountByCountForThreeItem2.price * 3f * discountByCountForThreeItem2.discount)) * 100f) / 100f == basket.getItems()
				.get("2").getPrice());
		Assert.assertTrue(Math.round((discountByCountForThreeItem0.price + (discountByCountForThreeItem0.price * 3f * discountByCountForThreeItem0.discount)) * 100f) / 100f == basket.getItems()
				.get("0").getPrice());
		Assert.assertTrue((Math.round((discountByCountForThreeItem2.price + (discountByCountForThreeItem2.price * 3f * discountByCountForThreeItem2.discount)) * 100f) / 100f)
				+ (Math.round((discountByCountForThreeItem0.price + (discountByCountForThreeItem0.price * 3f * discountByCountForThreeItem0.discount)) * 100f) / 100f) == basket.getTotalPrice());
	}

	@Test
	public void addTwoSeparateItemAndRemove() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 4, -1));
		res = smartCheckoutService.addItem(refresh.getId(), new Item("2", 9, -1));
		Basket basket = (Basket) res.getEntity();

		res = smartCheckoutService.removeItem(refresh.getId(), new Item("0", 4, basket.getItems().get("0").getPrice()));
		basket = (Basket) res.getEntity();
		Assert.assertTrue(basket.getItems().size() == 1);
		Assert.assertTrue(basket.getTotalPrice() == basket.getItems().get("2").getPrice());

		res = smartCheckoutService.removeItem(refresh.getId(), new Item("2", 9, basket.getItems().get("2").getPrice()));
		basket = (Basket) res.getEntity();
		Assert.assertTrue(basket.getItems().isEmpty());
		Assert.assertTrue(basket.getTotalPrice() == 0);
	}

	@Test
	public void removeBasket() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.removeBasket(refresh.getId());
		refresh = (Basket) res.getEntity();

		res = smartCheckoutService.getBasket(refresh.getId());
		Assert.assertTrue(res.getEntity() instanceof String);
		Assert.assertEquals(res.getStatusInfo(), javax.ws.rs.core.Response.Status.NOT_FOUND);
	}

	@Test
	public void getBasket() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.getBasket(refresh.getId());
		Assert.assertTrue(res.hasEntity());

		Basket basket = (Basket) res.getEntity();
		Assert.assertEquals(refresh, basket);
	}

	@Test
	public void getBasketNotAvailable() throws JSONException {
		Response res = smartCheckoutService.getBasket("basket_not_available");
		Assert.assertTrue(res.getEntity() instanceof String);
		Assert.assertEquals(res.getStatusInfo(), javax.ws.rs.core.Response.Status.NOT_FOUND);
	}

	@Test
	public void removeItemNotAvailable() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 4, -1));

		res = smartCheckoutService.removeItem(refresh.getId(), new Item("not_available", 1, -1));
		Basket basket = (Basket) res.getEntity();
		Assert.assertTrue("0".equals(basket.getItems().get("0").getId()));
	}

	@Test
	public void removeItemFromNotAvailableBasket() throws JSONException {
		Response res = smartCheckoutService.removeItem("basket_not_available", new Item("2", 1, -1));
		Assert.assertTrue(res.getEntity() instanceof String);
		Assert.assertEquals(res.getStatusInfo(), javax.ws.rs.core.Response.Status.NOT_FOUND);
	}

	@Test
	public void checkout() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.addItem(refresh.getId(), new Item("0", 4, -1));
		res = smartCheckoutService.addItem(refresh.getId(), new Item("2", 9, -1));

		res = smartCheckoutService.checkout(refresh.getId());
		Assert.assertEquals(Status.CREATED, res.getStatusInfo());
		Basket basket = (Basket) res.getEntity();

		Assert.assertFalse(basket.getItems().isEmpty());
		Assert.assertTrue(basket.isComplete());

	}

	@Test
	public void checkoutEmptyBasket() throws JSONException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.checkout(refresh.getId());
		Assert.assertEquals(Status.NOT_ACCEPTABLE, res.getStatusInfo());
	}

	@Test
	public void checkoutBasketNotAvailable() throws JSONException {
		Response res = smartCheckoutService.checkout("basket_not_available");
		Assert.assertEquals(Status.NOT_FOUND, res.getStatusInfo());
	}

	@Test
	public void cleanupBasket() throws JSONException, InterruptedException {
		Response res = smartCheckoutService.startBasket("5454");
		Basket refresh = (Basket) res.getEntity();

		res = smartCheckoutService.getBasket(refresh.getId());
		Assert.assertEquals(res.getStatusInfo(), Status.OK);

		Thread.sleep(6000l);
		res = smartCheckoutService.getBasket(refresh.getId());
		Assert.assertEquals(res.getStatusInfo(), Status.NOT_FOUND);
	}

}