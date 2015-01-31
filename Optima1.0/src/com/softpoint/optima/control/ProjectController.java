/**
 * 
 */
package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.WeekendDay;
import com.softpoint.optima.struct.ProjectPaymentDetail;
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
			int cityId , int provideId , int countryId , String postalCode , Date proposedStartDate, Date proposedFinishDate, double interestRate ,
			double overheadPerDay , int portfolioId , int clientId , int weekendDaysId ) throws OptimaException {
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
			int weekendDaysId ) throws OptimaException {
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
			double leftOverCost = PaymentUtil.getPortfolioLeftOverCost(portController , project.getPortfolio() , from, to);
			double openBalance = PaymentUtil.getPortfolioOpenBalance(session, project.getPortfolio() , from);
			double payment = PaymentUtil.getPortfolioPayment(from , to , paymentDetails , project.getPortfolio() , true);
			double financeLimit = PaymentUtil.getFinanceLimit(session , project.getPortfolio().getPortfolioId() , from);
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
			
			
			Map<Integer, TaskState> taskStates = initTaskState(project , logger);
			eligibleTasks = PaymentUtil.getEligibleTasks(project, from, to, true);
			int iteration = 0;
			while (!completed) {
				List<ProjectTask> currentEligibleSet = PaymentUtil.getEligibleTasks(project, from, to , true);
				double eligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(currentEligibleSet, from , to);	
				double eligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCost(currentEligibleSet, project, from, to);
				double expectedCashIn = PaymentUtil.getPortfolioPayment(to , next, paymentDetails, project.getPortfolio() , false);
				double totalCostCurrent = eligibleTasksCurrentPeroidCost; // - eligiableTaskFinanceCost;
				double leftOverNextCost = eligibleTasksLeftOverCost; // - nextPeriodLeftoverFinanceCost;
				
				logger.info(String.format("Current eligible tasks cost (current period): %f" ,  eligibleTasksCurrentPeroidCost ));
				logger.info(String.format("Current eligible tasks total cost: %f" ,  totalCostCurrent ));
				logger.info(String.format("Expected Cash: %f" ,  expectedCashIn ));
				logger.info(String.format("Current eligible tasks cost (leftover next period): %f" ,  eligibleTasksLeftOverCost ));
				logger.info(String.format("Current eligible tasks leftover total cost (leftover next period): %f" ,  leftOverNextCost ));
				
				solutionOutput.info(String.format("Selected: Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
						iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(currentEligibleSet), PaymentUtil.getTaskListStart(currentEligibleSet) , 
						PaymentUtil.getProjectLength(project , outputFormat , solutionOutput) , totalCostCurrent,
						cashAvailable - totalCostCurrent + expectedCashIn - leftOverNextCost, 
						totalCostCurrent <= cashAvailable && cashAvailable - totalCostCurrent + expectedCashIn >= leftOverNextCost?"Yes":"No",
								cashAvailable - totalCostCurrent < 0 ? 0: cashAvailable - totalCostCurrent )
						);
				
				
				if (totalCostCurrent <= cashAvailable && cashAvailable - totalCostCurrent + expectedCashIn >= leftOverNextCost ) {
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
						
						if (to.equals(calendar.getTime()) || to.after(calendar.getTime())) {
							noChange = false;
							task.setCalendarStartDate(calendar.getTime());
							PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
							updateScheduledState(project , taskStates);
						
							TaskSolution solution = new TaskSolution(task);
							List<ProjectTask> solutionCurrenteligibleTasks = PaymentUtil.getEligibleTasks(project, from, to , true);
							
							double solutionEligibleTasksCurrentPeroidCost =  PaymentUtil.getEligiableTaskCurrentPeriodCost(solutionCurrenteligibleTasks, from , to);	
							double solutionEligibleTasksLeftOverCost = PaymentUtil.getEligiableTaskLeftOverCost(solutionCurrenteligibleTasks, project, from, to);
							double solutionExpectedCashIn = PaymentUtil.getPortfolioPayment(from , to , paymentDetails, project.getPortfolio() , false); 
							double solutionTotalCostCurrent = solutionEligibleTasksCurrentPeroidCost;// - solutionEligiableTaskFinanceCost; 
							
							
							
							int projectLength = PaymentUtil.getProjectLength(project , outputFormat , solutionOutput);
							solution.setCurrentPeriodCost(solutionTotalCostCurrent);
							solution.setLeftOversCost(solutionEligibleTasksLeftOverCost); // - solutionNextPeriodLeftOverFinanceCost);
						
							solution.setIncome(solutionExpectedCashIn);
							solution.setProjectLength(projectLength);
							solution.setStartDate(calendar.getTime());
							
							solutionOutput.info(String.format("Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n" ,
									iteration, cashAvailable , PaymentUtil.getEligibaleTaskNameList(solutionCurrenteligibleTasks) , PaymentUtil.getTaskListStart(solutionCurrenteligibleTasks) , 
									projectLength , solutionTotalCostCurrent,
									cashAvailable - solutionTotalCostCurrent + solutionExpectedCashIn - solution.getLeftOversCost(), 
									solutionTotalCostCurrent <= cashAvailable && cashAvailable - solutionTotalCostCurrent + solutionExpectedCashIn >= solution.getLeftOversCost()?"Yes":"No",
									cashAvailable - solutionTotalCostCurrent < 0 ? 0 :cashAvailable - solutionTotalCostCurrent  )
									);
							solutions.add(solution);
							logger.info(String.format("Shifting task: %s , Project Length after shift: %d , New task start date: %s , Current Period Cost after shift:%f, Leftovers Cost after shift:%f" , task.getTaskName() , solution.getProjectLength() , solution.getStartDate().toString() , solution.getCurrentPeriodCost(), solution.getLeftOversCost()));
							task.setCalendarStartDate(taskDate);
							resetTaskState(project , taskStates);
							
						} 
					}
					if (noChange) {
						completed = true; // no tasks can be scheduled this period. Stop here
						logger.info("All tasks are out of period - please solve for next period");
					} else {
					
						TaskSolution nextIteration = PaymentUtil.findSolutionIteration(solutions, cashAvailable);
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
