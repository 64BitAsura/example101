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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import example101.bean.Basket;
import example101.bean.DiscountByCount;
import example101.bean.Discounts;
import example101.bean.Item;
import example101.exception.ItemPriceNotAvailableException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.ImmutableMultimap;

/**
 * @author SAMBATH
 *
 */
@Component
@Api(value = "checkout api for basket, use api must interact with basket as AA+ promise pattern to avoid race condition", produces = MediaType.APPLICATION_JSON)
public class SmartCheckoutService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmartCheckoutService.class);

	private final ConcurrentHashMap<String, Basket> activeBasket = new ConcurrentHashMap<>();
	private ItemPriceCalculator itemPriceCalculator;

	@Value("${discountByCountUri}")
	private String discountByCountUri;

	@Value("${inactiveLimit}")
	private long inactiveLimit;

	@Autowired
	RestTemplate restTemplate;

	@GET
	@Path("/")
	public Response root() {
		return Response.ok().build();
	}

	@POST
	@Path("{posid}/basket")
	@ApiOperation(value = "create new basket and return empty basket with id", response = Basket.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "created empty basket") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response startBasket(@ApiParam @PathParam("posid") String posUuid) {
		String basketId = UUID.randomUUID().toString();
		activeBasket.put(basketId, new Basket(basketId, new ImmutableMap.Builder<String, Item>().build(), false, -1));
		return Response.ok(activeBasket.get(basketId)).build();
	}

	@GET
	@Path("{posid}/basket/{basketId}")
	@ApiOperation(value = "retrieve basket for requested basked id", response = Basket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "basket is not available for requested basket id, either basket removed after reaching inactive limit or checked out or dont exist at all"),
			@ApiResponse(code = 200, message = "return active basket for request id") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBasket(@ApiParam @PathParam("basketId") String basketId) throws JSONException {
		final Basket basket = activeBasket.get(basketId);
		if (basket == null) {
			// return warning in http status with message
			LOGGER.warn("basket not found, baskedid: " + basketId);
			final String message = new String("basket not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(json.toString()).build();
		}
		return Response.ok(basket).build();
	}

	@PUT
	@Path("{posid}/basket/{basketId}/item")
	@ApiOperation(value = "add item to requested basket with id, calculate price for item with discount and refresh basket total price", response = Basket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "basket is not available for requested basket id, either basket removed after reaching inactive limit or checked out or dont exist at all"),
			@ApiResponse(code = 200, message = "return update basket with item") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addItem(@ApiParam @PathParam("basketId") String basketId, @ApiParam final Item item) throws JSONException {
		final Basket basket = activeBasket.get(basketId);
		if (basket == null) {
			// return warning in http status with message
			LOGGER.warn("basket not found, baskedid: " + basketId);
			final String message = new String("basket not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(json.toString()).build();
		}
		Basket updated = null;
		try {
			updated = updateItem(basket, item);
		} catch (ItemPriceNotAvailableException e) {
			LOGGER.error("add item exception", e);
			final String message = new String("item not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(Status.NOT_FOUND).entity(json.toString()).build();
		}
		return Response.ok(updated).build();
	}

	@DELETE
	@Path("{posid}/basket/{basketId}/item")
	@ApiOperation(value = "remove requested item from basket and refresh basket total price", response = Basket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "basket is not available for requested basket id, either basket removed after reaching inactive limit or checked out or dont exist at all"),
			@ApiResponse(code = 200, message = "return updated basket") })
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeItem(@ApiParam @PathParam("basketId") String basketId, @ApiParam final Item item) throws JSONException {
		final Basket basket = activeBasket.get(basketId);
		if (basket == null) {
			// return warning in http status with message
			LOGGER.warn("basket not found, baskedid: " + basketId);
			final String message = new String("basket not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(Status.NOT_FOUND).entity(json.toString()).build();
		}
		final Basket updated = removeItem(basket, item);
		return Response.ok(updated).build();
	}

	@DELETE
	@Path("{posid}/basket/{basketId}")
	@ApiOperation(value = "remove requested basket from checkout process and completely removed from state", response = Basket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "basket is not available for requested basket id, either basket removed after reaching inactive limit or checked out or dont exist at all"),
			@ApiResponse(code = 200, message = "return basket after removed checkout process") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeBasket(@ApiParam @PathParam(value = "basketId") final String basketId) throws JSONException {
		final Basket basket = activeBasket.remove(basketId);
		if (basket == null) {
			// return warning in http status with message
			LOGGER.warn("basket not found, baskedid: " + basketId);
			final String message = new String("basket not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(json.toString()).build();
		}
		return Response.ok(basket).build();
	}

	@POST
	@Path("{posid}/basket/{basketId}/checkout")
	@ApiOperation(value = "get ready basket for checkout", response = Basket.class)
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "basket is not available for requested basket id, either basket removed after reaching inactive limit or checked out or dont exist at all"),
			@ApiResponse(code = 406, message = "it empty basket cant process for checkout and removed from checkout process"),
			@ApiResponse(code = 200, message = "return basket with complete to do checkout") })
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkout(@ApiParam @PathParam("basketId") String basketId) throws JSONException {
		final Basket basket = activeBasket.remove(basketId);
		if (basket == null) {
			// return warning in http status with message
			LOGGER.warn("basket not found, baskedid: " + basketId);
			final String message = new String("basket not found");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(Status.NOT_FOUND).entity(json.toString()).build();
		} else if (basket.getItems().isEmpty()) {
			LOGGER.warn("basket is empty, basketid: " + basketId);
			final String message = new String("basket is empty");
			JSONObject json = new JSONObject();
			json.put("message", message);
			return Response.status(Status.NOT_ACCEPTABLE).entity(json.toString()).build();
		}
		return Response.status(Status.CREATED).entity(new Basket(basket.getId(), basket.getItems(), true, basket.getTotalPrice())).build();
	}

	private Basket removeItem(Basket basket, Item item) {
		synchronized (basket) {
			ImmutableMap.Builder<String, Item> builder = new ImmutableMap.Builder<>();
			for (Item oldImt : basket.getItems().values()) {
				if (item.equals(oldImt)) {
					continue;
				}
				builder.put(oldImt.getId(), oldImt);
			}
			ImmutableMap<String, Item> itmMap = builder.build();
			float totalPrice = 0f;
			for (Item itm : itmMap.values()) {
				totalPrice += itm.getPrice();
			}
			Basket refresh = new Basket(basket.getId(), builder.build(), false, totalPrice);
			activeBasket.put(refresh.getId(), refresh);
			return refresh;
		}
	}

	private Basket updateItem(Basket basket, Item item) throws ItemPriceNotAvailableException {
		synchronized (basket) {
			Item update = itemPriceCalculator.cal(item);
			jersey.repackaged.com.google.common.collect.ImmutableMap.Builder<String, Item> builder = new ImmutableMap.Builder<String, Item>();
			for (Item oldItm : basket.getItems().values()) {
				if (!oldItm.getId().equals(item.getId())) {
					builder.put(oldItm.getId(), oldItm);
				}
			}
			builder.put(update.getId(), update);
			ImmutableMap<String, Item> itmMap = builder.build();
			float totalPrice = 0f;
			for (Item itm : itmMap.values()) {
				totalPrice += itm.getPrice();
			}
			Basket refresh = new Basket(basket.getId(), builder.build(), false, totalPrice);
			activeBasket.put(basket.getId(), refresh);
			return refresh;
		}
	}

	@PostConstruct
	public void init() {
		updateDiscount();
	}

	@Scheduled(fixedRateString = "${priceRefreshRate}")
	public void updateDiscount() {
		final Discounts discounts = restTemplate.getForObject(discountByCountUri, Discounts.class);
		final Set<DiscountByCount> discountByCountList = discounts.getDiscountByCountList();
		final ImmutableMultimap.Builder<String, DiscountByCount> builder = new ImmutableMultimap.Builder<String, DiscountByCount>();
		for (DiscountByCount discountByCount : discountByCountList) {
			builder.put(discountByCount.itemId, discountByCount);
		}
		itemPriceCalculator = new ItemPriceCalculator(builder.build());
	}

	@Scheduled(fixedRateString = "${cleanupRate}")
	public void cleanBasket() {
		Hashtable<String, Basket> inactives = new Hashtable<>();
		Iterator<Entry<String, Basket>> iterator = activeBasket.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Basket> next = iterator.next();
			if ((next.getValue().getUpdateTime().getTime() - new Date().getTime()) < inactiveLimit) {
				inactives.put(next.getKey(), next.getValue());
			}
		}
		for (String basketId : inactives.keySet()) {
			activeBasket.remove(basketId, inactives.get(basketId));
		}
		LOGGER.warn("inactive basket are removed", inactives);
	}

}