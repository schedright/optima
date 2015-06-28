/**
 * 
 */
package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Column;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.softpoint.optima.JsonRpcInitializer;
import com.softpoint.optima.OptimaException;
import com.softpoint.optima.OptimaLogFactory;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Client;
import com.softpoint.optima.db.LocationInfo;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectPayment;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.WeekendDay;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;
import com.softpoint.optima.struct.Period;
import com.softpoint.optima.struct.ProjectPaymentDetail;
import com.softpoint.optima.struct.SchedulePeriod;
import com.softpoint.optima.struct.SolvedTask;
import com.softpoint.optima.struct.TaskSolution;
import com.softpoint.optima.struct.TaskState;
import com.softpoint.optima.util.PaymentUtil;

/**
 * @author WDARWISH
 *
 */
public class ProjectController {

	/**
	 * 
	 */
	public ProjectController() {
		
	}
	
	
	/**
	 * @param session
	 * @param name
	 * @param description
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse create(HttpSession session , String name , String code , String descritpion, String streetAddress, 
			int cityId , int provideId , int countryId , String postalCode , Date proposedStartDate, Date proposedFinishDate, double interestRate,
			double overheadPerDay , int portfolioId , int clientId , int weekendDaysId, double retainedPercentage, double advancedPaymentPercentage, 
			double advancedPaymentAmount, double delayPenaltyAmount, int collectPaymentPeriod,int paymentRequestPeriod) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		
		try {
			Project project = new Project();
			project.setProjectName(name);
			project.setProjectCode(code);
			project.setProjectDescription(descritpion);
			project.setProjectAddressStreet(streetAddress);
			project.setProjectAddressPostalCode(postalCode);
			EntityController<LocationInfo> locInfoController = new EntityController<LocationInfo>(session.getServletContext());
			LocationInfo city = locInfoController.find(LocationInfo.class, cityId);
			LocationInfo province = locInfoController.find(LocationInfo.class, provideId);
			LocationInfo country = locInfoController.find(LocationInfo.class, countryId);
			project.setCity(city);
			project.setProvince(province);
			project.setCountry(country);
			project.setPropusedStartDate(proposedStartDate);
			project.setProposedFinishDate(proposedFinishDate);
			project.setInterestRate(new BigDecimal(interestRate));
			project.setOverheadPerDay(new BigDecimal(overheadPerDay));
			
			project.setRetainedPercentage(new BigDecimal(retainedPercentage));
			project.setAdvancedPaymentPercentage(new BigDecimal(advancedPaymentPercentage));
			
			project.setDelayPenaltyAmount(new BigDecimal(delayPenaltyAmount));
			project.setAdvancedPaymentAmount(new BigDecimal(advancedPaymentAmount));
			project.setPaymentRequestPeriod(paymentRequestPeriod);
			project.setCollectPaymentPeriod(collectPaymentPeriod);
			
			EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
			project.setPortfolio(portfolio);
			
			EntityController<Client> cliController = new EntityController<Client>(session.getServletContext());
			Client client = cliController.find(Client.class, clientId);
			project.setClient(client);
			
			EntityController<WeekendDay> dayOffController = new EntityController<WeekendDay>(session.getServletContext());
			WeekendDay weekendDay = dayOffController.find(WeekendDay.class, weekendDaysId);
			project.setWeekendDays(weekendDay);
			controller.persist(project);
			return new ServerResponse("0", "Success", project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROG0001" , String.format("Error creating project %s: %s" , name , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session , 
			int key , 
			String name ,
			String code ,  
			String descritpion, 
			String streetAddress, 
			int cityId , 
			int provideId , 
			int countryId , 
			String postalCode , 
			Date proposedStartDate, 
			Date proposedFinishDate, 
			double interestRate ,
			double overheadPerDay , 
			int portfolioId , 
			int clientId ,
			int weekendDaysId,
			double retainedPercentage,
			double advancedPaymentPercentage, 
			double advancedPaymentAmount,
			double delayPenaltyAmount,
			int collectPaymentPeriod,
			int paymentRequestPeriod ) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		Project project = null;
		try {
			project = controller.find(Project.class, key);
			project.setProjectName(name);
			project.setProjectCode(code);
			project.setProjectDescription(descritpion);
			project.setProjectAddressStreet(streetAddress);
			project.setProjectAddressPostalCode(postalCode);
			EntityController<LocationInfo> locInfoController = new EntityController<LocationInfo>(session.getServletContext());
			LocationInfo city = locInfoController.find(LocationInfo.class, cityId);
			LocationInfo province = locInfoController.find(LocationInfo.class, provideId);
			LocationInfo country = locInfoController.find(LocationInfo.class, countryId);
			project.setCity(city);
			project.setProvince(province);
			project.setCountry(country);
			project.setPropusedStartDate(proposedStartDate);
			project.setProposedFinishDate(proposedFinishDate);
			project.setInterestRate(new BigDecimal(interestRate));
			project.setOverheadPerDay(new BigDecimal(overheadPerDay));
			
			project.setRetainedPercentage(new BigDecimal(retainedPercentage));
			project.setAdvancedPaymentPercentage(new BigDecimal(advancedPaymentPercentage));
			
			project.setDelayPenaltyAmount(new BigDecimal(delayPenaltyAmount));
			project.setAdvancedPaymentAmount(new BigDecimal(advancedPaymentAmount));
			project.setPaymentRequestPeriod(paymentRequestPeriod);
			project.setCollectPaymentPeriod(collectPaymentPeriod);
			
			
			EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
			project.setPortfolio(portfolio);
			
			EntityController<Client> cliController = new EntityController<Client>(session.getServletContext());
			Client client = cliController.find(Client.class, clientId);
			project.setClient(client);
			
			EntityController<WeekendDay> dayOffController = new EntityController<WeekendDay>(session.getServletContext());
			WeekendDay weekendDay = dayOffController.find(WeekendDay.class, weekendDaysId);
			project.setWeekendDays(weekendDay);
			controller.merge(project);
			// because we might have changed the weekend or the days off
			// Bug#1 Shifting is not working correctly when changing weekends! -- BassemVic
			TaskController taskController = new TaskController();
			taskController.adjustStartDateBasedOnTaskDependency(session, key , false);
			return new ServerResponse("0", "Success", project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0002" , String.format("Error updating project %s: %s" , project!=null?project.getProjectName():"", e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session , Integer key) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			Project project = controller.find(Project.class , key);
			return new ServerResponse("0", "Success", project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error looking up project %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	
	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session , Integer key) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			controller.remove(Project.class , key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0004" , String.format("Error removing project %d: %s" , key , e.getMessage() ), e);
		}
	}
	
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAll(HttpSession session) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			List<Project> projects = controller.findAll(Project.class);
			return new ServerResponse("0", "Success", projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005" , String.format("Error loading projects : %s" , e.getMessage() ), e);
		}
	}
	

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByPortfolio(HttpSession session , int portfolioId) throws OptimaException {
		try {
				EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portController.find(Portfolio.class, portfolioId);			
			return new ServerResponse("0", "Success", portfolio.getProjects());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0006" , String.format("Error loading projects : %s" , e.getMessage() ), e);
		}
	}
	
	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByClient(HttpSession session , int clientId) throws OptimaException {
		try {
			EntityController<Client> cliController = new EntityController<Client>(session.getServletContext());
			Client client = cliController.find(Client.class, clientId);
			return new ServerResponse("0", "Success", client.getProjects());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0007" , String.format("Error loading projects : %s" , e.getMessage() ), e);
		}
	}
	
	
	
	
	public ServerResponse getOtherProjectsCurrentPeriodCost (HttpSession session, int projectId , Date from, Date to) {
		
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			Project project = controller.find(Project.class , projectId);
			double otherProjectsCurrentPeriodCost = PaymentUtil.getOtherProjectsCurrentPeriodCost(project, from , to);	
			return new ServerResponse("0", "Success", otherProjectsCurrentPeriodCost);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error looking up project %d: %s" , projectId , e.getMessage() ), e);
		}
	}
	


	public ServerResponse getSolutionCurrentPeriodCost (HttpSession session, int projectId , Date from, Date to) {
		
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			
			double taskCostCounterEligibleTasks = 0;
			Date taskEndDate ; 
			Date taskDate ;
			Calendar calendar = Calendar.getInstance();

			int effictiveNumberOfDays;

			
			Project project = controller.find(Project.class , projectId);
			List<ProjectTask> projectTasks = project.getProjectTasks();
			Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
			@SuppressWarnings("unused")
			Date projectEndDate = projectDates[1];
			
			
			for (ProjectTask currentTask : projectTasks) {
				taskDate = PaymentUtil.getTaskDate(currentTask);
				calendar.setTime(taskDate);
				calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
				taskEndDate = calendar.getTime();
				
				if ( (taskDate.after(from) || taskDate.equals(from) ) && taskDate.before(to)) {
					
					effictiveNumberOfDays = Math.min( PaymentUtil.daysBetween(taskDate , taskEndDate)   + 1, PaymentUtil.daysBetween(taskDate, to) );
				
					
					Calendar endEffectiveDate = Calendar.getInstance();
					endEffectiveDate.setTime(taskDate);
					endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays);
				
					int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, taskDate, endEffectiveDate.getTime()); // What if taskEndDate > to?
					
					effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
					double taskCost = currentTask.getUniformDailyCost().doubleValue() * effictiveNumberOfDays;
					taskCostCounterEligibleTasks += taskCost;
					
					
				}	
				
			}
		
			
			
			
			return new ServerResponse("0", "Success", taskCostCounterEligibleTasks);
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error looking up project %d: %s" , projectId , e.getMessage() ), e);
		}
	}
	
	private ReentrantLock solutionLock = new ReentrantLock();
	
	public ServerResponse getSolution(HttpSession session , int projectId, String outputFormat, String projectsPriority) {

		EntityController<Project> projectController = new EntityController<>(session.getServletContext());
		try {
			Project project = projectController.find(Project.class, projectId);
			int portfolioId = project.getPortfolio().getPortfolioId();
			
			SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy");
			
			TaskController taskController = new TaskController();
			
			HashMap<Integer, Boolean> flagToStop = new HashMap<Integer, Boolean>();
			HashMap<Integer, Date> proposedLastDate = new HashMap<Integer, Date>();
			
			for(Project proj : project.getPortfolio().getProjects())
			{
				taskController.resetScheduling(session, proj.getProjectId());
				flagToStop.put(proj.getProjectId(), false);
				proposedLastDate.put(proj.getProjectId(), proj.getProposedFinishDate());
				//getlastDayBeforeScheduling(proj)
			}
			
			
			
			String finalSolution = "";
			EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
			Date currentDate = projectBoundaries[0]; 
			Date lastDate = projectBoundaries[1];
			ServerResponse solution;
			
			
			boolean cannotCompleteSolution = false;
			HashMap<Integer, Integer> stoppedProjects = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> completedProjects = new HashMap<Integer, Integer>();
			
			
			Hashtable<Integer, Double> totalRetainedAmount = new Hashtable();
			
			Hashtable<Integer, Double> totalAdvancedPaymentAmount = new Hashtable();
			
			Hashtable<Integer, String> solutionInformation= new Hashtable();
			
			while(currentDate.getTime() <= lastDate.getTime() && !cannotCompleteSolution){
				
				projectBoundaries = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
				lastDate = projectBoundaries[1];
				
				String solvedProjects = "";
				String separator = "";
				SchedulePeriod currentPeriod = getCurrentPeriodBoundriesNew(session, currentDate, project.getPortfolio().getPortfolioId());
				
				String fromString = format.format(currentPeriod.getCurrent().getDateFrom());
				String toString = format.format(currentPeriod.getCurrent().getDateTo());
				
				finalSolution = finalSolution +  "<div class='div-row-red'>From: " + fromString + " - To: " + toString +"</div>";
				if (projectsPriority != null && projectsPriority.length() > 0) 
				{
					String[] projectsPriorityArray = projectsPriority.split(",");
					int currentProjectID;
					for (int i = 0 ; i < projectsPriorityArray.length; i++) { //Another condition to stop 
						currentProjectID = Integer.parseInt(projectsPriorityArray[i]);
						Project solvedProject = projectController.find(Project.class, currentProjectID);
						if(stoppedProjects.get(currentProjectID)==null)
						{
							finalSolution = finalSolution + "<div class='div-row-blue'>Project: " + solvedProject.getProjectCode() + "</div>";
							
							solution = getPeriodSolutionNew(session, currentProjectID, currentPeriod.getCurrent().getDateFrom(), currentPeriod.getCurrent().getDateTo(), currentPeriod.getNext().getDateTo(), solvedProjects, outputFormat, totalRetainedAmount, totalAdvancedPaymentAmount, completedProjects, solutionInformation);

							String solutionSummary ="";
							
							
							String currentSolInfo = solutionInformation.get(currentProjectID);
							
							String[] currentSolInfoArray = currentSolInfo.split(",");
							
							solutionSummary = solutionSummary + "<div class='div-row-grayInfo'>";
							double cashAvailableCurrent = new Double(currentSolInfoArray[0]);
							double totalCostCurrent = new Double(currentSolInfoArray[1]);
							double cashAvailableNext = new Double(currentSolInfoArray[2]);
							
							double payment = new Double(currentSolInfoArray[8]);
							
							
							if(cashAvailableCurrent<totalCostCurrent)
								solutionSummary = solutionSummary + "Initial: CashAvailableCurrent:" + "<font color='red'>" + currentSolInfoArray[0] + "</font>" + " TotalCostCurrent:" + "<font color='red'>" + currentSolInfoArray[1] + "</font>";
							else
								solutionSummary = solutionSummary + "Initial: CashAvailableCurrent:" + "<font color='blue'>" + currentSolInfoArray[0] + "</font>" + " TotalCostCurrent:" + "<font color='blue'>" + currentSolInfoArray[1] + "</font>";
							if(cashAvailableNext<0)
								solutionSummary = solutionSummary + " CashAvailableNext:" + "<font color='red'>" + currentSolInfoArray[2] + "</font>";
							else
								solutionSummary = solutionSummary + " CashAvailableNext:" + "<font color='blue'>" + currentSolInfoArray[2] + "</font>";
							solutionSummary = solutionSummary + "</div>";
							
							
							List<SolvedTask> solvedTasks = (List<SolvedTask>) solution.getData();
							
							cashAvailableNext = new Double(currentSolInfoArray[6]);
							
							if((payment>0 || !flagToStop.get(currentProjectID)) && cashAvailableNext>0){
								//solvedTasks!=null && solvedTasks.size()!=0 && atLeastOneTaskAtCurrentPeriod(solvedTasks,currentPeriod.getCurrent().getDateTo())){
								
								for (ProjectTask task : solvedProject.getProjectTasks()) {
									for (SolvedTask solvedTask : solvedTasks) {
										if (task.getTaskId() == solvedTask.getTaskId()) {
											solutionSummary = solutionSummary + "<div class='div-row-gray'>";
											if(task.getCalendarStartDate().getTime() == solvedTask.getScheduledStartDate().getTime())
												solutionSummary = solutionSummary + "<div class='notShiftedTaskLogo'>";
											else{
												if(solvedTask.getScheduledStartDate().getTime() <= currentPeriod.getCurrent().getDateTo().getTime())
													solutionSummary = solutionSummary + "<div class='shiftedTaskInLogo'>"; 
												else
													solutionSummary = solutionSummary + "<div class='shiftedTaskOutLogo'>";
											}
											
											String taskInitialString = format.format(task.getCalendarStartDate());
											String taskSchedualeString = format.format(solvedTask.getScheduledStartDate());
											
											solutionSummary = solutionSummary + "<div>" + solvedTask.getTaskName() + " (" + taskInitialString + " - " + taskSchedualeString + ")" +"</div></div></div>" ;
										}
									}
								}
								
								SolvedTask[] solvedTasksArray = solvedTasks.toArray((new SolvedTask[solvedTasks.size()]));
								commitSolution(session, currentProjectID, solvedTasksArray);
 							}
							else
							{
								Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, currentProjectID)[0];
								if(projectStartDate.getTime() < currentPeriod.getCurrent().getDateTo().getTime())
								{
									if(stoppedProjects.get(currentProjectID)==null)
										stoppedProjects.put(currentProjectID, 1);
									if(stoppedProjects.size() == project.getPortfolio().getProjects().size()){
										cannotCompleteSolution = true;
									}
									solutionSummary = solutionSummary + "<div class='div-row-gray'>" +  "Project stopped!" +"</div>" ;
								}else
									solutionSummary = solutionSummary + "<div class='div-row-gray'>" +  "Project didn't start yet!" +"</div>" ;
							}
							
							
							if(payment<=0){
								if(flagToStop.get(currentProjectID)== null)
									flagToStop.put(currentProjectID, true);
								else
									flagToStop.replace(currentProjectID, true);
							}else{
								if(flagToStop.get(currentProjectID)== null)
									flagToStop.put(currentProjectID, false);
								else
									flagToStop.replace(currentProjectID, false);
							}
							
							
							if(getScheduledTasksCount(solvedProject,currentPeriod.getCurrent().getDateTo())==solvedProject.getProjectTasks().size() && completedProjects.get(currentProjectID) == null){
								int paymentRequestPeriod = solvedProject.getPaymentRequestPeriod();
								Calendar calendar = Calendar.getInstance();
								Date nextToDate = currentPeriod.getCurrent().getDateTo();
								calendar.setTime(nextToDate);
								calendar.add(Calendar.DATE, paymentRequestPeriod);
								Date lastPaymentDate = calendar.getTime();
								
								List<ProjectTask> completetedProjectTasks = solvedProject.getProjectTasks();
								for(ProjectTask completetedTask: completetedProjectTasks)
								{
									if(completetedTask.getScheduledStartDate()!=null)
										calendar.setTime(completetedTask.getScheduledStartDate());
									else{
										calendar.setTime(completetedTask.getCalendarStartDate());
										System.out.println(completetedTask.getTaskName());
									}
									calendar.add(Calendar.DATE, completetedTask.getDuration());
									Date completedTaskEndDate = calendar.getTime();
									
									if(completedTaskEndDate.getTime() > lastPaymentDate.getTime())
										lastPaymentDate = completedTaskEndDate;
								}
								
								
								double lastPaymentValue = PaymentUtil.getExpectedPaymentNew(session, solvedProject,  currentPeriod.getCurrent().getDateFrom(),  currentPeriod.getCurrent().getDateTo(), lastPaymentDate,  0, solvedProject.getRetainedPercentage().doubleValue(), solvedProject.getAdvancedPaymentPercentage().doubleValue(), projectBoundaries[1], true);
								//double lastPaymentValue = PaymentUtil.getPortfolioPaymentNew(session, currentPeriod.getNext().getDateTo(), lastPaymentDate, paymentDetails , solvedProject.getPortfolio() , false, projectBoundaries);
								
								double originalLastPayment = lastPaymentValue / (1.0-project.getRetainedPercentage().doubleValue()-project.getAdvancedPaymentPercentage().doubleValue());
								double originalBeforeLastPayment = payment / (1.0-project.getRetainedPercentage().doubleValue()-project.getAdvancedPaymentPercentage().doubleValue());
								if(totalRetainedAmount.get(projectId)==null){
									totalRetainedAmount.put(projectId, (originalLastPayment+originalBeforeLastPayment) * project.getRetainedPercentage().doubleValue());
								}else{
									totalRetainedAmount.replace(projectId, totalRetainedAmount.get(projectId) + (originalLastPayment+originalBeforeLastPayment) * project.getRetainedPercentage().doubleValue());
								}
									
								lastPaymentValue = lastPaymentValue + totalRetainedAmount.get(currentProjectID).doubleValue();
								
								double delayPenalty = 0;
								Date lastDayAfterScheduling = getlastDayAfterScheduling(solvedProject);
								long days = 0;
								if(proposedLastDate.get(solvedProject.getProjectId())!=null)
								{
									days = (lastDayAfterScheduling.getTime() - proposedLastDate.get(solvedProject.getProjectId()).getTime()) / (1000 * 60 * 60 * 24);
								}
								if(days > 0)
									delayPenalty = -1 * days * solvedProject.getDelayPenaltyAmount().doubleValue();
								
								lastPaymentValue = lastPaymentValue + delayPenalty;
								
								String lastPaymentDateString = format.format(lastPaymentDate);
								String lastPaymentStartString = format.format( currentPeriod.getCurrent().getDateFrom());
								String lastPaymentToString = format.format(currentPeriod.getCurrent().getDateTo());
								
								solutionSummary = solutionSummary + "<div class='div-row-orange'>" +  "Last Payment:" + lastPaymentValue + " At: " + lastPaymentDateString +"</div>" ;
								solutionSummary = solutionSummary + "<div class='div-row-orange'>" +  "Last Payment = Payment (" + lastPaymentStartString + " - " + lastPaymentToString + ") " + (lastPaymentValue + delayPenalty - totalRetainedAmount.get(currentProjectID).doubleValue()) + " + Retained amount: " + totalRetainedAmount.get(currentProjectID).floatValue() + " + Delay Penalty: " + delayPenalty +"</div>" ;
								
								double balance = PaymentUtil.getPortfolioOpenBalanceNew(session, project.getPortfolio() , lastPaymentDate); //balance that is accumulated from the previous period
 								System.out.println(balance);
								
								solutionSummary = solutionSummary + "<div class='div-row-orange'>" +  "Profit:" + (lastPaymentValue + balance) +"</div>" ;
								
								completedProjects.put(currentProjectID, 1);
								//set flag Done
							}
							
							solutionSummary = solutionSummary + "<div class='div-row-grayInfo'>";
							cashAvailableCurrent = new Double(currentSolInfoArray[4]);
							totalCostCurrent = new Double(currentSolInfoArray[5]);
							cashAvailableNext = new Double(currentSolInfoArray[6]);
							if(cashAvailableCurrent<totalCostCurrent)
								solutionSummary = solutionSummary + "Final: CashAvailableCurrent:" + "<font color='red'>" + currentSolInfoArray[4] + "</font>" + " TotalCostCurrent:" + "<font color='red'>" + currentSolInfoArray[5] + "</font>";
							else
								solutionSummary = solutionSummary + "Final: CashAvailableCurrent:" + "<font color='blue'>" + currentSolInfoArray[4] + "</font>" + " TotalCostCurrent:" + "<font color='blue'>" + currentSolInfoArray[5] + "</font>";
							if(cashAvailableNext<0)
								solutionSummary = solutionSummary + " CashAvailableNext:" + "<font color='red'>" + currentSolInfoArray[6] + "</font>" + " Payment: " + payment;
							else
								solutionSummary = solutionSummary + " CashAvailableNext:" + "<font color='blue'>" + currentSolInfoArray[6] + "</font>" + " Payment: " + payment;
							solutionSummary = solutionSummary + "</div>";
							
							finalSolution = finalSolution +  solutionSummary;
							solvedProjects = solvedProjects + separator + currentProjectID;
							separator = ",";
						}
					}
				}
				currentDate = currentPeriod.getNext().getDateFrom();
			}
			
			System.out.println(finalSolution);
			System.out.println("totalRetainedAmount \n _______________________________________________________________\n");
			System.out.println(totalRetainedAmount);
			
			System.out.println("totalAdvancedPaymentAmount \n _______________________________________________________________\n");
			System.out.println(totalAdvancedPaymentAmount);
			
			return new ServerResponse("0" , "Success" , finalSolution);
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error finding solution: %s", e.getMessage() ), e);
		}
		
		
	}
	

	private Date getlastDayBeforeScheduling(Project project) {
		Date lastDay = new Date(0);
		List<ProjectTask> tasks = project.getProjectTasks();
		for(ProjectTask task: tasks)
		{
			Date taskEndDate = null;
			if(task.getCalendarStartDate()!=null)
				taskEndDate = task.getCalendarStartDate();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(taskEndDate);
			calendar.add(Calendar.DATE, task.getDuration());
			taskEndDate = calendar.getTime();
			
			if(lastDay.getTime() < taskEndDate.getTime())
				lastDay = taskEndDate;
				
		}
		return lastDay;
	}


	private Date getlastDayAfterScheduling(Project project) {
		Date lastDay = new Date(0);
		List<ProjectTask> tasks = project.getProjectTasks();
		for(ProjectTask task: tasks)
		{
			Date taskEndDate;
			if(task.getScheduledStartDate()!=null)
				taskEndDate = task.getScheduledStartDate();
			else
				taskEndDate = task.getCalendarStartDate();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(taskEndDate);
			calendar.add(Calendar.DATE, task.getDuration());
			taskEndDate = calendar.getTime();
			
			if(lastDay.getTime() < taskEndDate.getTime())
				lastDay = taskEndDate;
				
		}
		return lastDay;
	}


	private boolean atLeastOneTaskAtCurrentPeriod(List<SolvedTask> solvedTasks, Date to) {
		
		for(SolvedTask task : solvedTasks)
		{
			if(task.getScheduledStartDate().getTime() < to.getTime())
				return true;
		}
		
		return false;
	}


	private int getScheduledTasksCount(Project project, Date to) {
		List<ProjectTask> projectTasks = project.getProjectTasks();
		int count = 0;
		for(ProjectTask task : projectTasks)
		{
			Date startDate = task.getCalendarStartDate();
			if (startDate == null) {
				startDate =	task.getActualStartDate();
			}
			if (startDate == null) {
				startDate = task.getScheduledStartDate();
			}
			if (startDate == null) {
				startDate = task.getTentativeStartDate();
			}
			if (startDate == null) {
				startDate = new Date();
			}
			
			int duration = task.getCalenderDuration();
			if (duration == 0) {
				duration = task.getDuration();
			}
			
			long endDate = startDate.getTime() + (duration - 1) * 86400000;
			
			if(endDate <= to.getTime())
					count++;
		}
		return count;
	}


	private SchedulePeriod getCurrentPeriodBoundries (HttpSession session, Date startDate, int portfolioId) {
		SchedulePeriod schedulePeriod = new SchedulePeriod();
		try {
			
			Period paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session, startDate, portfolioId);
			Period financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, startDate, portfolioId);
			
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
				
			return schedulePeriod;
			
		} catch (OptimaException e) {
			
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	public SchedulePeriod getCurrentPeriodBoundriesNew (HttpSession session, Date startDate, int portfolioId) {
		SchedulePeriod schedulePeriod = new SchedulePeriod();
		try {
			
			Period paymentSchedulePeriod = PaymentUtil.findPaymentScheduleNew(session, startDate, portfolioId);
			Period financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, startDate, portfolioId);
			
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
			
		    paymentSchedulePeriod = PaymentUtil.findPaymentScheduleNew(session, calendar.getTime(), portfolioId);
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
				
			return schedulePeriod;
			
		} catch (OptimaException e) {
			
			e.printStackTrace();
			return null;
		}
		
	}

	public ServerResponse  getPeriodSolutionNew (HttpSession session , int projectId, Date from , Date to , Date next , String solvedProjects , String outputFormat,
			Hashtable<Integer, Double> totalRetainedAmount, Hashtable<Integer, Double> totalAdvancedPaymentAmount, HashMap<Integer, Integer> completedProjects, Hashtable<Integer, String> solutionInformation) {
		Map<Integer, ProjectPaymentDetail> paymentDetails = PaymentUtil.getPaymentDetailsNew(session, projectId, from, to);
		
		EntityController<Project> projectController = new EntityController<>(session.getServletContext());
		EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());

		double paymentCurrent = 0;
		try {
			
			solutionLock.lock();
			
			Project project = projectController.find(Project.class, projectId);
			
			double advancedPaymentAmount = project.getAdvancedPaymentAmount().doubleValue();
			double advancedPaymentPercentage = project.getAdvancedPaymentPercentage().doubleValue();
			double retainedPercentage = project.getRetainedPercentage().doubleValue();
			
			double retainedAmountDeduction = 0.0;
			double advancedPaymentDeduction = 0.0;
			
			OptimaLogFactory factory = (OptimaLogFactory)session.getServletContext().getAttribute(JsonRpcInitializer.__LOG__FACTORY);
			Logger logger = factory.getProjectLogger(project.getProjectCode());
			Logger solutionOutput = factory.getProjectOutput(project.getProjectCode());
			solutionOutput.info("Project Name:" + project.getProjectCode() + " - " + project.getProjectName());
			logger.info("######################################################################################");
			logger.info("Generating solution");
			double leftOverCost = PaymentUtil.getPortfolioLeftOverCost(portController , project.getPortfolio() , from, to, completedProjects); //Overhead current + cost of any task starts before this period and still not finished 
			double openBalance = PaymentUtil.getPortfolioOpenBalanceNew(session, project.getPortfolio() , from); //balance that is accumulated from the previous period
			
			
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRanges(controller, project.getPortfolio().getPortfolioId());
			
			double payment = PaymentUtil.getPortfolioPaymentNew(session, from , to , paymentDetails , project.getPortfolio() , false, projectBoundaries);
			
			paymentCurrent = payment;
			
			double financeLimit = PaymentUtil.getFinanceLimit(session , project.getPortfolio().getPortfolioId() , from);
			double extraPayment = PaymentUtil.getExtraPayment(session , project.getPortfolio().getPortfolioId() , from);
			financeLimit += extraPayment;
			
			double financeLimitNextPeriod = PaymentUtil.getFinanceLimit(session , project.getPortfolio().getPortfolioId() , to);
			double extraPaymentNextPeriod = PaymentUtil.getExtraPayment(session , project.getPortfolio().getPortfolioId() , to);
			financeLimitNextPeriod += extraPaymentNextPeriod;
			
			double cashOutOthers = PaymentUtil.getCashOutOtherProjects(session, project , from, to , solvedProjects); // cash out for solved projects
			logger.info(String.format("Period: %s - %s", from.toString(), to.toString()) );
			logger.info("Startup Data");
			logger.info(String.format("leftover Cost: %f" , leftOverCost));
			logger.info(String.format("openBalance: %f" , openBalance));
			logger.info(String.format("payment: %f" , payment));
			logger.info(String.format("financeLimit: %f" , financeLimit));
			logger.info(String.format("Cashout other projects (based on current schedule): %f" , cashOutOthers));
			
			
			
			
			List<ProjectTask> eligibleTasks  = null;
			boolean completed = false;
			
			if(isPaymentAtThisPeriod(session, from, to, project, controller, projectBoundaries)){
				retainedAmountDeduction = retainedPercentage * payment;
				advancedPaymentDeduction = advancedPaymentAmount * advancedPaymentPercentage;
			}
			
			double cashAvailable = financeLimit + openBalance + payment - leftOverCost  - cashOutOthers;
			double cashAvailableNextPeriod = financeLimitNextPeriod + openBalance + payment - leftOverCost  - cashOutOthers - retainedAmountDeduction - advancedPaymentDeduction;
			
			
			Map<Integer, TaskState> taskStates = initTaskState(project , logger);
			eligibleTasks = PaymentUtil.getEligibleTasks(project, from, to, true);
			int iteration = 0;
			boolean initial = true;
			String initialInfo = "";
			String solInfo = "";
			while (!completed) {
				List<ProjectTask> currentEligibleSet = PaymentUtil.getEligibleTasks(project, from, to , true);
				double eligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(currentEligibleSet, from , to);
				
				//Bug#4
				double otherPojectsEligibleTasksCurrentPeroidCost =  PaymentUtil.getOtherProjectsCurrentPeriodCost(project, from , to);	
				double otherPojectsEligibleTasksLeftOverCost =  PaymentUtil.getOtherProjectsLeftOverCostNew(session, project, from , to, projectBoundaries[1], completedProjects);	
				//-------------
				
				//TODO Input Parameter should consider all tasks going through the current period
				
				List<ProjectTask> currentTaskSet = PaymentUtil.getCurrentTasks(project, from, to , true);
				
				double eligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCostNew(session, currentTaskSet, project, from, to, projectBoundaries[1]);

				paymentDetails = PaymentUtil.getPaymentDetailsNew(session, projectId, to, next);
				double expectedCashIn = PaymentUtil.getPortfolioPaymentNew(session, to , next, paymentDetails, project.getPortfolio() , false, projectBoundaries);
				
				paymentCurrent = expectedCashIn;
				
				double totalCostCurrent = eligibleTasksCurrentPeroidCost; // - eligiableTaskFinanceCost;
				double leftOverNextCost = eligibleTasksLeftOverCost + otherPojectsEligibleTasksLeftOverCost; // - nextPeriodLeftoverFinanceCost;
				
				logger.info(String.format("Current eligible tasks cost (current period): %f" ,  eligibleTasksCurrentPeroidCost ));
				logger.info(String.format("Current eligible tasks total cost: %f" ,  totalCostCurrent ));
				logger.info(String.format("Expected Cash: %f" ,  expectedCashIn ));
				logger.info(String.format("Current eligible tasks cost (leftover next period): %f" ,  eligibleTasksLeftOverCost ));
				logger.info(String.format("Current eligible tasks leftover total cost (leftover next period): %f" ,  leftOverNextCost ));
				
				solutionOutput.info(String.format("Selected: Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
						iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(currentEligibleSet), PaymentUtil.getTaskListStart(currentEligibleSet) , 
						PaymentUtil.getProjectLength(project , outputFormat , solutionOutput) , totalCostCurrent,
						cashAvailableNextPeriod - totalCostCurrent + expectedCashIn - leftOverNextCost, 
						totalCostCurrent <= cashAvailable && cashAvailableNextPeriod - totalCostCurrent + expectedCashIn >= leftOverNextCost?"Yes":"No",
								cashAvailable - totalCostCurrent < 0 ? 0: cashAvailable - totalCostCurrent )
						);
				
				if(initial)
				{
					initialInfo = cashAvailable + "," + totalCostCurrent + "," + (cashAvailableNextPeriod - totalCostCurrent + expectedCashIn - leftOverNextCost) + "," + (cashAvailable - totalCostCurrent);
					solInfo = cashAvailable + "," + totalCostCurrent + "," + (cashAvailableNextPeriod - totalCostCurrent + expectedCashIn - leftOverNextCost) + "," + (cashAvailable - totalCostCurrent);
					initial = false;
				}else
					solInfo = cashAvailable + "," + totalCostCurrent + "," + (cashAvailableNextPeriod - totalCostCurrent + expectedCashIn - leftOverNextCost) + "," + (cashAvailable - totalCostCurrent);
				
				if (totalCostCurrent <= cashAvailable && cashAvailableNextPeriod - totalCostCurrent + expectedCashIn >= leftOverNextCost ) {
					logger.info("######################################################################################");
					logger.info(String.format("Solution Found: project tasks cost(elligibe) : %f  , cash before: %f , cash after: %f" , totalCostCurrent , cashAvailable , cashAvailable - totalCostCurrent ));
					for (ProjectTask task : eligibleTasks) {
						//task.setScheduledStartDate(PaymentUtil.getTaskDate(task));
						logger.info(String.format("Task: %s , StartDate: %s" , task.getTaskName() , task.getScheduledStartDate()));
					}
					logger.info("######################################################################################");
					completed = true;
				} else {
					logger.info("--------------------------------------------------------------------------------------");
					
					List<TaskSolution> solutions = new LinkedList<>();
					boolean noChange = true;
					for (ProjectTask task : currentEligibleSet) {
						task.setScheduledStartDate(null);
						Date taskDate = PaymentUtil.getTaskDate(task);
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(taskDate);
						do {
							calendar.add(Calendar.DATE, 1);
						}
						while (PaymentUtil.isDayOff(calendar.getTime(), project.getDaysOffs()) 
								|| PaymentUtil.isWeekendDay(calendar.getTime() ,  project.getWeekendDays()) );
						
						//Bug#2 Not checking all the cases
						//if (to.equals(calendar.getTime()) || to.after(calendar.getTime())) {
							noChange = false;
							task.setCalendarStartDate(calendar.getTime());
							PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
							updateScheduledState(project , taskStates);
						
							TaskSolution solution = new TaskSolution(task);
							List<ProjectTask> solutionCurrenteligibleTasks = PaymentUtil.getEligibleTasks(project, from, to , true);
							
							double solutionEligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(solutionCurrenteligibleTasks, from , to);
							
							//TODO Input Parameter should consider all tasks going through the current period
							List<ProjectTask> solutionCurrentTaskSet = PaymentUtil.getCurrentTasks(project, from, to , true);
							double solutionEligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCostNew(session, solutionCurrentTaskSet, project, from, to, projectBoundaries[1]);
							
							paymentDetails = PaymentUtil.getPaymentDetailsNew(session, projectId, to, next);
							double solutionExpectedCashIn = PaymentUtil.getPortfolioPaymentNew(session, to , next , paymentDetails, project.getPortfolio() , false, projectBoundaries); 
							
							double solutionTotalCostCurrent = solutionEligibleTasksCurrentPeroidCost;// - solutionEligiableTaskFinanceCost; 
							
							double solutionOtherPojectsEligibleTasksLeftOverCost =  PaymentUtil.getOtherProjectsLeftOverCostNew(session, project, from , to, projectBoundaries[1], completedProjects);	
							
							int projectLength = PaymentUtil.getProjectLength(project , outputFormat , solutionOutput);
							solution.setCurrentPeriodCost(solutionTotalCostCurrent);
							solution.setLeftOversCost(solutionEligibleTasksLeftOverCost + solutionOtherPojectsEligibleTasksLeftOverCost); // - solutionNextPeriodLeftOverFinanceCost);
						
							solution.setIncome(solutionExpectedCashIn);
							solution.setProjectLength(projectLength);
							solution.setStartDate(calendar.getTime());
							
							solutionOutput.info(String.format("Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
									iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(solutionCurrenteligibleTasks) , PaymentUtil.getTaskListStart(solutionCurrenteligibleTasks) , 
									projectLength , solutionTotalCostCurrent,
									cashAvailableNextPeriod - solutionTotalCostCurrent + solutionExpectedCashIn - solution.getLeftOversCost(), 
									solutionTotalCostCurrent <= cashAvailable && cashAvailableNextPeriod - solutionTotalCostCurrent + solutionExpectedCashIn >= solution.getLeftOversCost()?"Yes":"No",
									cashAvailable - solutionTotalCostCurrent < 0 ? 0 :cashAvailable - solutionTotalCostCurrent  )
									);
							solutions.add(solution);
							logger.info(String.format("Shifting task: %s , Project Length after shift: %d , New task start date: %s , Current Period Cost after shift:%f, Leftovers Cost after shift:%f" , task.getTaskName() , solution.getProjectLength() , solution.getStartDate().toString() , solution.getCurrentPeriodCost(), solution.getLeftOversCost()));
							task.setCalendarStartDate(taskDate);
							resetTaskState(project , taskStates);
							
						//} 
					}
					if (noChange) {
						completed = true; // no tasks can be scheduled this period. Stop here
						logger.info("All tasks are out of period - please solve for next period");
					} else {
					
						TaskSolution nextIteration = PaymentUtil.findSolutionIteration(solutions, cashAvailable, cashAvailableNextPeriod);
						if (nextIteration != null) {
							nextIteration.getTask().setCalendarStartDate(nextIteration.getStartDate());
							
							PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
							taskStates = initTaskState(project , logger);
							
							logger.info(String.format("Next Iteration: %s , Project Length after shift: %d , New task start date: %s , Current period cost after shift:%f, Leftover cost after shift:%f" , nextIteration.getTask().getTaskName() , nextIteration.getProjectLength() , nextIteration.getStartDate().toString() , nextIteration.getCurrentPeriodCost(), nextIteration.getLeftOversCost()));
							
						} else {
							return new ServerResponse("0" , "Success" , null);							
						}
					}
					
				}
				iteration++;
			}
			solutionInformation.put(projectId, initialInfo + "," + solInfo + "," + paymentCurrent);
			
			if(from.getTime() != project.getPropusedStartDate().getTime())
			{
				double originalPayment = payment / (1.0-project.getRetainedPercentage().doubleValue()-project.getAdvancedPaymentPercentage().doubleValue());
				
				if(totalRetainedAmount.get(projectId)==null)
					totalRetainedAmount.put(projectId, originalPayment * project.getRetainedPercentage().doubleValue());
				else
					totalRetainedAmount.replace(projectId, totalRetainedAmount.get(projectId)+originalPayment * project.getRetainedPercentage().doubleValue());
				
				if(totalAdvancedPaymentAmount.get(projectId)==null)
					totalAdvancedPaymentAmount.put(projectId, originalPayment*project.getAdvancedPaymentPercentage().doubleValue());
				else
					totalAdvancedPaymentAmount.replace(projectId, totalAdvancedPaymentAmount.get(projectId) + originalPayment * project.getAdvancedPaymentAmount().doubleValue());
				
			}

			
			List<SolvedTask> solvedTasks = new ArrayList<>();
			for (ProjectTask task : eligibleTasks) {
				SolvedTask solvedTask = new SolvedTask();
				solvedTask.setTaskId(task.getTaskId());
				solvedTask.setTaskName(task.getTaskName());
				solvedTask.setTaskDescription(task.getTaskDescription());
				solvedTask.setScheduledStartDate(task.getCalendarStartDate());
				solvedTask.setCalenderDuration(task.getCalenderDuration());
				solvedTasks.add(solvedTask);
			}
			
			return new ServerResponse("0" , "Success" , solvedTasks);
			
		} catch (EntityControllerException | OptimaException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error solving period for project %d: %s" , projectId , e.getMessage() ), e);
		} finally {
			solutionLock.unlock();
		}
		
		
	}

	
	private boolean isPaymentAtThisPeriod(HttpSession session, Date from, Date to, Project project, EntityController<ProjectPayment> controller, Date[] projectBoundaries) {
		try {
			List<ProjectPayment> projectPayments = PaymentUtil.getProjectPayments(session, project, projectBoundaries[1]);
			for(ProjectPayment projectPayment : projectPayments)
			{
				if(projectPayment.getPaymentDate() == to)
					return true;
			}
		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
		return false;
	}


	public ServerResponse  getPeriodSolution(HttpSession session , int projectId, Date from , Date to , Date next , String paymentDetailsJson , String solvedProjects , String outputFormat) {
		Map<Integer, ProjectPaymentDetail> paymentDetails = PaymentUtil.getPaymentDetailsMap(paymentDetailsJson);
		EntityController<Project> projectController = new EntityController<>(session.getServletContext());
		EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
		try {
			solutionLock.lock();
			
			Project project = projectController.find(Project.class, projectId);
			OptimaLogFactory factory = (OptimaLogFactory)session.getServletContext().getAttribute(JsonRpcInitializer.__LOG__FACTORY);
			Logger logger = factory.getProjectLogger(project.getProjectCode());
			Logger solutionOutput = factory.getProjectOutput(project.getProjectCode());
			solutionOutput.info("Project Name:" + project.getProjectCode() + " - " + project.getProjectName());
			logger.info("######################################################################################");
			logger.info("Generating solution");
			double leftOverCost = PaymentUtil.getPortfolioLeftOverCost(portController , project.getPortfolio() , from, to, null);
			double openBalance = PaymentUtil.getPortfolioOpenBalance(session, project.getPortfolio() , from);
			double payment = PaymentUtil.getPortfolioPayment(from , to , paymentDetails , project.getPortfolio() , true);
			
			
			double financeLimit = PaymentUtil.getFinanceLimit(session , project.getPortfolio().getPortfolioId() , from);
			double financeLimitNextPeriod = PaymentUtil.getFinanceLimit(session , project.getPortfolio().getPortfolioId() , to);
			
			double cashOutOthers = PaymentUtil.getCashOutOtherProjects(session, project , from, to , solvedProjects); 
			logger.info(String.format("Period: %s - %s", from.toString(), to.toString()) );
			logger.info("Startup Data");
			logger.info(String.format("leftover Cost: %f" , leftOverCost));
			logger.info(String.format("openBalance: %f" , openBalance));
			logger.info(String.format("payment: %f" , payment));
			logger.info(String.format("financeLimit: %f" , financeLimit));
			logger.info(String.format("Cashout other projects (based on current schedule): %f" , cashOutOthers));
			
			
			
			
			List<ProjectTask> eligibleTasks  = null;
			boolean completed = false;
			double cashAvailable = financeLimit + openBalance + payment - leftOverCost  - cashOutOthers;
			double cashAvailableNextPeriod = financeLimitNextPeriod + openBalance + payment - leftOverCost  - cashOutOthers;
			
			
			Map<Integer, TaskState> taskStates = initTaskState(project , logger);
			eligibleTasks = PaymentUtil.getEligibleTasks(project, from, to, true);
			int iteration = 0;
			while (!completed) {
				List<ProjectTask> currentEligibleSet = PaymentUtil.getEligibleTasks(project, from, to , true);
				double eligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(currentEligibleSet, from , to);
				
				//Bug#4
				double otherPojectsEligibleTasksCurrentPeroidCost =  PaymentUtil.getOtherProjectsCurrentPeriodCost(project, from , to);	
				double otherPojectsEligibleTasksLeftOverCost =  PaymentUtil.getOtherProjectsLeftOverCost(project, from , to);	
				//-------------
				
				//TODO Input Parameter should consider all tasks going through the current period
				List<ProjectTask> currentTaskSet = PaymentUtil.getCurrentTasks(project, from, to , true);
				
				double eligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCost(currentTaskSet, project, from, to);
				double expectedCashIn = PaymentUtil.getPortfolioPayment(to , next, paymentDetails, project.getPortfolio() , false);
				
				double totalCostCurrent = eligibleTasksCurrentPeroidCost; // - eligiableTaskFinanceCost;
				double leftOverNextCost = eligibleTasksLeftOverCost + otherPojectsEligibleTasksLeftOverCost; // - nextPeriodLeftoverFinanceCost;
				
				logger.info(String.format("Current eligible tasks cost (current period): %f" ,  eligibleTasksCurrentPeroidCost ));
				logger.info(String.format("Current eligible tasks total cost: %f" ,  totalCostCurrent ));
				logger.info(String.format("Expected Cash: %f" ,  expectedCashIn ));
				logger.info(String.format("Current eligible tasks cost (leftover next period): %f" ,  eligibleTasksLeftOverCost ));
				logger.info(String.format("Current eligible tasks leftover total cost (leftover next period): %f" ,  leftOverNextCost ));
				
				solutionOutput.info(String.format("Selected: Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
						iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(currentEligibleSet), PaymentUtil.getTaskListStart(currentEligibleSet) , 
						PaymentUtil.getProjectLength(project , outputFormat , solutionOutput) , totalCostCurrent,
						cashAvailableNextPeriod - totalCostCurrent + expectedCashIn - leftOverNextCost, 
						totalCostCurrent <= cashAvailable && cashAvailableNextPeriod - totalCostCurrent + expectedCashIn >= leftOverNextCost?"Yes":"No",
								cashAvailable - totalCostCurrent < 0 ? 0: cashAvailable - totalCostCurrent )
						);
				
				
				if (totalCostCurrent <= cashAvailable && cashAvailableNextPeriod - totalCostCurrent + expectedCashIn >= leftOverNextCost ) {
					logger.info("######################################################################################");
					logger.info(String.format("Solution Found: project tasks cost(elligibe) : %f  , cash before: %f , cash after: %f" , totalCostCurrent , cashAvailable , cashAvailable - totalCostCurrent ));
					for (ProjectTask task : eligibleTasks) {
						//task.setScheduledStartDate(PaymentUtil.getTaskDate(task));
						logger.info(String.format("Task: %s , StartDate: %s" , task.getTaskName() , task.getScheduledStartDate()));
					}
					logger.info("######################################################################################");
					completed = true;
				} else {
					logger.info("--------------------------------------------------------------------------------------");
					
					List<TaskSolution> solutions = new LinkedList<>();
					boolean noChange = true;
					for (ProjectTask task : currentEligibleSet) {
						task.setScheduledStartDate(null);
						Date taskDate = PaymentUtil.getTaskDate(task);
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(taskDate);
						do {
							calendar.add(Calendar.DATE, 1);
						}
						while (PaymentUtil.isDayOff(calendar.getTime(), project.getDaysOffs()) 
								|| PaymentUtil.isWeekendDay(calendar.getTime() ,  project.getWeekendDays()) );
						
						//Bug#2 Not checking all the cases
						//if (to.equals(calendar.getTime()) || to.after(calendar.getTime())) {
							noChange = false;
							task.setCalendarStartDate(calendar.getTime());
							PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
							updateScheduledState(project , taskStates);
						
							TaskSolution solution = new TaskSolution(task);
							List<ProjectTask> solutionCurrenteligibleTasks = PaymentUtil.getEligibleTasks(project, from, to , true);
							
							double solutionEligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(solutionCurrenteligibleTasks, from , to);	
							
							//TODO Input Parameter should consider all tasks going through the current period
							List<ProjectTask> solutionCurrentTaskSet = PaymentUtil.getCurrentTasks(project, from, to , true);
							double solutionEligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCost(solutionCurrentTaskSet, project, from, to);
							
							double solutionExpectedCashIn = PaymentUtil.getPortfolioPayment(to , next , paymentDetails, project.getPortfolio() , false); 
							double solutionTotalCostCurrent = solutionEligibleTasksCurrentPeroidCost;// - solutionEligiableTaskFinanceCost; 
							
							double solutionOtherPojectsEligibleTasksLeftOverCost =  PaymentUtil.getOtherProjectsLeftOverCost(project, from , to);	
							
							int projectLength = PaymentUtil.getProjectLength(project , outputFormat , solutionOutput);
							solution.setCurrentPeriodCost(solutionTotalCostCurrent);
							solution.setLeftOversCost(solutionEligibleTasksLeftOverCost + solutionOtherPojectsEligibleTasksLeftOverCost); // - solutionNextPeriodLeftOverFinanceCost);
						
							solution.setIncome(solutionExpectedCashIn);
							solution.setProjectLength(projectLength);
							solution.setStartDate(calendar.getTime());
							
							solutionOutput.info(String.format("Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
									iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(solutionCurrenteligibleTasks) , PaymentUtil.getTaskListStart(solutionCurrenteligibleTasks) , 
									projectLength , solutionTotalCostCurrent,
									cashAvailableNextPeriod - solutionTotalCostCurrent + solutionExpectedCashIn - solution.getLeftOversCost(), 
									solutionTotalCostCurrent <= cashAvailable && cashAvailableNextPeriod - solutionTotalCostCurrent + solutionExpectedCashIn >= solution.getLeftOversCost()?"Yes":"No",
									cashAvailable - solutionTotalCostCurrent < 0 ? 0 :cashAvailable - solutionTotalCostCurrent  )
									);
							solutions.add(solution);
							logger.info(String.format("Shifting task: %s , Project Length after shift: %d , New task start date: %s , Current Period Cost after shift:%f, Leftovers Cost after shift:%f" , task.getTaskName() , solution.getProjectLength() , solution.getStartDate().toString() , solution.getCurrentPeriodCost(), solution.getLeftOversCost()));
							task.setCalendarStartDate(taskDate);
							resetTaskState(project , taskStates);
							
						//} 
					}
					if (noChange) {
						completed = true; // no tasks can be scheduled this period. Stop here
						logger.info("All tasks are out of period - please solve for next period");
					} else {
					
						TaskSolution nextIteration = PaymentUtil.findSolutionIteration(solutions, cashAvailable, cashAvailableNextPeriod);
						if (nextIteration != null) {
							nextIteration.getTask().setCalendarStartDate(nextIteration.getStartDate());
							
							PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
							taskStates = initTaskState(project , logger);
							
							logger.info(String.format("Next Iteration: %s , Project Length after shift: %d , New task start date: %s , Current period cost after shift:%f, Leftover cost after shift:%f" , nextIteration.getTask().getTaskName() , nextIteration.getProjectLength() , nextIteration.getStartDate().toString() , nextIteration.getCurrentPeriodCost(), nextIteration.getLeftOversCost()));
							
						} else {
							return new ServerResponse("0" , "Success" , null);							
						}
					}
					
				}
				iteration++;
			}
			List<SolvedTask> solvedTasks = new ArrayList<>();
			for (ProjectTask task : eligibleTasks) {
				SolvedTask solvedTask = new SolvedTask();
				solvedTask.setTaskId(task.getTaskId());
				solvedTask.setTaskName(task.getTaskName());
				solvedTask.setTaskDescription(task.getTaskDescription());
				solvedTask.setScheduledStartDate(task.getCalendarStartDate());
				solvedTask.setCalenderDuration(task.getCalenderDuration());
				solvedTasks.add(solvedTask);
			}
			
			return new ServerResponse("0" , "Success" , solvedTasks);
			
		} catch (EntityControllerException | OptimaException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003" , String.format("Error solving period for project %d: %s" , projectId , e.getMessage() ), e);
		} finally {
			solutionLock.unlock();
		}
		
		
	}


	/**
	 * @param project
	 * @return
	 */
	private Map<Integer, TaskState> initTaskState(Project project , Logger logger) {
		Map<Integer , TaskState > taskStates = new HashMap<Integer, TaskState>();
		logger.info("Current state:");
		logger.info("=====================================================================================================");
		for (ProjectTask task : project.getProjectTasks()) {
			logger.info(String.format("Task: %s , StartDate (Calendar) : %s, StartDate (Scheduled): %s, duration: %d" , task.getTaskName() , task.getCalendarStartDate(),  task.getScheduledStartDate() , task.getCalenderDuration()));
			taskStates.put(task.getTaskId(), new TaskState(task.getCalendarStartDate(), task.getCalendarStartDate() , task.getCalenderDuration() , task.getCalenderDuration() , task.getScheduledStartDate() , task.getScheduledStartDate()));
		}
		return taskStates;
	}
	
	private void resetTaskState(Project project,
			Map<Integer, TaskState> taskStates) {
		for (ProjectTask task : project.getProjectTasks()) {
			TaskState state = taskStates.get(task.getTaskId());
			task.setCalendarStartDate(state.getOriginalStartDate());
			task.setCalenderDuration(state.getOriginalCalendarDuration());
			task.setScheduledStartDate(state.getOriginalScheduledStartDate());
		}
		
	}


	private void updateScheduledState(Project project,
			Map<Integer, TaskState> taskStates) {
		for (ProjectTask task : project.getProjectTasks()) {
			TaskState state = taskStates.get(task.getTaskId());
			state.setShiftedCalendarDuration(task.getCalenderDuration());
			state.setShiftedStartDate(task.getCalendarStartDate());
			state.setShiftedScheduledStartData(task.getScheduledStartDate());
		}
		
	}


	public ServerResponse  commitSolution(HttpSession session , int projectId, SolvedTask[] tasks) {
		
		EntityController<Project> projectController = new EntityController<>(session.getServletContext());
		try {
			Project project = projectController.find(Project.class, projectId);
			for (ProjectTask task : project.getProjectTasks()) {
				for (SolvedTask solvedTask : tasks) {
					if (task.getTaskId() == solvedTask.getTaskId()) {
						task.setScheduledStartDate(solvedTask.getScheduledStartDate());
						task.setCalendarStartDate(solvedTask.getScheduledStartDate());
						task.setCalenderDuration(solvedTask.getCalenderDuration());
					}
				}
			}
			PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
			EntityController<ProjectTask> taskController = new EntityController<>(session.getServletContext());
			for (ProjectTask task : project.getProjectTasks()) {
				taskController.merge(task);
			}
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
		}

		
		
		return new ServerResponse("0" , "Success" , null);
	}
	
	
	public ServerResponse  getProjectCost( HttpSession session , int portfolioId , Date fromDate, Date toDate , int projectId ) {
		System.out.println("Inside getProjectCost()");
		return new ServerResponse("0" , "Success" , 10.0);
	}
} 
