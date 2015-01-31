/**
 * 
 */
package com.softpoint.optima;

/**
 * @author WDARWISH
 *
 */
public class OptimaException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public OptimaException() {
		
	}

	/**
	 * @param message
	 */
	public OptimaException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OptimaException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OptimaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public OptimaException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
