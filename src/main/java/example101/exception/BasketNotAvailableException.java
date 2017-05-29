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
public class BasketNotAvailableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2231630253619273724L;

	public BasketNotAvailableException(final String basketId) {
		super("basket is removed, seems race condition from client side basketId: " + basketId);
	}

}
