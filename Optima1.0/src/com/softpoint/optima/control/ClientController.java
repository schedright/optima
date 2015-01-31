/**
 * 
 */
package com.softpoint.optima.control;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Client;
import com.softpoint.optima.db.LocationInfo;

/**
 * @author WDARWISH
 *
 */
public class ClientController {

	/**
	 * 
	 */
	public ClientController() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * @param session
	 * @param name
	 * @param description
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session , String name , String streetAddress , String postalCode , int city , int province , int country) throws OptimaException {
		EntityController<Client> controller = new EntityController<Client>(session.getServletContext());
		Client client = new Client();
		client.setClientAddressPostalCode(postalCode);
		client.setClientAddressStreet(streetAddress);
		client.setClientName(name);
		try {
			
			EntityController<LocationInfo> locInfoController = new EntityController<LocationInfo>(session.getServletContext());
			LocationInfo cityInfo = locInfoController.find(LocationInfo.class, city);
			client.setCity(cityInfo);
			LocationInfo provinceInfo = locInfoController.find(LocationInfo.class, province);
			client.setProvince(provinceInfo);
			LocationInfo countryInfo = locInfoController.find(LocationInfo.class, country);
			client.setCountry(countryInfo);
			controller.persist(client);
			return new ServerResponse("0", "Success", client);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("CLNT0001" , String.format("Error creating Client %s: %s" , name , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session , int key , String name , String streetAddress , String postalCode , int city , int province , int country) throws OptimaException {
		EntityController<Client> controller = new EntityController<Client>(session.getServletContext());
		try {
			Client client = controller.find(Client.class, key);
			client.setClientAddressPostalCode(postalCode);
			client.setClientAddressStreet(streetAddress);
			client.setClientName(name);
			EntityController<LocationInfo> locInfoController = new EntityController<LocationInfo>(session.getServletContext());
			LocationInfo cityInfo = locInfoController.find(LocationInfo.class, city);
			client.setCity(cityInfo);
			LocationInfo provinceInfo = locInfoController.find(LocationInfo.class, province);
			client.setProvince(provinceInfo);
			LocationInfo countryInfo = locInfoController.find(LocationInfo.class, country);
			client.setCountry(countryInfo);
			controller.merge(client);
			return new ServerResponse("0", "Success", client);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("CLNT0002" , String.format("Error updating Client %s: %s" , name, e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , Integer key) throws OptimaException {
		EntityController<Client> controller = new EntityController<Client>(session.getServletContext());
		try {
			Client client = controller.find(Client.class , key);
			return new ServerResponse("0", "Success", client);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("CLNT0003" , String.format("Error looking up Client %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , Integer key) throws OptimaException {
		EntityController<Client> controller = new EntityController<Client>(session.getServletContext());
		try {
			controller.remove(Client.class , key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("CLNT0004" , String.format("Error removing Client %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<Client> controller = new EntityController<Client>(session.getServletContext());
		try {
			List<Client> clients = controller.findAll(Client.class);
			return new ServerResponse("0", "Success", clients);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("CLNT0005" , String.format("Error loading clients : %s" , e.getMessage() ), e);
		}
	}

}
