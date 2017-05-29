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
public class DiscountByCount {

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
		result = prime * result + Float.floatToIntBits(discount);
		result = prime * result + (int) (expiryTime ^ (expiryTime >>> 32));
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
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
		DiscountByCount other = (DiscountByCount) obj;
		if (count != other.count)
			return false;
		if (Float.floatToIntBits(discount) != Float.floatToIntBits(other.discount))
			return false;
		if (expiryTime != other.expiryTime)
			return false;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		if (Float.floatToIntBits(price) != Float.floatToIntBits(other.price))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DiscountByCount [itemId=" + itemId + ", count=" + count + ", price=" + price + ", discount=" + discount + ", expiryTime=" + expiryTime + "]";
	}

	public final String itemId;

	public final int count;

	public final float price;

	public final float discount;

	public final long expiryTime;

	@JsonCreator
	public DiscountByCount(@JsonProperty("itemId") final String itemId, @JsonProperty("count") final int count, @JsonProperty("price") final float price,
			@JsonProperty("discount") final float discount, @JsonProperty("expiryTime") final long expiryTime) {
		this.itemId = itemId;
		this.count = count;
		this.price = price;
		this.discount = discount;
		this.expiryTime = expiryTime;
	}

}
