/**
 * 
 */
package com.softpoint.optima.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.codec.binary.Base64;
import org.eclipse.persistence.internal.sessions.DirectCollectionChangeRecord.NULL;

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

	
	private static final int MAX_CASHFLOW_CELLS = 1095; //Projects range 3years maximum



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
	
	public ServerResponse getSolution(HttpSession session , int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = null;
		try {
			portfolio = controller.find(Portfolio.class, portfolioId);
			//verify it is already solved
			List<Project> projects = portfolio.getProjects();
			for (Project project : projects) {
				List<ProjectTask> tasks = project.getProjectTasks();
				for (ProjectTask task:tasks) {
					if (task.getScheduledStartDate()==null) {
						//if at least one task doesn't have scheduled start date then it is not really solved yet
						return new ServerResponse("0", "Success", "");
					}
				}
			}
			//if all tasks have scheduled start date it means that it is already been solved before
			SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy");

			Date[] portofolioDateRanges;
			portofolioDateRanges = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
			Date startDate = portofolioDateRanges[0];
			ProjectController projectController = new ProjectController();
			SchedulePeriod currentPeriod = projectController.getCurrentPeriodBoundriesNew(session, startDate, portfolioId);
			
			
			StringBuilder sb = new StringBuilder();
			sb.append("<table class=\"solutionTable\">");
			int periodIndex = 0;
			int totalTasks = 0;
			int reportedTasks = 0;
			for(Project project : projects) {
				totalTasks += project.getProjectTasks().size();
			}
			while (reportedTasks<totalTasks) {
				Boolean periodHeaderAdded = false;
				for(Project project : projects)
				{
					Boolean projectHeaderAdded = false;
					List<ProjectPayment> payments = project.getProjectPayments();
					if (periodIndex<payments.size()-1) {
						ProjectPayment currentPayment = payments.get(periodIndex);
						ProjectPayment nextPayment = payments.get(periodIndex+1);
						
						List<ProjectTask> tasks = project.getProjectTasks();
						for (ProjectTask task : tasks) {
							Boolean shouldIncrement = inBetweenDates(task.getScheduledStartDate(),currentPayment.getPaymentDate(),nextPayment.getPaymentDate());
							Boolean shouldPrint = shouldIncrement || inBetweenDates(task.getTentativeStartDate(),currentPayment.getPaymentDate(),nextPayment.getPaymentDate());
							
							//only on tasks that will start in current period or that initially should have started this period
							if (shouldPrint) {
									if (!periodHeaderAdded) {
										sb.append("<tr><td  colspan=\"6\"><h3>From: ").append(format.format(currentPayment.getPaymentDate())).append(" TO: ").append(format.format(nextPayment.getPaymentDate())).append("</h3></td></tr>");
										periodHeaderAdded = true;
									}
									if (!projectHeaderAdded) {
										sb.append("<tr><td  width=\"30px\"></td><td  colspan=\"5\"><b>").append(project.getProjectCode()).append("</b></td></tr>"); 
										projectHeaderAdded = true;
									}
									sb.append("<tr><td  width=\"30px\"></td><td  width=\"30px\"></td><td  width=\"10px\">");
									if (task.getTentativeStartDate().compareTo(task.getScheduledStartDate())==0) {
										sb.append("<div style=\"width:16px;height:16px\" class=\"notShiftedTaskLogo\"></div>");
									} else if (!shouldIncrement) {
										sb.append("<div style=\"width:16px;height:16px\" class=\"shiftedTaskOutLogo\"></div>");
									} else {
										sb.append("<div style=\"width:16px;height:16px\" class=\"shiftedTaskInLogo\"></div>");
									}
									sb.append("</td><td>").append(task.getTaskDescription()).append("</td><td>").append(format.format(task.getTentativeStartDate())).append("</td><td>").append(format.format(task.getScheduledStartDate())).append("</td></tr>\r");
									if (shouldIncrement) {
										reportedTasks++;
									}
							}
						}
						projectHeaderAdded = false;
						
					}
				}		
				periodIndex ++;
				
			} 
			sb.append("</table>");
/*			
			for(Project project : projects)
			{
				List<ProjectPayment> payments = project.getProjectPayments();
				List<PeriodCashout> periodCashouts = new ArrayList<PeriodCashout>();

				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				if(projectDates[1].getTime() > currentPeriod.getCurrent().getDateFrom().getTime())
				{
					Calendar end = Calendar.getInstance();
					end.setTime(portofolioDateRanges[1]);
					end.add(Calendar.DATE, project.getPaymentRequestPeriod());
					portofolioDateRanges[1] = end.getTime();
				}
			}
*/			
			return new ServerResponse("0", "Success", sb.toString());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0002" , String.format("Error updating Portfolio %s: %s" , portfolio!=null?portfolio.getPortfolioName():"", e.getMessage() ), e);
		}
	}
	
	private static Boolean inBetweenDates(Date d,Date start, Date end) {
		return !d.before(start) && d.before(end);
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
	
	public Date[] getPortfolioDateRangeWithLastPayment(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Date[] portofolioDateRanges;
		try {
			portofolioDateRanges = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
			Date endDate = portofolioDateRanges[1];
			ProjectController projectController = new ProjectController();
			SchedulePeriod currentPeriod = projectController.getCurrentPeriodBoundriesNew(session, endDate, portfolioId);
			portofolioDateRanges[1] = currentPeriod.getCurrent().getDateTo();
			
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
			List<Project> projects = portfolio.getProjects();
			for(Project project : projects)
			{
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				if(projectDates[1].getTime() > currentPeriod.getCurrent().getDateFrom().getTime())
				{
					Calendar end = Calendar.getInstance();
					end.setTime(portofolioDateRanges[1]);
					end.add(Calendar.DATE, project.getPaymentRequestPeriod());
					portofolioDateRanges[1] = end.getTime();
				}
			}
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return null;
		}
		return portofolioDateRanges;
	}
		
	public ServerResponse getPortfolioDateRangeNew(HttpSession session, int portfolioId) throws OptimaException {
		try {
			Date[] portofolioDateRanges = getPortfolioDateRangeWithLastPayment(session, portfolioId);
			return new ServerResponse("0", "Success", portofolioDateRanges);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0006" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);
		}
	}
	
	

	public ServerResponse	getPortfolioCashFlowDataNew(HttpSession session, int portfolioId) throws OptimaException {
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
					entity.setPayments(PaymentUtil.getProjectPaymentstNew(session, currentProject, date));
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
	
	
	
	public ServerResponse	getProjectCashFlowDataNew(HttpSession session, int projectId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			
			Project project = controller.find(Project.class , projectId);
			
			int portfolioId = project.getPortfolio().getPortfolioId();
			Date[] portoflioDateRange = getPortfolioDateRangeWithLastPayment(session, portfolioId);
			
			Calendar start = Calendar.getInstance();
			start.setTime(portoflioDateRange[0]);
			Calendar end = Calendar.getInstance();
			end.setTime(portoflioDateRange[1]);

			Date[] projectDates = PaymentUtil.getProjectExtendedDateRanges(controller, project.getProjectId());
			Date projectStartDate = projectDates[0];
		
			
			Date[] tasksSpan = PaymentUtil.getProjectDateRanges(controller, projectId);
		
			Date lastTaskEndDate = tasksSpan[1];
			double totalRetained = 0.0;
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
				double payment = PaymentUtil.getProjectPaymentstNew(session, project, date);
				double originalPayment = payment / (1.0-project.getRetainedPercentage().doubleValue()-project.getAdvancedPaymentPercentage().doubleValue());

				
				if(!start.equals(end))
				{
					double retainedAmount = originalPayment * project.getRetainedPercentage().doubleValue();
					double advancedAmount = originalPayment * project.getAdvancedPaymentPercentage().doubleValue();
					totalRetained = totalRetained + retainedAmount;
					entity.setPayments(payment);
				}else
					entity.setPayments(payment + totalRetained);
								
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
	
	public ServerResponse getCashoutPreviousPeriodNew (HttpSession session, int portfolioId, Date from){
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
			double openBalance = PaymentUtil.getPortfolioOpenBalanceNew(session, portfolio , from);
			
			return new ServerResponse("0", "Success", openBalance);			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);		
		}
	}
	
	public ServerResponse getCashoutPreviousPeriod (HttpSession session, int portfolioId, Date from){
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
			double openBalance = PaymentUtil.getPortfolioOpenBalance(session, portfolio , from);
			
			return new ServerResponse("0", "Success", openBalance);			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);		
		}
	}
	
	public ServerResponse getCashoutCurrentPeriodNew (HttpSession session, int portfolioId, Date from, Date to ){
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
					cashFlowInfo = PaymentUtil.getProjectCashFlowDataNew(session, currentProject.getProjectId(), projectStartDate , calendar.getTime());
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
					double projectPayment = PaymentUtil.getProjectPaymentstNew(session, currentProject, from);
						
				
					
					
					periodCashouts.add(new PeriodCashout(currentProject.getProjectId(), currentProject.getProjectCode(), taskCostCounter, overhead, cashoutCounter, firstDayBalanceToReturn , projectPayment ));
				}
			}
			
			return new ServerResponse("0", "Success", periodCashouts);			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009" , String.format("Error loading portfolios : %s" , e.getMessage() ), e);		
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
	
	private String getHorizontalIndexCellAt(int index)
	{
		String horizontalIndex ="";
		int x;
		char c;
	    while (index > 0) {
	        x = index % 26;
	        index = index / 26;
	        c = (char) ('A' + (x-1));
	        horizontalIndex = c + horizontalIndex;
	    }
		return horizontalIndex;
	}
	public ServerResponse downloadCashflowGraph (HttpSession session, int portfolioId) {
			EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
			try {
				
				//Load graph template 
				Workbook workbook = new XSSFWorkbook(OPCPackage.open(new FileInputStream(session.getServletContext().getRealPath( "/WEB-INF" + File.separator + "/files" + File.separator + "template.xlsx"))));
				CreationHelper createHelper = workbook.getCreationHelper();
				
				
				Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
				Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
				Date[] portoflioDateRange = PaymentUtil.getPortofolioDateRanges(controller, portfolioId);
				List<Project> projects = portfolio.getProjects();
				
				Sheet sheet = workbook.getSheetAt(0);
				Row portfolioNameRow = sheet.getRow(0);
				Cell portfolioNameCell = portfolioNameRow.getCell(4);
				portfolioNameCell.setCellValue(portfolio.getPortfolioName());
				
				Map<Date, Double> totalCashOut = new HashMap();
				Map<Date, Double> totalFinanceCost = new HashMap();
				Map<Date, Double> totalBalanceSolution = new HashMap();
				Map<Date, Double> totalPayments = new HashMap();
				Map<Date, Double> totalBalanceInitial = new HashMap();
				Map<Date, Double> totalFinance = new HashMap();
				
				int proj = 1;
				for (Project currentProject : projects) {
				
					sheet = workbook.getSheetAt(proj);
					Row projectNameRow = sheet.getRow(0);
					Cell projectNameCell = projectNameRow.getCell(4);
					projectNameCell.setCellValue(currentProject.getProjectCode());
					
					
					workbook.setSheetName(workbook.getSheetIndex(sheet), currentProject.getProjectCode());
					CellStyle cellStyle = workbook.createCellStyle();
					cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
					for(int row=2; row < 9 ;row++)
						for(int col=2; col < MAX_CASHFLOW_CELLS ;col++)
						{
							Cell currentCell = sheet.getRow(row).getCell(col);
							if(currentCell != null)
								sheet.getRow(row).getCell(col).setCellValue("");
						}
							
					Row dayRow =sheet.getRow(2);
					Row cashOutRow =sheet.getRow(3);
					Row financeCostRow =sheet.getRow(4);
					Row balanceSolutionRow =sheet.getRow(5);
					Row paymentsRow =sheet.getRow(6);
					Row financeRow =sheet.getRow(7);
					Row balanceInitialRow =sheet.getRow(8);
					
					
					
					Calendar start = Calendar.getInstance();
					start.setTime(portoflioDateRange[0]);
					Calendar end = Calendar.getInstance();
					end.setTime(portoflioDateRange[1]);
					
					Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
					Date projectStartDate = projectDates[0];
					Date projectEndDate = projectDates[1];
					
					int index = 2;
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
						
						
						Cell dayCell = dayRow.getCell(index);
						Cell cashOutCell = cashOutRow.getCell(index);
						Cell financeCostCell = financeCostRow.getCell(index);
						Cell balanceSolutionCell = balanceSolutionRow.getCell(index);
						Cell paymentsCell = paymentsRow.getCell(index);
						Cell financeCell = financeRow.getCell(index);
						Cell balanceInitialCell = balanceInitialRow.getCell(index);
						
						if(dayCell == null)
							dayCell = dayRow.createCell(index);
						if(cashOutCell == null)
							cashOutCell = cashOutRow.createCell(index);
						if(financeCostCell == null)
							financeCostCell = financeCostRow.createCell(index);
						if(balanceSolutionCell == null)
							balanceSolutionCell = balanceSolutionRow.createCell(index);
						if(paymentsCell == null)
							paymentsCell = paymentsRow.createCell(index);
						if(financeCell == null)
							financeCell = financeRow.createCell(index);
						if(balanceInitialCell == null)
							balanceInitialCell = balanceInitialRow.createCell(index);
						
						
						dayCell.setCellStyle(cellStyle);
						dayCell.setCellValue(date);
						
						
						double cashOutValue = PaymentUtil.getDateTasksCashout(currentProject, date , includeOverhead);
						cashOutCell.setCellValue(cashOutValue);
						entity.setCashout(cashOutValue);
						if(totalCashOut.get(date)==null)
							totalCashOut.put(date, cashOutValue);
						else
							totalCashOut.put(date, totalCashOut.get(date) + cashOutValue);
									
						
						double financeCostValue = PaymentUtil.getDateFinanceCost(currentProject, date, results);
						financeCostCell.setCellValue(financeCostValue);
						entity.setFinanceCost(financeCostValue);
						if(totalFinanceCost.get(date)==null)
							totalFinanceCost.put(date, financeCostValue);
						else
							totalFinanceCost.put(date, totalFinanceCost.get(date) + financeCostValue);
						
						
						double paymentsValue = PaymentUtil.getProjectPaymentst(currentProject, date);
						paymentsCell.setCellValue(paymentsValue);
						entity.setPayments(paymentsValue);
						if(totalPayments.get(date)==null)
							totalPayments.put(date, paymentsValue);
						else
							totalPayments.put(date, totalPayments.get(date) + paymentsValue);
						
						double balanceSolutionValue = PaymentUtil.getBalance(date, entity, results);
						balanceSolutionCell.setCellValue(balanceSolutionValue);
						entity.setBalance(balanceSolutionValue);
						if(totalBalanceSolution.get(date)==null)
							totalBalanceSolution.put(date, balanceSolutionValue);
						else
							totalBalanceSolution.put(date, totalBalanceSolution.get(date) + balanceSolutionValue);
						
						double finance = PaymentUtil.getFinanceLimit(session , portfolioId , date);
						financeCell.setCellValue(-1 * finance);
						totalFinance.put(date, -1 * finance);
						
								
						balanceInitialCell.setCellValue(entity.getBalance() + entity.getFinanceCost());
						if(totalBalanceInitial.get(date)==null)
							totalBalanceInitial.put(date, entity.getBalance() + entity.getFinanceCost());
						else
							totalBalanceInitial.put(date, totalBalanceInitial.get(date) + entity.getBalance() + entity.getFinanceCost());
						
						
						results.put( PaymentUtil.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(), entity);
						
						index++;
					}
					
					String horizontalIndex = getHorizontalIndexCellAt(index);
					String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index-1);
					
					
					Name rangeDays = workbook.getName("Days_Proj"+proj);
					Name rangeDaysX = workbook.getName("Days_Proj"+proj+"X");
					String reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndex + "$3";
					String referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndexBeforeLast + "$3";
					rangeDays.setRefersToFormula(reference);
					rangeDaysX.setRefersToFormula(referenceX);
					
					
					
					Name rangeBalanceSol = workbook.getName("Balance_Solution_Proj"+proj);
					Name rangeBalanceSolX = workbook.getName("Balance_Solution_Proj"+proj+"X");
					reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$6:$" + horizontalIndex + "$6";
					referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$6:$" + horizontalIndex + "$6";
					rangeBalanceSol.setRefersToFormula(reference);
					rangeBalanceSolX.setRefersToFormula(referenceX);
					

					
					Name rangeFinance = workbook.getName("Finance_Proj"+proj);
					Name rangeFinanceX = workbook.getName("Finance_Proj"+proj+"X");
					reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$8:$" + horizontalIndex + "$8";
					referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$8:$" + horizontalIndex + "$8";
					rangeFinance.setRefersToFormula(reference);
					rangeFinanceX.setRefersToFormula(referenceX);
					
					Name rangeBalanceInit = workbook.getName("Initial_Balance_Proj"+proj);
					Name rangeBalanceInitX = workbook.getName("Initial_Balance_Proj"+proj+"X");
					reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$9:$" + horizontalIndex + "$9";
					referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$9:$" + horizontalIndex + "$9";
					rangeBalanceInit.setRefersToFormula(reference);
					rangeBalanceInitX.setRefersToFormula(referenceX);
					
					proj++;
				}
				
				//Portfolio
				sheet = workbook.getSheetAt(0);
				CellStyle cellStyle = workbook.createCellStyle();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
				for(int row=2; row < 9 ;row++)
					for(int col=2; col < MAX_CASHFLOW_CELLS ;col++)
					{
						Cell currentCell = sheet.getRow(row).getCell(col);
						if(currentCell != null)
							sheet.getRow(row).getCell(col).setCellValue("");
					}
							
				Row dayRow =sheet.getRow(2);
				Row cashOutRow =sheet.getRow(3);
				Row financeCostRow =sheet.getRow(4);
				Row balanceSolutionRow =sheet.getRow(5);
				Row paymentsRow =sheet.getRow(6);
				Row financeRow =sheet.getRow(7);
				Row balanceInitialRow =sheet.getRow(8);
					
					
					
				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);
				Calendar end = Calendar.getInstance();
				end.setTime(portoflioDateRange[1]);
				
				int index = 2;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
						Cell dayCell = dayRow.getCell(index);
						Cell cashOutCell = cashOutRow.getCell(index);
						Cell financeCostCell = financeCostRow.getCell(index);
						Cell balanceSolutionCell = balanceSolutionRow.getCell(index);
						Cell paymentsCell = paymentsRow.getCell(index);
						Cell financeCell = financeRow.getCell(index);
						Cell balanceInitialCell = balanceInitialRow.getCell(index);
						
						if(dayCell == null)
							dayCell = dayRow.createCell(index);
						if(cashOutCell == null)
							cashOutCell = cashOutRow.createCell(index);
						if(financeCostCell == null)
							financeCostCell = financeCostRow.createCell(index);
						if(balanceSolutionCell == null)
							balanceSolutionCell = balanceSolutionRow.createCell(index);
						if(paymentsCell == null)
							paymentsCell = paymentsRow.createCell(index);
						if(financeCell == null)
							financeCell = financeRow.createCell(index);
						if(balanceInitialCell == null)
							balanceInitialCell = balanceInitialRow.createCell(index);
						
						
						dayCell.setCellStyle(cellStyle);
						dayCell.setCellValue(date);
						
						cashOutCell.setCellValue(totalCashOut.get(date));
						financeCostCell.setCellValue(totalFinanceCost.get(date));
						paymentsCell.setCellValue(totalPayments.get(date));
						balanceSolutionCell.setCellValue(totalBalanceSolution.get(date));
						financeCell.setCellValue(totalFinance.get(date));
						balanceInitialCell.setCellValue(totalBalanceInitial.get(date));
						index++;
				}
					
				String horizontalIndex = getHorizontalIndexCellAt(index);
				String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index-1);
				
				Name rangeDays = workbook.getName("Days_Port");
				Name rangeDaysX = workbook.getName("Days_PortX");
				String reference = "Portfolio" + "!$C$3:$" + horizontalIndex + "$3";
				String referenceX = "Portfolio" + "!$C$3:$" + horizontalIndexBeforeLast + "$3";
				rangeDays.setRefersToFormula(reference);
				rangeDaysX.setRefersToFormula(referenceX);
				
			
				Name rangeBalanceSol = workbook.getName("Balance_Solution_Port");
				Name rangeBalanceSolX = workbook.getName("Balance_Solution_PortX");
				reference = "Portfolio" + "!$C$6:$" + horizontalIndex + "$6";
				referenceX = "Portfolio" + "!$D$6:$" + horizontalIndex + "$6";
				rangeBalanceSol.setRefersToFormula(reference);
				rangeBalanceSolX.setRefersToFormula(referenceX);
				
		
				Name rangeFinance = workbook.getName("Finance_Port");
				Name rangeFinanceX = workbook.getName("Finance_PortX");
				reference = "Portfolio" + "!$C$8:$" + horizontalIndex + "$8";
				referenceX = "Portfolio" + "!$D$8:$" + horizontalIndex + "$8";
				rangeFinance.setRefersToFormula(reference);
				rangeFinanceX.setRefersToFormula(referenceX);
				
				Name rangeBalanceInit = workbook.getName("Initial_Balance_Port");
				Name rangeBalanceInitX = workbook.getName("Initial_Balance_PortX");
				reference = "Portfolio" + "!$C$9:$" + horizontalIndex + "$9";
				referenceX = "Portfolio" + "!$D$9:$" + horizontalIndex + "$9";
				rangeBalanceInit.setRefersToFormula(reference);
				rangeBalanceInitX.setRefersToFormula(referenceX);
					
				//Remove extra projects from template
				for (int p=10; p>=proj; p--) {
					if(workbook.getSheetAt(p)!=null)
						workbook.removeSheetAt(p);
				}
				
				String fileName = "UpdatedGraph.xlsx";
				FileOutputStream f = new FileOutputStream(fileName);
				workbook.write(f);
				f.close();
		        
		        File my_file = new File(fileName);
		        byte[] bytes = loadFile(my_file);
				byte[] encoded = Base64.encodeBase64(bytes);
				String encodedString = new String(encoded);
				return new ServerResponse("0", "Success", encodedString);
				
			} catch (Exception e) {
				e.printStackTrace();
				return new ServerResponse("PROJ0003" , String.format("Error looking up portfolio %d: %s" , portfolioId , e.getMessage() ), e);
			}
		}
	
	public ServerResponse downloadCashflowGraphNew (HttpSession session, int portfolioId) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			
			//Load graph template 
			Workbook workbook = new XSSFWorkbook(OPCPackage.open(new FileInputStream(session.getServletContext().getRealPath( "/WEB-INF" + File.separator + "/files" + File.separator + "template.xlsx"))));
			CreationHelper createHelper = workbook.getCreationHelper();
			
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			Portfolio portfolio = controller.find(Portfolio.class , portfolioId);
			Date[] portoflioDateRange = getPortfolioDateRangeWithLastPayment(session, portfolioId);
			List<Project> projects = portfolio.getProjects();
			
			Sheet sheet = workbook.getSheetAt(0);
			Row portfolioNameRow = sheet.getRow(0);
			Cell portfolioNameCell = portfolioNameRow.getCell(4);
			portfolioNameCell.setCellValue(portfolio.getPortfolioName());
			
			Map<Date, Double> totalCashOut = new HashMap();
			Map<Date, Double> totalFinanceCost = new HashMap();
			Map<Date, Double> totalBalanceSolution = new HashMap();
			Map<Date, Double> totalPayments = new HashMap();
			Map<Date, Double> totalBalanceInitial = new HashMap();
			Map<Date, Double> totalFinance = new HashMap();
			
			int proj = 1;
			for (Project currentProject : projects) {
			
				sheet = workbook.getSheetAt(proj);
				Row projectNameRow = sheet.getRow(0);
				Cell projectNameCell = projectNameRow.getCell(4);
				projectNameCell.setCellValue(currentProject.getProjectCode());
				
				
				workbook.setSheetName(workbook.getSheetIndex(sheet), currentProject.getProjectCode());
				CellStyle cellStyle = workbook.createCellStyle();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
				for(int row=2; row < 9 ;row++)
					for(int col=2; col < MAX_CASHFLOW_CELLS ;col++)
					{
						Cell currentCell = sheet.getRow(row).getCell(col);
						if(currentCell != null)
							sheet.getRow(row).getCell(col).setCellValue("");
					}
						
				Row dayRow =sheet.getRow(2);
				Row cashOutRow =sheet.getRow(3);
				Row financeCostRow =sheet.getRow(4);
				Row balanceSolutionRow =sheet.getRow(5);
				Row paymentsRow =sheet.getRow(6);
				Row financeRow =sheet.getRow(7);
				Row balanceInitialRow =sheet.getRow(8);
				
				
				
				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);
				Calendar end = Calendar.getInstance();
				end.setTime(portoflioDateRange[1]);
				
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				Date projectEndDate = projectDates[1];
				
				int index = 2;
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
					
					
					Cell dayCell = dayRow.getCell(index);
					Cell cashOutCell = cashOutRow.getCell(index);
					Cell financeCostCell = financeCostRow.getCell(index);
					Cell balanceSolutionCell = balanceSolutionRow.getCell(index);
					Cell paymentsCell = paymentsRow.getCell(index);
					Cell financeCell = financeRow.getCell(index);
					Cell balanceInitialCell = balanceInitialRow.getCell(index);
					
					if(dayCell == null)
						dayCell = dayRow.createCell(index);
					if(cashOutCell == null)
						cashOutCell = cashOutRow.createCell(index);
					if(financeCostCell == null)
						financeCostCell = financeCostRow.createCell(index);
					if(balanceSolutionCell == null)
						balanceSolutionCell = balanceSolutionRow.createCell(index);
					if(paymentsCell == null)
						paymentsCell = paymentsRow.createCell(index);
					if(financeCell == null)
						financeCell = financeRow.createCell(index);
					if(balanceInitialCell == null)
						balanceInitialCell = balanceInitialRow.createCell(index);
					
					
					dayCell.setCellStyle(cellStyle);
					dayCell.setCellValue(date);
					
					
					double cashOutValue = PaymentUtil.getDateTasksCashout(currentProject, date , includeOverhead);
					cashOutCell.setCellValue(cashOutValue);
					entity.setCashout(cashOutValue);
					if(totalCashOut.get(date)==null)
						totalCashOut.put(date, cashOutValue);
					else
						totalCashOut.put(date, totalCashOut.get(date) + cashOutValue);
								
					
					double financeCostValue = PaymentUtil.getDateFinanceCost(currentProject, date, results);
					financeCostCell.setCellValue(financeCostValue);
					entity.setFinanceCost(financeCostValue);
					if(totalFinanceCost.get(date)==null)
						totalFinanceCost.put(date, financeCostValue);
					else
						totalFinanceCost.put(date, totalFinanceCost.get(date) + financeCostValue);
					
					
					double paymentsValue = PaymentUtil.getProjectPaymentstNew(session, currentProject, date);
					paymentsCell.setCellValue(paymentsValue);
					entity.setPayments(paymentsValue);
					if(totalPayments.get(date)==null)
						totalPayments.put(date, paymentsValue);
					else
						totalPayments.put(date, totalPayments.get(date) + paymentsValue);
					
					double balanceSolutionValue = PaymentUtil.getBalance(date, entity, results);
					balanceSolutionCell.setCellValue(balanceSolutionValue);
					entity.setBalance(balanceSolutionValue);
					if(totalBalanceSolution.get(date)==null)
						totalBalanceSolution.put(date, balanceSolutionValue);
					else
						totalBalanceSolution.put(date, totalBalanceSolution.get(date) + balanceSolutionValue);
					
					double finance = PaymentUtil.getFinanceLimit(session , portfolioId , date);
					financeCell.setCellValue(-1 * finance);
					totalFinance.put(date, -1 * finance);
					
							
					balanceInitialCell.setCellValue(entity.getBalance() + entity.getFinanceCost());
					if(totalBalanceInitial.get(date)==null)
						totalBalanceInitial.put(date, entity.getBalance() + entity.getFinanceCost());
					else
						totalBalanceInitial.put(date, totalBalanceInitial.get(date) + entity.getBalance() + entity.getFinanceCost());
					
					
					results.put( PaymentUtil.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(), entity);
					
					index++;
				}
				
				String horizontalIndex = getHorizontalIndexCellAt(index);
				String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index-1);
				
				
				Name rangeDays = workbook.getName("Days_Proj"+proj);
				Name rangeDaysX = workbook.getName("Days_Proj"+proj+"X");
				String reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndex + "$3";
				String referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndexBeforeLast + "$3";
				rangeDays.setRefersToFormula(reference);
				rangeDaysX.setRefersToFormula(referenceX);
				
				
				
				Name rangeBalanceSol = workbook.getName("Balance_Solution_Proj"+proj);
				Name rangeBalanceSolX = workbook.getName("Balance_Solution_Proj"+proj+"X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$6:$" + horizontalIndex + "$6";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$6:$" + horizontalIndex + "$6";
				rangeBalanceSol.setRefersToFormula(reference);
				rangeBalanceSolX.setRefersToFormula(referenceX);
				

				
				Name rangeFinance = workbook.getName("Finance_Proj"+proj);
				Name rangeFinanceX = workbook.getName("Finance_Proj"+proj+"X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$8:$" + horizontalIndex + "$8";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$8:$" + horizontalIndex + "$8";
				rangeFinance.setRefersToFormula(reference);
				rangeFinanceX.setRefersToFormula(referenceX);
				
				Name rangeBalanceInit = workbook.getName("Initial_Balance_Proj"+proj);
				Name rangeBalanceInitX = workbook.getName("Initial_Balance_Proj"+proj+"X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$9:$" + horizontalIndex + "$9";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$9:$" + horizontalIndex + "$9";
				rangeBalanceInit.setRefersToFormula(reference);
				rangeBalanceInitX.setRefersToFormula(referenceX);
				
				proj++;
			}
			
			//Portfolio
			sheet = workbook.getSheetAt(0);
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
			for(int row=2; row < 9 ;row++)
				for(int col=2; col < MAX_CASHFLOW_CELLS ;col++)
				{
					Cell currentCell = sheet.getRow(row).getCell(col);
					if(currentCell != null)
						sheet.getRow(row).getCell(col).setCellValue("");
				}
						
			Row dayRow =sheet.getRow(2);
			Row cashOutRow =sheet.getRow(3);
			Row financeCostRow =sheet.getRow(4);
			Row balanceSolutionRow =sheet.getRow(5);
			Row paymentsRow =sheet.getRow(6);
			Row financeRow =sheet.getRow(7);
			Row balanceInitialRow =sheet.getRow(8);
				
				
				
			Calendar start = Calendar.getInstance();
			start.setTime(portoflioDateRange[0]);
			Calendar end = Calendar.getInstance();
			end.setTime(portoflioDateRange[1]);
			
			int index = 2;
			for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
					Cell dayCell = dayRow.getCell(index);
					Cell cashOutCell = cashOutRow.getCell(index);
					Cell financeCostCell = financeCostRow.getCell(index);
					Cell balanceSolutionCell = balanceSolutionRow.getCell(index);
					Cell paymentsCell = paymentsRow.getCell(index);
					Cell financeCell = financeRow.getCell(index);
					Cell balanceInitialCell = balanceInitialRow.getCell(index);
					
					if(dayCell == null)
						dayCell = dayRow.createCell(index);
					if(cashOutCell == null)
						cashOutCell = cashOutRow.createCell(index);
					if(financeCostCell == null)
						financeCostCell = financeCostRow.createCell(index);
					if(balanceSolutionCell == null)
						balanceSolutionCell = balanceSolutionRow.createCell(index);
					if(paymentsCell == null)
						paymentsCell = paymentsRow.createCell(index);
					if(financeCell == null)
						financeCell = financeRow.createCell(index);
					if(balanceInitialCell == null)
						balanceInitialCell = balanceInitialRow.createCell(index);
					
					
					dayCell.setCellStyle(cellStyle);
					dayCell.setCellValue(date);
					
					cashOutCell.setCellValue(totalCashOut.get(date));
					financeCostCell.setCellValue(totalFinanceCost.get(date));
					paymentsCell.setCellValue(totalPayments.get(date));
					balanceSolutionCell.setCellValue(totalBalanceSolution.get(date));
					financeCell.setCellValue(totalFinance.get(date));
					balanceInitialCell.setCellValue(totalBalanceInitial.get(date));
					index++;
			}
				
			String horizontalIndex = getHorizontalIndexCellAt(index);
			String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index-1);
			
			Name rangeDays = workbook.getName("Days_Port");
			Name rangeDaysX = workbook.getName("Days_PortX");
			String reference = "Portfolio" + "!$C$3:$" + horizontalIndex + "$3";
			String referenceX = "Portfolio" + "!$C$3:$" + horizontalIndexBeforeLast + "$3";
			rangeDays.setRefersToFormula(reference);
			rangeDaysX.setRefersToFormula(referenceX);
			
		
			Name rangeBalanceSol = workbook.getName("Balance_Solution_Port");
			Name rangeBalanceSolX = workbook.getName("Balance_Solution_PortX");
			reference = "Portfolio" + "!$C$6:$" + horizontalIndex + "$6";
			referenceX = "Portfolio" + "!$D$6:$" + horizontalIndex + "$6";
			rangeBalanceSol.setRefersToFormula(reference);
			rangeBalanceSolX.setRefersToFormula(referenceX);
			
	
			Name rangeFinance = workbook.getName("Finance_Port");
			Name rangeFinanceX = workbook.getName("Finance_PortX");
			reference = "Portfolio" + "!$C$8:$" + horizontalIndex + "$8";
			referenceX = "Portfolio" + "!$D$8:$" + horizontalIndex + "$8";
			rangeFinance.setRefersToFormula(reference);
			rangeFinanceX.setRefersToFormula(referenceX);
			
			Name rangeBalanceInit = workbook.getName("Initial_Balance_Port");
			Name rangeBalanceInitX = workbook.getName("Initial_Balance_PortX");
			reference = "Portfolio" + "!$C$9:$" + horizontalIndex + "$9";
			referenceX = "Portfolio" + "!$D$9:$" + horizontalIndex + "$9";
			rangeBalanceInit.setRefersToFormula(reference);
			rangeBalanceInitX.setRefersToFormula(referenceX);
				
			//Remove extra projects from template
			for (int p=10; p>=proj; p--) {
				if(workbook.getSheetAt(p)!=null)
					workbook.removeSheetAt(p);
			}
			
			String fileName = "UpdatedGraph.xlsx";
			FileOutputStream f = new FileOutputStream(fileName);
			workbook.write(f);
			f.close();
	        
	        File my_file = new File(fileName);
	        byte[] bytes = loadFile(my_file);
			byte[] encoded = Base64.encodeBase64(bytes);
			String encodedString = new String(encoded);
			return new ServerResponse("0", "Success", encodedString);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error looking up portfolio %d: %s" , portfolioId , e.getMessage() ), e);
		}
	}
	
	private static byte[] loadFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);
 
	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }
	    byte[] bytes = new byte[(int)length];
	    
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }
 
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }
 
	    is.close();
	    return bytes;
	}
	public ServerResponse getPortfolioLeftOverCost (HttpSession session, int portfolioId , Date from, Date to) {
		
		EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			try {
				Portfolio protfolio = portController.find(Portfolio.class, portfolioId);
				double totalPortfolioLeftOverCost = PaymentUtil.getPortfolioLeftOverCost(portController , protfolio, from, to, null);
				return new ServerResponse("0", "Success", totalPortfolioLeftOverCost);
				
			} catch (Exception e) {
				e.printStackTrace();
				return new ServerResponse("PROJ0003" , String.format("Error looking up portfolio %d: %s" , portfolioId , e.getMessage() ), e);
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
