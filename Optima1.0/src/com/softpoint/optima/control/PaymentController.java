package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.PaymentType;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectPayment;

/**
 * @author user mhamdy
 *
 */
public class PaymentController {

	
	
	
	
	
	/**
	 * @param session
	 * @param projectId
	 * @param paymentTypeId
	 * @param paymentAmount
	 * @param paymentDate
	 * @param paymentInterimNumber
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session ,int projectId, int paymentTypeId, double paymentAmount, double paymentInitialAmount,  Date paymentDate, String paymentInterimNumber) throws OptimaException {
		
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		
		//Instantiate a project Payment and set its properties
		ProjectPayment projectPayment = new ProjectPayment();
		projectPayment.setPaymentAmount(new BigDecimal(paymentAmount));
		projectPayment.setPaymentInitialAmount(new BigDecimal(paymentInitialAmount));
		projectPayment.setPaymentDate(paymentDate);
		projectPayment.setPaymentInterimNumber(paymentInterimNumber);
		projectPayment.setPaymentId(paymentTypeId);
		
		
		try {
			
			//Get the PaymentType using the PaymentTypeId
			EntityController<PaymentType> paymentController = new EntityController<PaymentType>(session.getServletContext());
			PaymentType paymentType = paymentController.find(PaymentType.class, paymentTypeId);
			projectPayment.setPaymentType(paymentType);
			
			//Get the Project and set it to the Project Payment
			EntityController<Project> ProjectController = new EntityController<Project>(session.getServletContext());
			Project project = ProjectController.find(Project.class, projectId);
			projectPayment.setProject(project);
			
			//Finally Persist the projectPayment
			controller.persist(projectPayment);
			return new ServerResponse("0", "Success", projectPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0001" , String.format("Error creating Finance Portoflio %s: %s" , projectPayment , e.getMessage() ), e);
		}
	}
	

	
	/**
	 * @param session
	 * @param projectId
	 * @param paymentTypeId
	 * @param paymentAmount
	 * @param paymentDate
	 * @param paymentInterimNumber
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session,int projectId,int paymentId, int paymentTypeId, double paymentAmount, double paymentInitialAmount, Date paymentDate, String paymentInterimNumber) throws OptimaException {
		
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		
		ProjectPayment projectPayment =null;
		Project project = null;
		try {
			//Get the Project from the ProjectId
			EntityController<Project> ProjectController = new EntityController<Project>(session.getServletContext());
			project = ProjectController.find(Project.class, projectId);
			
			//Set the projectPayment
			projectPayment = controller.find(ProjectPayment.class, paymentId);
			projectPayment.setPaymentAmount(new BigDecimal(paymentAmount));
			projectPayment.setPaymentInitialAmount(new BigDecimal(paymentInitialAmount));
			projectPayment.setPaymentDate(paymentDate);
			projectPayment.setPaymentInterimNumber(paymentInterimNumber);
			projectPayment.setProject(project);
			
			
			//Get the PaymentType using the PaymentTypeId
			EntityController<PaymentType> paymentController = new EntityController<PaymentType>(session.getServletContext());
			PaymentType paymentType = paymentController.find(PaymentType.class, paymentTypeId);
			projectPayment.setPaymentType(paymentType);
			
			//merge the projectPayment
			controller.merge(projectPayment);
			return new ServerResponse("0", "Success", projectPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0002" , String.format("Error updating projectPayment for Project %s: %s" , project!=null?project.getProjectName():"", e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param paymentId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , int paymentId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			controller.remove(ProjectPayment.class , paymentId);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("FINA0004" , String.format("Error removing ProjectPayment %d: %s" , paymentId , e.getMessage() ), e);
		}
	}

	
	
	/**
	 * @param session
	 * @param paymentId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , int paymentId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			ProjectPayment projectPayment = controller.find(ProjectPayment.class, paymentId);
			return new ServerResponse("0", "Success",projectPayment);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0003" , String.format("Error looking up finance %d: %s" , paymentId , e.getMessage() ), e);
		}
	}
		

	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			List<ProjectPayment> projectPayments = controller.findAll(ProjectPayment.class);
			return new ServerResponse("0", "Success", projectPayments);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0005" , String.format("Error loading projectPayments : %s" , e.getMessage() ), e);
		}
	}
	

	/**
	 * @param session
	 * @param projectId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session , int projectId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			
			EntityController<Project> projectcontroller = new EntityController<Project>(session.getServletContext());
			Project project = projectcontroller.find(Project.class, projectId);
			List<ProjectPayment> projectPayments = controller.findAll(ProjectPayment.class , "Select d from ProjectPayment d where d.project = :1 " , project);
			return new ServerResponse("0", "Success", projectPayments);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0006" , String.format("Error loading projectPayments : %s" , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param projectId
	 * @param paymentDate
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse removePaymentsByDate(HttpSession session , int projectId, Date paymentDate) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
	
			EntityController<Project> projectcontroller = new EntityController<Project>(session.getServletContext());
			Project project = projectcontroller.find(Project.class, projectId);
			controller.dml(ProjectPayment.class, "Delete from ProjectPayment p Where p.project = ?1 and p.paymentDate = ?2" , project , paymentDate);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0010" , String.format("Error loading projectPayments : %s" , e.getMessage() ), e);
		}
	}
	
	

		
	public ServerResponse getIntrimPaymentsPerProject(HttpSession session, int projectId) throws OptimaException {
		EntityController<ProjectPayment> paymentController = new EntityController<>(session.getServletContext());
		try {
			EntityController<Project> projectController = new EntityController<>(session.getServletContext());
			Project project = projectController.find(Project.class, projectId);
			List<ProjectPayment> payments = paymentController.findAll(ProjectPayment.class,
					"Select p from ProjectPayment p where p.project = ?1", project);
			List<ProjectPayment> intrimPayments = new ArrayList<>();
			for (ProjectPayment payment : payments) {
				if (payment.getPaymentType().getPaymentTypeId() == 2) {
					intrimPayments.add(payment);
				}
			}
			return new ServerResponse("0" , "Success" , intrimPayments);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0011" , String.format("Error loading projectPayments : %s" , e.getMessage() ), e);
		}
	}

	
	public ServerResponse submitAnInterimPayment(HttpSession session, int paymentId, double paymentAmount) throws OptimaException {
		EntityController<ProjectPayment> paymentController = new EntityController<>(session.getServletContext());
		try {
			ProjectPayment payment = paymentController.find(ProjectPayment.class, paymentId);
			payment.setPaymentAmount(new BigDecimal(paymentAmount));
			paymentController.merge(payment);
			return new ServerResponse("0" , "Success" , null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0012" , String.format("Error submitting projectPayments : %s" , e.getMessage() ), e);
		}
	}

	
}
