/**
 * 
 */
package com.softpoint.optima.control;

/**
 * @author WDARWISH
 *
 */
public class EntityControllerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4984258922277499983L;

	/**
	 * 
	 */
	public EntityControllerException() {
	}

	/**
	 * @param message
	 */
	public EntityControllerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EntityControllerException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EntityControllerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public EntityControllerException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
