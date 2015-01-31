/**
 * 
 */
package com.softpoint.optima;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jabsorb.JSONRPCServlet;

/**
 * @author WDARWISH
 *
 */
public class OptimaRPCServlet extends JSONRPCServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1347567308324879860L;

	/**
	 * 
	 */
	public OptimaRPCServlet() {
	
	}
	@Override
	public void service(HttpServletRequest arg0, HttpServletResponse arg1)
			throws IOException {
		
		super.service(arg0, arg1);
	}
}
