/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import example101.bean.DiscountByCount;
import example101.bean.Item;
import example101.exception.ItemPriceNotAvailableException;
import jersey.repackaged.com.google.common.collect.ImmutableMultimap;

/**
 * @author SAMBATH
 *
 */
public class ItemPriceCalculator {

	private final ImmutableMultimap<String, DiscountByCount> priceTable;

	public ItemPriceCalculator(ImmutableMultimap<String, DiscountByCount> priceTable) {
		this.priceTable = priceTable;
	}

	// O[n] time performance
	public Item cal(final Item item) throws ItemPriceNotAvailableException {
		Collection<DiscountByCount> discountByCountCol = priceTable.get(item.getId());
		if (discountByCountCol.isEmpty()) {
			throw new ItemPriceNotAvailableException(item.getId());
		}
		List<DiscountByCount> discountByCountList = new ArrayList<>(discountByCountCol);
		Collections.sort(discountByCountList, new Comparator<DiscountByCount>() {

			@Override
			public int compare(DiscountByCount from, DiscountByCount to) {
				return to.count - from.count;
			}
		});

		int curItmCount = item.getCount();
		float price = 0;
		for (DiscountByCount discountByCount : discountByCountList) {
			if (discountByCount.expiryTime < new Date().getTime()) {
				continue;
			}
			int reminder = curItmCount % discountByCount.count;
			if (reminder == curItmCount) {
				continue;
			}
			int dividen = curItmCount - reminder;
			price += (dividen * discountByCount.price * discountByCount.discount);
			curItmCount = reminder;
		}
		return new Item(item.getId(), item.getCount(), (Math.round(price * 100.0f) / 100.0f));
	}

}