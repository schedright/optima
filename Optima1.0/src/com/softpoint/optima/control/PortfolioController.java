/**
 * 
 */
package com.softpoint.optima.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectPayment;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;
import com.softpoint.optima.struct.Period;
import com.softpoint.optima.struct.PeriodCashout;
import com.softpoint.optima.struct.ProjectPaymentDetail;
import com.softpoint.optima.struct.SchedulePeriod;
import com.softpoint.optima.util.PaymentUtil;

/**
 * @author WDARWISH
 *
 */
public class PortfolioController {

	
	public ServerResponse getSchedulePeriod(HttpSession session ,Date date, int portfolioId) 
	{
		
		SchedulePeriod schedulePeriod = new SchedulePeriod();
		

		try {
			
			Period paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session, date, portfolioId);
			Period financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, date, portfolioId);
			
			//Construct the current period
			Period current = new Period();
			
			
			if(paymentSchedulePeriod.getDateFrom().getTime() >= financeSchedulePeriod.getDateFrom().getTime()){
				current.setDateFrom(paymentSchedulePeriod.getDateFrom());
			} else {
				current.setDateFrom(financeSchedulePeriod.getDateFrom());
			}
			
			if(paymentSchedulePeriod.getDateTo().getTime() <= financeSchedulePeriod.getDateTo().getTime()){
				current.setDateTo(paymentSchedulePeriod.getDateTo());				
			} else {
				current.setDateTo(financeSchedulePeriod.getDateTo());
			} 
		    
			//Construct the current to and the next period
			Period next = new Period();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(current.getDateTo());
			calendar.add(Calendar.DATE, 1);
			
		    paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session, calendar.getTime(), portfolioId);
			financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session,  calendar.getTime(), portfolioId);
			
			if(paymentSchedulePeriod.getDateFrom().getTime() >= financeSchedulePeriod.getDateFrom().getTime()){
				next.setDateFrom(paymentSchedulePeriod.getDateFrom());
			} else {
				next.setDateFrom(financeSchedulePeriod.getDateFrom());
			}
			
			if(paymentSchedulePeriod.getDateTo().getTime() <= financeSchedulePeriod.getDateTo().getTime()){
				next.setDateTo(paymentSchedulePeriod.getDateTo());				
			} else {
				next.setDateTo(financeSchedulePeriod.getDateTo());
			} 
		
			schedulePeriod.setCurrent(current);
			schedulePeriod.setNext(next);
				
			return new ServerResponse( "0", "Success" , schedulePeriod);
			
		} catch (OptimaException e) {
			
			e.printStackTrace();
			return new ServerResponse( "9004", "Error: " + e.getMessage() , null);
		}
	}
	
	boolean dateExistInDateSet(List<Date> dateSet, Date date)
	{
		for(Iterator<Date> dateIter = dateSet.iterator(); dateIter.hasNext();)
		{
			if(date.getTime() == dateIter.next().getTime())
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * @param session
	 * @param name
	 * @param description
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session , String name , String description) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = new Portfolio();
		portfolio.setPortfolioDescreption(description);
		portfolio.setPortfolioName(name);
		try {
			controller.persist(portfolio);
			return new ServerResponse("0", "Success", portfolio);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0001" , String.format("Error creating Portfolio %s: %s" , name , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session , int key,  String name , String description) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = null;
		try {
			portfolio = controller.find(Portfolio.class, key);
			portfolio.setPortfolioDescreption(description);
			portfolio.setPortfolioName(name);
			controller.merge(portfolio);
			return new ServerResponse("0", "Success", portfolio);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0002" , String.format("Error updating Portfolio %s: %s" , portfolio!=null?portfolio.getPortfolioName():"", e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , Integer key) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class , key);
			return new ServerResponse("0", "Success", portfolio);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0003" , String.format("Error looking up Portfolio %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , Integer key) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			controller.remove(Portfolio.class , key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0004" , String.format("Error removing Portfolio %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			List<Portfolio> portfolios = controller.findAll(Portfolio.class);
			return new ServerResponse("0", "Success", portfolios);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0005" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);
		}
	}
	


	/** 
	 * @param session
	 * @param portfolioId
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse getPortfolioDateRange(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			return new ServerResponse("0", "Success", PaymentUtil.getPortofolioDateRanges(controller, portfolioId));
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0006" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);
		}
	}
	
	
	

	public ServerResponse	getPortfolioCashFlowData(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);

			Date[] portoflioDateRange = PaymentUtil.getPortofolioDateRanges(controller, portfolioId);
			
			Calendar start = Calendar.getInstance();
			start.setTime(portoflioDateRange[0]);
			Calendar end = Calendar.getInstance();
			end.setTime(portoflioDateRange[1]);

			List<Project> projects = portfolio.getProjects();
			for (Project currentProject : projects) {
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				Date projectEndDate = projectDates[1];
				
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
					DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
					entity.setPortfolioId(portfolioId);
					entity.setProjectId(currentProject.getProjectId());
					entity.setDay(date);
					boolean includeOverhead = false;
					if (   (date.equals(projectStartDate) || date.after(projectStartDate)) 
						&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
						includeOverhead = true;
					}
					entity.setCashout(PaymentUtil.getDateTasksCashout(currentProject, date , includeOverhead));
					entity.setFinanceCost(PaymentUtil.getDateFinanceCost(currentProject, date, results));
					entity.setPayments(PaymentUtil.getProjectPaymentst(currentProject, date));
					entity.setBalance(PaymentUtil.getBalance(date, entity, results));
					entity.setNetBalance( entity.getBalance() + entity.getFinanceCost());
					
					
					results.put( PaymentUtil.getDatesOnly(date) + "," + currentProject.getProjectId(), entity);
				}
			}

			
			
			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0007" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);
		}
		
	}
	
	
	public ServerResponse	getProjectCashFlowData(HttpSession session, int projectId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			
			Project project = controller.find(Project.class , projectId);

			Date[] portoflioDateRange = PaymentUtil.getPortofolioDateRanges(controller, project.getPortfolio().getPortfolioId());
				
			Calendar start = Calendar.getInstance();
			start.setTime(portoflioDateRange[0]);
			Calendar end = Calendar.getInstance();
			end.setTime(portoflioDateRange[1]);

			Date[] projectDates = PaymentUtil.getProjectExtendedDateRanges(controller, project.getProjectId());
			Date projectStartDate = projectDates[0];
		
			
			Date[] tasksSpan = PaymentUtil.getProjectDateRanges(controller, projectId);
		
			Date lastTaskEndDate = tasksSpan[1];
			
			for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
				DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
				entity.setPortfolioId(project.getPortfolio().getPortfolioId());
				entity.setProjectId(project.getProjectId());
				entity.setDay(date);
				boolean includeOverhead = false;
				if (   (date.equals(projectStartDate) || date.after(projectStartDate)) 
					&& (date.before(lastTaskEndDate) || date.equals(lastTaskEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date , includeOverhead  ));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				entity.setPayments(PaymentUtil.getProjectPaymentst(project, date));
				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());
				
				results.put( PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			
			
			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);
		}
		
	}
	
	
	public ServerResponse getCashoutCurrentPeriod (HttpSession session, int portfolioId, Date from, Date to ){
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			List<PeriodCashout> periodCashouts = new ArrayList<PeriodCashout>();
			int numberOfDays = PaymentUtil.daysBetween(from, to) ;
			int effictiveNumberOfDays;
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
			List<Project> projects = portfolio.getProjects();
			Calendar calendar = Calendar.getInstance();
			List<ProjectTask> tasks = null;
			Date taskEndDate ; 
			Date taskDate ;
			
			 Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;
			
			
			for (Project currentProject : projects) {
				// reset for each project
				double taskCostCounter = 0;
				double cashoutCounter = 0;
				calendar.setTime(from);
				calendar.add(Calendar.DATE, -1);
				Date[] projectDates = PaymentUtil.getProjectExtendedDateRanges(controller, currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				if (projectStartDate != null) {  // Null means there are no tasks for that project
					cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, currentProject.getProjectId(), projectStartDate , calendar.getTime());
					tasks = currentProject.getProjectTasks();
					
	
					// 1. Task already started. schedule start date > start
					// 2. task end date (schedule start date + calendar duration) > end
					for (ProjectTask currentTask : tasks) {
						
						
						taskDate = PaymentUtil.getTaskDate(currentTask);
						calendar.setTime(taskDate);
						calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
						taskEndDate = calendar.getTime();
							
						if(taskDate.before(from) && ( taskEndDate.after(from) || taskEndDate.equals(from) )){
							
							int taskDaysAfterPeriodStart = PaymentUtil.daysBetween(from, taskEndDate) + 1 ; // daysBetween exclude last day. It should be included here 
							effictiveNumberOfDays = taskDaysAfterPeriodStart;
							if(numberOfDays < taskDaysAfterPeriodStart) {
								effictiveNumberOfDays = numberOfDays;
							}
							Calendar endEffectiveDate = Calendar.getInstance();
							endEffectiveDate.setTime(from);
							endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays) ;
							int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, from, endEffectiveDate.getTime()); // What if taskEndDate > to?
							// task cost 
							effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
							double taskCost = currentTask.getUniformDailyCost().doubleValue() * effictiveNumberOfDays;
							
							taskCostCounter += taskCost;
							
							cashoutCounter += taskCost;
							
							
						}
						
					}
					projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
					
					int overheadDays = 0;
					if (!to.equals(from)) {
						overheadDays = PaymentUtil.daysBetween(PaymentUtil.maxDate(from, projectStartDate) , to);
					}
					double overhead =  currentProject.getOverheadPerDay().doubleValue() * overheadDays;
					cashoutCounter += overhead;
				
					Calendar prevDay = Calendar.getInstance();
					prevDay.setTime(from);
					DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from , cashFlowInfo, currentProject.getProjectId());
					
					double firstDayBalance = previousDay == null? 0 : previousDay.getBalance() ;
					double firstDayBalanceToReturn = firstDayBalance;
					double projectPayment = PaymentUtil.getProjectPaymentst(currentProject, from);
						
				
					
					
					periodCashouts.add(new PeriodCashout(currentProject.getProjectId(), currentProject.getProjectCode(), taskCostCounter, overhead, cashoutCounter, firstDayBalanceToReturn , projectPayment ));
				}
			}
			
			return new ServerResponse("0", "Success", periodCashouts);			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);		
			}
		}
	
	
	public synchronized ServerResponse getExtraCachCurrentPeriod(HttpSession session, int portfolioId, Date from, Date to ){ 
		EntityController<ProjectPayment> paymentController = new EntityController<>(session.getServletContext());
		try {
			@SuppressWarnings("unchecked")
			List<Double> total = (List<Double>) paymentController.nativeQuery("Select coalesce(sum(coalesce(payment_amount , 0 )) , 0 ) from project_payment where project_id in ( select project_id from project where portfolio_id = ? ) and payment_date >= ? and payment_date < ?" , portfolioId , from , to);
			return new ServerResponse("0" , "Succes" , total.get(0));
			
		} catch (EntityControllerException e) {
			
			e.printStackTrace();
			return new ServerResponse("PORT0011" , String.format("Error getting extra cash for protfolio %d : %s" , portfolioId,  e.getMessage() ), e);		
		}
		
		
	}
	
	public synchronized ServerResponse getExtraCachNextPeriod(HttpSession session, int portfolioId, Date from, Date to , String paymentDetailsJson ){ 
		Map<Integer, ProjectPaymentDetail> paymentDetails = PaymentUtil.getPaymentDetailsMap(paymentDetailsJson);
		
		EntityController<Portfolio> porController = new EntityController<>(session.getServletContext());
		try {
			Portfolio portfolio =  porController.find(Portfolio.class, portfolioId);
			if (portfolio == null) {
				return new ServerResponse("0" , "Succes" , 0);
				
			} else {
				double expectedPayment = PaymentUtil.getPortfolioPayment(from, to,paymentDetails, portfolio , false);
				return new ServerResponse("0" , "Succes" , expectedPayment);
			}
			
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0011" , String.format("Error getting extra cash for protfolio %d : %s" , portfolioId,  e.getMessage() ), e);		
		}
		
		
	}


	
	public synchronized ServerResponse getExtraCachNextPeriodByProject(HttpSession session, int projectId, Date from, Date to , double advanceRepayment , double retainPercent , double extraPayment , Date paymentFrom, Date paymentTo ){ 
		EntityController<Project> projController = new EntityController<>(session.getServletContext());
		try {
			Project project =  projController.find(Project.class, projectId);
			if (project == null) {
				return new ServerResponse("0" , "Succes" , 0);
				
			} else {
				double expectedPayment = PaymentUtil.getProjectPayment(from, to,
						advanceRepayment, retainPercent, extraPayment,
						paymentFrom , paymentTo, project);
				return new ServerResponse("0" , "Succes" , expectedPayment);
			}
			
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0014" , String.format("Error getting extra cash for project %d : %s" , projectId,  e.getMessage() ), e);		
		}
		
		
	}
	
	public synchronized ServerResponse getExtraCachNextPeriodByProjectNoAdjustments(HttpSession session, int projectId, Date from, Date to , Date paymentFrom, Date paymentTo ){ 
		EntityController<Project> projController = new EntityController<>(session.getServletContext());
		try {
			Project project =  projController.find(Project.class, projectId);
			if (project == null) {
				return new ServerResponse("0" , "Succes" , 0);
				
			} else {
				double expectedPayment = PaymentUtil.getProjectPaymentNoAdjustments(from, to,
						paymentFrom , paymentTo, project);
				return new ServerResponse("0" , "Succes" , expectedPayment);
			}
			
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0014" , String.format("Error getting extra cash for project %d : %s" , projectId,  e.getMessage() ), e);		
		}
		
		
	}
	

	
	public synchronized ServerResponse getProjectsByPaymentDate(HttpSession session, int portfolioId , Date from ) {
		EntityController<Portfolio> porController = new EntityController<>(session.getServletContext());
		try {
			Portfolio portfolio =  porController.find(Portfolio.class, portfolioId);
			if (portfolio == null) {
				return new ServerResponse("0" , "Succes" , null);
				
			} else {
				List<Project> projects = new ArrayList<Project>();
				for (Project project : portfolio.getProjects()) {
					for (ProjectPayment payment: project.getProjectPayments()) {
						if (payment.getPaymentDate().equals(from)) {
							projects.add(project);
						}
							
					}
				}
				return new ServerResponse("0" , "Success" , projects);
			}
			
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0012" , String.format("Error getting projects by payment date for protfolio %d : %s" , portfolioId,  e.getMessage() ), e);		
		}
		
		
				
			
		
	}
	
	
	
}
