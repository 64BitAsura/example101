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

import org.junit.Assert;
import org.junit.Test;

import example101.bean.DiscountByCount;
import example101.bean.Item;
import example101.exception.ItemPriceNotAvailableException;
import jersey.repackaged.com.google.common.collect.ImmutableMultimap;

/**
 * @author SAMBATH
 *
 */
public class ItemPriceCalculatorTest {

	@Test
	public void calculateThreeAndFiveItemAndDiscountForOnlyTwo() throws ItemPriceNotAvailableException {

		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round(discountForOne.price * 100.0f) / 100.0f == new ItemPriceCalculator(
				new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo).build())
						.cal(new Item("item-xx-xx", 1, -1)).getPrice());

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 2f * discountForTwo.discount)) * 100.0f) / 100.0f == new ItemPriceCalculator(
				new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo).build())
						.cal(new Item("item-xx-xx", 3, -1)).getPrice());

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 4f * discountForTwo.discount)) * 100.0f) / 100.0f == new ItemPriceCalculator(
				new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo).build())
						.cal(new Item("item-xx-xx", 5, -1)).getPrice());

	}

	@Test
	public void calculateSevenItemAndDiscountForTwoAndThree() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForThree = new DiscountByCount("item-xx-xx", 3, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 6f * discountForThree.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForThree.itemId, discountForThree).build()).cal(new Item("item-xx-xx", 7, -1)).getPrice());

	}

	@Test
	public void calculateSixItemAndDiscountForTwoAndThree() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForThree = new DiscountByCount("item-xx-xx", 3, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round(((discountForOne.price * 3f * discountForThree.discount) + (discountForOne.price * 3f * discountForThree.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForThree.itemId, discountForThree).build()).cal(new Item("item-xx-xx", 6, -1)).getPrice());

	}

	@Test
	public void calculateSevenItemAndDiscountForTwoAndThreeInExpiry() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForThree = new DiscountByCount("item-xx-xx", 3, 4.99f, 0.75f, new Date().getTime() - 6000);

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 6f * discountForTwo.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForThree.itemId, discountForThree).build()).cal(new Item("item-xx-xx", 7, -1)).getPrice());

	}

	@Test
	public void calculateSevenItemAndDiscountForTwoAndTwoDiscountForThreeButOneInExpiry() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForThreeInExpiry = new DiscountByCount("item-xx-xx", 3, 6.0f, 0.80f, new Date().getTime() - 6000);

		DiscountByCount discountForThree = new DiscountByCount("item-xx-xx", 3, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 6f * discountForThree.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForThreeInExpiry.itemId, discountForThreeInExpiry).put(discountForThree.itemId, discountForThree).build()).cal(new Item("item-xx-xx", 7, -1)).getPrice());

	}

	@Test(expected = ItemPriceNotAvailableException.class)
	public void calculateItemNotAvailable() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForThree = new DiscountByCount("item-xx-xx", 3, 4.99f, 0.75f, new Date().getTime() + 60000);

		new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
				.put(discountForThree.itemId, discountForThree).build()).cal(new Item("item-xx-fh", 7, -1)).getPrice();

	}

	@Test
	public void calculateSevenItemAndDiscountForTwoAndFour() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForFour = new DiscountByCount("item-xx-xx", 4, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 2f * discountForTwo.discount) + (discountForOne.price * 4f * discountForFour.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForFour.itemId, discountForFour).build()).cal(new Item("item-xx-xx", 7, -1)).getPrice());

	}

	@Test
	public void calculateSevenItemAndDiscountForTwoAndOtherItemDiscountForThree() throws ItemPriceNotAvailableException {
		DiscountByCount discountForOne = new DiscountByCount("item-xx-xx", 1, 4.99f, 1.0f, new Date().getTime() + 60000);

		DiscountByCount discountForTwo = new DiscountByCount("item-xx-xx", 2, 4.99f, 0.90f, new Date().getTime() + 60000);

		DiscountByCount discountForOtherItemThree = new DiscountByCount("item-xx-xz", 3, 4.99f, 0.75f, new Date().getTime() + 60000);

		Assert.assertTrue(Math.round((discountForOne.price + (discountForOne.price * 6f * discountForTwo.discount)) * 100f)
				/ 100f == new ItemPriceCalculator(new ImmutableMultimap.Builder<String, DiscountByCount>().put(discountForOne.itemId, discountForOne).put(discountForTwo.itemId, discountForTwo)
						.put(discountForOtherItemThree.itemId, discountForOtherItemThree).build()).cal(new Item("item-xx-xx", 7, -1)).getPrice());

	}

}
