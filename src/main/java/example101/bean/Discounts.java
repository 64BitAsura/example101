/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.bean;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;

/**
 * @author SAMBATH
 *
 */
public class Discounts {

	private final ImmutableSortedSet<DiscountByCount> discountByCountList;

	@JsonCreator
	public Discounts(@JsonProperty("discountByCountList") List<DiscountByCount> discountByCountList) {
		this.discountByCountList = new ImmutableSortedSet.Builder<DiscountByCount>(new Comparator<DiscountByCount>() {

			@Override
			public int compare(DiscountByCount o1, DiscountByCount o2) {
				return 1;
			}
		}).addAll(discountByCountList).build();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Discounts [discountByCountList=" + discountByCountList + "]";
	}

	/**
	 * @return the discountByCountList
	 */
	public Set<DiscountByCount> getDiscountByCountList() {
		return discountByCountList;
	}

}
