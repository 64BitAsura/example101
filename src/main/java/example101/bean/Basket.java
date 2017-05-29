/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.bean;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

/**
 * @author SAMBATH
 *
 */
public final class Basket {

	private final String id;

	private final ImmutableMap<String, Item> items;

	private final Date updateTime = new Date();

	private final boolean complete;

	private final float totalPrice;

	@JsonCreator
	public Basket(@JsonProperty("basketId") String basketId, @JsonProperty("items") Map<String, Item> items, @JsonProperty("complete") boolean complete, @JsonProperty("totalPrice") float totalPrice) {
		this.id = basketId;
		final ImmutableMap.Builder<String, Item> builder = new ImmutableMap.Builder<String, Item>();
		for (Entry<String, Item> ien : items.entrySet()) {
			builder.put(ien.getKey(), ien.getValue());
		}
		this.items = builder.build();
		this.complete = complete;
		this.totalPrice = totalPrice;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the items
	 */
	public ImmutableMap<String, Item> getItems() {
		return items;
	}

	/**
	 * @return the totalPrice
	 */
	public float getTotalPrice() {
		return totalPrice;
	}

	/**
	 * @return the updateTime
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * @return the complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Basket [id=" + id + ", items=" + items + ", updateTime=" + updateTime + ", complete=" + complete + ", totalPrice=" + totalPrice + "]";
	}

}