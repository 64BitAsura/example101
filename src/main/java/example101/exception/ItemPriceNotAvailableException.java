/**
 * (c) Copyright 2009-2015 Velocimetrics Ltd
 * All Rights Reserved
 * Permission is granted to licensees to use
 * or alter this software for any purpose, including commercial applications,
 * only according to the terms laid out in the Software License Agreement.
 * All other use of this source code is expressly forbidden.
 */
package example101.exception;

/**
 * @author SAMBATH
 *
 */
public class ItemPriceNotAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -926350476930860128L;

	public ItemPriceNotAvailableException(String itemId) {
		super("Item price is available in list, item id: " + itemId);
	}

}
