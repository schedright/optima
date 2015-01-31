/**
 * 
 */
package com.softpoint.optima.control;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.Project;

/**
 * @author WDARWISH
 *
 */
public class DaysOffController {

	/**
	 * 
	 */
	public DaysOffController() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param session
	 * @param name
	 * @param description
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session , Date date , String type , int projectId ) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		DaysOff dayOff = new DaysOff();
		dayOff.setDayOff(date);
		dayOff.setDayoffType(type);
		try {
			
			EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
			Project project = projectController.find(Project.class, projectId);
			dayOff.setProject(project);
			controller.persist(dayOff);
			return new ServerResponse("0", "Success", dayOff);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0001" , String.format("Error creating Day Off %s: %s" , date.toString() , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session , int key ,  Date date , String type , int projectId) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		try {
			DaysOff dayOff = controller.find(DaysOff.class, key);
			dayOff.setDayOff(date);
			dayOff.setDayoffType(type);
			EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
			Project project = projectController.find(Project.class, projectId);
			dayOff.setProject(project);
			controller.merge(dayOff);
			return new ServerResponse("0", "Success", dayOff);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0002" , String.format("Error updating DayOff %s: %s" , date.toString(), e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , Integer key) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		try {
			DaysOff dayOff = controller.find(DaysOff.class , key);
			return new ServerResponse("0", "Success", dayOff);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0003" , String.format("Error looking up day off %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , Integer key) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		try {
			controller.remove(DaysOff.class , key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0004" , String.format("Error removing DaysOff %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		try {
			List<DaysOff> dayOffs = controller.findAll(DaysOff.class);
			return new ServerResponse("0", "Success", dayOffs);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0005" , String.format("Error loading clients : %s" , e.getMessage() ), e);
		}
	}
	
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByProject(HttpSession session , int projectId) throws OptimaException {
		EntityController<DaysOff> controller = new EntityController<DaysOff>(session.getServletContext());
		try {
			
			EntityController<Project> projectController = new EntityController<Project>(session.getServletContext());
			Project project = projectController.find(Project.class, projectId);
			List<DaysOff> dayOffs = controller.findAll(DaysOff.class , "Select d from DaysOff d where d.project = ?1 " , project);
			return new ServerResponse("0", "Success", dayOffs);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("DOFF0006" , String.format("Error loading clients : %s" , e.getMessage() ), e);
		}
	}
	
	

}
