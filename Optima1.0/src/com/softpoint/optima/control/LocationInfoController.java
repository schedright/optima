/**
 * 
 */
package com.softpoint.optima.control;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.LocationInfo;

/**
 * @author WDARWISH
 *
 */
public class LocationInfoController {

	/**
	 * 
	 */
	public LocationInfoController() {
		
	}
	
	/**
	 * @param session
	 * @param name
	 * @param description
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session , String locationName , String locationType , int parentLocationId) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		try {
			LocationInfo parentLocationInfo = controller.find(LocationInfo.class, parentLocationId);
			LocationInfo info = new LocationInfo();
			info.setLocationName(locationName);
			info.setLocationType(locationType);
			info.setParentLocation(parentLocationInfo);
			controller.persist(info);
			return new ServerResponse("0", "Success", info);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("LOCA0001" , String.format("Error creating location %s: %s" , locationName , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session , int key , String locationName , String locationType , int parentLocationId ) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		LocationInfo info = null;
		try {
			info = controller.find(LocationInfo.class, key);
			info.setLocationName(locationName);
			info.setLocationType(locationType);
			LocationInfo parent = controller.find(LocationInfo.class, parentLocationId);
			info.setParentLocation(parent);
			controller.merge(info);
			return new ServerResponse("0", "Success", info);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("LOCA0002" , String.format("Error updating Location %s: %s" , info!=null?info.getLocationName():"", e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , Integer key) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		try {
			LocationInfo info = controller.find(LocationInfo.class , key);
			return new ServerResponse("0", "Success", info);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0003" , String.format("Error looking up location %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , Integer key) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		try {
			controller.remove(LocationInfo.class , key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("LOCA0004" , String.format("Error removing LocationInfo %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		try {
			List<LocationInfo> locationInfos = controller.findAll(LocationInfo.class);
			return new ServerResponse("0", "Success", locationInfos);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("LOCA0005" , String.format("Error loading LocationInfo : %s" , e.getMessage() ), e);
		}
	}

	
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByType(HttpSession session , String locationType) throws OptimaException {
		EntityController<LocationInfo> controller = new EntityController<LocationInfo>(session.getServletContext());
		try {
			List<LocationInfo> locationInfos = controller.findAll(LocationInfo.class, "Select l From LocationInfo l where l.locationType = ?1" , locationType);
			return new ServerResponse("0", "Success", locationInfos);                 
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("LOCA0006" , String.format("Error loading LocationInfo : %s" , e.getMessage() ), e);
		}
	}

}
