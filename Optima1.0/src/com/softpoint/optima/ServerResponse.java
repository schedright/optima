/**
 * 
 */
package com.softpoint.optima;

/**
 * @author WDARWISH
 *
 */
public class ServerResponse {
	private String result;
	private String message;
	private Object data;
	
	
	/**
	 * @param result
	 * @param message
	 * @param data
	 */
	public ServerResponse(String result, String message, Object data) {
		super();
		this.result = result;
		this.message = message;
		this.data = data;
	}
	/**
	 * 
	 */
	public ServerResponse() {
		
	}
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

}
