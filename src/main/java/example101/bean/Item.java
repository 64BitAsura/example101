/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author SAMBATH
 *
 */
public final class Item {

	private final String id;
	private final int count;
	private final float price;

	@JsonCreator
	public Item(@JsonProperty("id") String id, @JsonProperty("count") int count, @JsonProperty(value = "price", defaultValue = "-1", required = false) float price) {
		this.id = id;
		this.count = count;
		this.price = price;
	}

	public String getId() {
		return id;
	}

	public int getCount() {
		return count;
	}

	/**
	 * @return the price
	 */
	public float getPrice() {
		return price;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Float.floatToIntBits(price);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (count != other.count)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Float.floatToIntBits(price) != Float.floatToIntBits(other.price))
			return false;
		return true;
	}

}
