package com.softpoint.optima.util.solution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import com.mysql.jdbc.BestResponseTimeBalanceStrategy;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.control.EntityController;
import com.softpoint.optima.control.EntityControllerException;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioFinance;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.struct.SolvedTask;
import com.softpoint.optima.util.PaymentUtil;
import com.softpoint.optima.util.ProjectSolutionDetails;
import com.softpoint.optima.util.TaskUtil;

public class PortfolioSolver {
	private static final String P2_START = "P2_START";
	private static final String PAYMENTS = "PAYMENTS";
	private static final String COMPLETED_TASKS = "COMPLETED_TASKS";
	private static final String DAYSSINCELASTREQUEST2 = "DAYSSINCELASTREQUEST";
//	private static final String LEFTOVERS = "LEFTOVERS";
	private static final String FEASIBLE = "FEASIBLE";
	private static final String P2_END = "P2_END";
//	private static final String DAYS_COMPLETED_FIRSTPERIOD = "DAYS_COMPLETED_FIRSTPERIOD";
	private static final String LEFTOVER_COST = "LEFTOVER_COST";
	private static final String P1_START = "P1_START";
	private static final String P1_END = "P1_END";
	
	Portfolio portfolio;
	List<ProjectWrapper> projects;
	List<ProjectWrapper> allProjects;
	Date currentPeriodStart;
	int finishedProjects;
	Map<ProjectWrapper, List<TaskTreeNode>> projectLeftovers;

	List<Date> financeList;
	List<BigDecimal> financeLimit;

	Map<Date, Double> payments;
	List<TaskTreeNode> leftOverTasks;
	Map<ProjectWrapper,Integer> numberOfDaysSinceLastRequest;
	
	public static class DayNumbers {
		double payment;
		double cost;
		double overhead;
		double finance;
	}

	public PortfolioSolver(Portfolio portfolio, String projectsPriority) {
		super();
		this.portfolio = portfolio;
		finishedProjects = 0;
		projects = new ArrayList<ProjectWrapper>();
		allProjects = new ArrayList<ProjectWrapper>();
		leftOverTasks = new ArrayList<TaskTreeNode>();
		numberOfDaysSinceLastRequest= new HashMap<ProjectWrapper,Integer>();
		
		List<Project> subProjects = portfolio.getProjects();
		Map<Integer, Project> projectsMap = new HashMap<Integer, Project>();
		for (Project p : subProjects) {
			projectsMap.put(p.getProjectId(), p);
		}

		String[] projectIds = projectsPriority.split(",");
		for (String temp : projectIds) {
			int id = Integer.valueOf(temp);
			Project p = projectsMap.get(id);
			if (p != null) {
				ProjectWrapper projectWrapper = new ProjectWrapper(p);
				projects.add(projectWrapper);
				allProjects.add(projectWrapper);
				numberOfDaysSinceLastRequest.put(projectWrapper, Integer.valueOf(0));
			}
		}

		financeList = new ArrayList<Date>();
		financeLimit = new ArrayList<BigDecimal>();
		List<PortfolioFinance> finances = portfolio.getPortfolioFinances();
		for (PortfolioFinance finance : finances) {
			Date fd = finance.getFinanceUntillDate();
			BigDecimal amount = finance.getFinanceAmount();

			int i = 0;
			for (i = 0; i < financeList.size(); i++) {
				if (fd.before(financeList.get(i))) {
					break;
				}
			}
			financeList.add(i, fd);
			financeLimit.add(i, amount);
		}

		payments = new HashMap<Date, Double>();
		// get advanced payments
		for (ProjectWrapper projectW : projects) {
			Project proj = projectW.getProject();
			BigDecimal advancedPayment = proj.getAdvancedPaymentAmount();
			addPayment(proj.getPropusedStartDate(), advancedPayment.doubleValue());
		}
		
		projectLeftovers = new HashMap<ProjectWrapper,List<TaskTreeNode>>();

	}

	// add a payment, if another payment exists in the same date (probably from
	// different project), combine them
	public void addPayment(Date date, double ammount) {
		payments.put(date, getPayment(date) + ammount);
	}
	public double getPayment(Date date) {
		if (payments.containsKey(date)) {
			return payments.get(date).doubleValue();
		}
		return 0;
	}
	public void removetPayment(Date date) {
		payments.remove(date);
	}
	public double getFinanceAtDate(Date date) {
		BigDecimal current = BigDecimal.ZERO;
		for (int i = 0; i < financeList.size(); i++) {
			if (date.before(financeList.get(i))) {
				current = financeLimit.get(i);
				break;
			} 
		}
		return current.doubleValue();
	}

	public void updateTask(EntityController<ProjectTask> taskController, TaskTreeNode task) throws EntityControllerException {
		Date d1 = task.getCalculatedTaskStart();
		Date d2 = task.getCalculatedTaskEnd();
		
		int diff = TaskUtil.daysBetween(d1, d2) + 1;
		if (task.task.getScheduledStartDate()==null || task.task.getCalendarStartDate()==null ||
				task.task.getScheduledStartDate().equals(d1) || task.task.getCalendarStartDate().equals(d1)) {
			task.task.setScheduledStartDate(d1);
			task.task.setCalendarStartDate(d1);
			task.task.setCalenderDuration(diff);
			taskController.merge(task.task);
			
		}
		for (TaskTreeNode child:task.getChildren()) {
			updateTask(taskController, child);
		}
	}
	
	public void commitSolution(EntityController<ProjectTask> taskController,ProjectWrapper project) {
		try {
			for (TaskTreeNode task:project.rootTasks) {
				updateTask(taskController, task);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
	}

	public String solveIt(HttpSession session) {
		Date startDate = null;

		// find the portfolio starting date
		for (ProjectWrapper pw : projects) {
			Project p = pw.getProject();
			Date d = p.getPropusedStartDate();
			if (startDate == null || d.before(startDate)) {
				startDate = d;
			}
		}
		currentPeriodStart = startDate;

		// the heart of the calculations,
		// it goes through the period then verify each project feasibility, and
		// it does the shifting if not feasible
		DayDetails dayDetails = new DayDetails();
/*		Double payment = payments.get(currentPeriodStart);
		if (payment != null) {
			dayDetails.setPayments(payment);
		}*/
		dayDetails.setFinance(getFinanceAtDate(currentPeriodStart));
		
		Map<ProjectWrapper,Double> totalIncome = new HashMap<ProjectWrapper,Double>();
		for (ProjectWrapper p:projects) {
			totalIncome.put(p, Double.valueOf(0));
		}
		int temp = 0;
		while (finishedProjects < projects.size()) {
			temp++;
			Date p1Start = currentPeriodStart;
			Date p1End = getPeriodEnd(p1Start);
			Date p2End = getPeriodEnd(TaskUtil.addDays(p1End, 1));

//			DayDetails p1EndDetails = new DayDetails();
//			DayDetails p2EndDefails = new DayDetails();
			int twoPeriodDuration = TaskUtil.daysBetween(p1Start, p2End);

			for (int projectIndex = 0; projectIndex < projects.size(); projectIndex++) {
				ProjectWrapper projectW = projects.get(projectIndex);
				DayDetails currentProjectDayDetails = new DayDetails(dayDetails);
				currentProjectDayDetails.setLeftOver((double) 0);

				// Add overhead from all next project, we dont add current or
				// previous cause they are already calculated
				double totalCashoutOther = 0;
				for (int i = projectIndex + 1; i < projects.size(); i++) {
					ProjectWrapper next = projects.get(i);
					
					DayDetails tmp = new DayDetails();
					Map<Date, Double> pamymentClone = cloneMap(payments);
					//fake call just to find the cash out
					List<TaskTreeNode> LO = projectLeftovers.get(next);
					List<TaskTreeNode> tmpLO = new ArrayList<TaskTreeNode>();
					if (LO!=null) {
						tmpLO.addAll(LO);
					}
					Map<String, Object> tmpResult = isValidPeriod(next, tmpLO, tmpLO, tmp,p1Start,p1End,p2End,0);
					payments = cloneMap(pamymentClone);
					
					tmp = (DayDetails) tmpResult.get(P1_END);
					totalCashoutOther += tmp.getOverhead() + tmp.getLeftOver();
				}
				currentProjectDayDetails.setOtherProjectsCashOut(totalCashoutOther);

				// list of left over tasks for this project
				List<TaskTreeNode> leftOverTasks = projectLeftovers.get(projectW);
				if (leftOverTasks==null) {
					leftOverTasks = new ArrayList<TaskTreeNode>();
				}

				List<TaskTreeNode> eligibleTasks = new ArrayList<TaskTreeNode>();
				getEligibleTasks(projectW, p1Start, p2End, eligibleTasks);

				currentProjectDayDetails.setPeriodIncome(totalIncome.get(projectW));
				Map<String, Object> details = getPeriodSolutionPerProject(projectW, eligibleTasks, leftOverTasks,
						currentProjectDayDetails,p1Start,p1End,p2End);
				if (details.get(FEASIBLE)==Boolean.FALSE) {
					return "FAILED"; //check if we can add more details
				}
				
				@SuppressWarnings("unchecked")
				List<TaskTreeNode> finishedTasks = (List<TaskTreeNode>) details.get(COMPLETED_TASKS);
				projectW.completedTasks.addAll(finishedTasks);
				for (TaskTreeNode completed:finishedTasks) {
					projectW.tasks.remove(completed);
					for (TaskTreeNode dep:completed.getChildren()) {
						if (!finishedTasks.contains(dep) && !projectW.tasks.contains(dep)) {
							projectW.tasks.add(dep);
						}
					}
				}
				@SuppressWarnings("unchecked")
				Map<Date,Double> pms = (Map<Date, Double>) details.get(PAYMENTS);
				if (pms!=null) {
					for (Date d:pms.keySet()) {
						Double v = pms.get(d);
						addPayment(d, v);
					}
				}

				List<TaskTreeNode> leftoverTasks = new ArrayList<TaskTreeNode>();
				for (TaskTreeNode tsk:leftOverTasks) {
					if (!tsk.getCalculatedTaskEnd().before(p1End) && !leftoverTasks.contains(tsk)) { // TODO verify if p1End is in the old or the new period
						leftoverTasks.add(tsk);
					}
				}
				for (TaskTreeNode tsk:eligibleTasks) {
					if (tsk.getCalculatedTaskStart().before(p1End) && !tsk.getCalculatedTaskEnd().before(p1End) && !leftoverTasks.contains(tsk)) { // TODO verify if p1End is in the old or the new period
						leftoverTasks.add(tsk);
					}
				}
				dayDetails = (DayDetails) details.get(P1_END);
				totalIncome.put(projectW,dayDetails.getPeriodIncome());
				
				dayDetails.setOverhead((double)0);
				dayDetails.setPeriodCost((double)0);
				projectLeftovers.put(projectW,leftoverTasks);
			}
			
			for (int i=projects.size()-1;i>-1;i--) {
				ProjectWrapper p = projects.get(i);
				if (p.tasks.size()==0) {
					finishedProjects++;
					projects.remove(p);
				}
			}

			currentPeriodStart = p1End;
		}
		EntityController<ProjectTask> taskController = new EntityController<>(session.getServletContext());
		for (ProjectWrapper p:allProjects) {
			commitSolution(taskController, p);
		}
		return "SOLVED";
	}

	Map<Date, Double> cloneMap(Map<Date,Double> original) {
		HashMap<Date,Double> clone = new HashMap<Date,Double>(original);
		return clone;
	}
	private Map<String, Object> getPeriodSolutionPerProject(ProjectWrapper projectW, List<TaskTreeNode> eligibleTasks,
			List<TaskTreeNode> leftOverTasks, DayDetails currentProjectDayDetails, Date p1Start, Date p1End, Date p2End) {
		DayDetails tempDayDetails = new DayDetails(currentProjectDayDetails);
		Map<Date, Double> pamymentClone = cloneMap(payments);
		Map<String, Object> result = isValidPeriod(projectW, eligibleTasks, leftOverTasks, tempDayDetails,p1Start,p1End,p2End,numberOfDaysSinceLastRequest.get(projectW));
		if (result.get(FEASIBLE)==Boolean.TRUE) {
			numberOfDaysSinceLastRequest.put(projectW, (Integer) result.get(DAYSSINCELASTREQUEST2));
			List<TaskTreeNode> newLeftOvers = new ArrayList<TaskTreeNode>();
			for (TaskTreeNode tsk:leftOverTasks) {
				if (!tsk.getCalculatedTaskEnd().before(p1End) && !newLeftOvers.contains(tsk)) { // TODO verify if p1End is in the old or the new period
					newLeftOvers.add(tsk);
				}
			}
			for (TaskTreeNode tsk:eligibleTasks) {
				if (tsk.getCalculatedTaskStart().before(p1End) && !tsk.getCalculatedTaskEnd().before(p1End) && !newLeftOvers.contains(tsk)) { // TODO verify if p1End is in the old or the new period
					newLeftOvers.add(tsk);
				}
			}
			
			projectLeftovers.put(projectW,newLeftOvers);

		} else {
			Map<String, Object> bestResult = null;
			//do the shifting
			int iterationIndex = 0;
			while (result.get(FEASIBLE)!=Boolean.TRUE) {
				boolean bestIsSet = false;
				boolean bestIsFeasible = false;
				
				Boolean first = true;
				int bestLength = Integer.MAX_VALUE;
				double bestP1Cost = 0;	
				Map<Date, Double> bestPamymentClone = null;
				boolean shiftHappens = false;
				TaskTreeNode shiftedTask = null;
				for (TaskTreeNode task:eligibleTasks) {
					//left overs doesn't move, and we dont need to push any task further than outside the period
					if (leftOverTasks.contains(task) || !task.getCalculatedTaskStart().before(p1End) || task.getCalculatedTaskStart().before(p1Start)) {
						continue;
					}
					if (!task.getCalculatedTaskStart().before(p1End)) {
						continue;
					}
					tempDayDetails = new DayDetails(currentProjectDayDetails);
					shiftHappens = true;
					int actualShift = task.shift(1);
					payments = cloneMap(pamymentClone);
					result = isValidPeriod(projectW, eligibleTasks, leftOverTasks, tempDayDetails,p1Start,p1End,p2End,numberOfDaysSinceLastRequest.get(projectW));
					boolean resultFeasible = result.get(FEASIBLE)==Boolean.TRUE;
					DayDetails p1EndDetails = (DayDetails) result.get(P1_END);
					Boolean newIsBetter = false;
					if (first) {
						first = false;
						bestIsFeasible = resultFeasible;
						bestLength = projectW.getProjectDuratoin();
						newIsBetter = true;
					} else {
						if (bestIsFeasible && !resultFeasible) {
							//do nothing, as there is a better option
						} else if (bestIsFeasible && resultFeasible) {
							int d2 = projectW.getProjectDuratoin();
							if (d2<bestLength) {
								bestLength = d2;
								newIsBetter = true;
							} else if (d2 == bestLength) {
								if (bestP1Cost < p1EndDetails.getPeriodCost()) {
									newIsBetter = true;
								} else if (bestP1Cost < p1EndDetails.getPeriodCost()) {
									if (task.getChildren().contains(shiftedTask)) {
										newIsBetter = true;
									}
								}
							}
							//compare which one is better
						} else if (resultFeasible) {
							newIsBetter = true;
							bestLength = projectW.getProjectDuratoin();
							bestIsFeasible = true;
						} else {
							//best is not feasible and result is not feasible, get the best of them for another round of shifting
							int d2 = projectW.getProjectDuratoin();
							if (d2<bestLength) {
								newIsBetter = true;
								bestLength = d2;
							} else if (d2 == bestLength) {
								if (bestP1Cost > p1EndDetails.getPeriodCost()) {
									newIsBetter = true;
								} else if (bestP1Cost == p1EndDetails.getPeriodCost()) {
									if (task.getChildren().contains(shiftedTask)) {
										newIsBetter = true;
									}
								}
							}
						}
					}
					if (newIsBetter) {
						shiftedTask = task;
						bestP1Cost = p1EndDetails.getPeriodCost();
						bestResult = result;
						bestPamymentClone = cloneMap(payments);
					}

					task.shift(-actualShift);
				}
				if (!shiftHappens) {
					break;
				}
				shiftedTask.shift(1);
				result = bestResult;
				Date ccc = eligibleTasks.get(3).getCalculatedTaskStart();
				numberOfDaysSinceLastRequest.put(projectW, (Integer) result.get(DAYSSINCELASTREQUEST2));
				payments = cloneMap(bestPamymentClone);
				iterationIndex++;
			}
			
		}
		return result;
	}
	
	Date getMaxProjectEnd(TaskTreeNode task) {
		if (task.getChildren().size()==0) {
			return task.getCalculatedTaskEnd();
		} else {
			Date ret = null;
			for (TaskTreeNode child:task.getChildren()) {
				Date d = getMaxProjectEnd(child);
				if (ret==null || ret.before(d)) {
					ret = d;
				}
			}
			return ret;
		}
	}
	
	Boolean isAfterProjectEnd(ProjectWrapper projectW,Date date) {
		for (TaskTreeNode tsk:projectW.getTasks()) {
			if (date.after(getMaxProjectEnd(tsk))) {
				return true;
			}
		}
		return false;
	}
	private Map<String,Object> isValidPeriod(ProjectWrapper projectW, List<TaskTreeNode> eligibleTasks,
			List<TaskTreeNode> leftOverTasks, DayDetails currentProjectDayDetails, Date p1Start, Date p1End, Date p2End, Integer daysSinceLastRequest) {
		Map<String,Object> results = new HashMap<String,Object>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(p1Start);
		Date psd = projectW.getProject().getPropusedStartDate();
		int requestPeriod = projectW.getProject().getPaymentRequestPeriod();
		int paymentPeriod = projectW.getProject().getCollectPaymentPeriod();
		 double advancedPercentage = projectW.getProject().getAdvancedPaymentPercentage().doubleValue();
		 double retainedPercentage = projectW.getProject().getRetainedPercentage().doubleValue();
		
		//Map to indicate how many days are done, this will be used to know if task is done or not and if a dependent can start or not
		//initialize with the temp days done, as it can varry as we shift the solution
		Set<TaskTreeNode> finishedTasks = new HashSet<TaskTreeNode>();
		
		HashSet<TaskTreeNode> startedInFirst = new HashSet<TaskTreeNode>();
		Map<Date,Double> payments = new HashMap<Date,Double>();
		results.put(PAYMENTS, payments);
		List<TaskTreeNode> completedInFirst = new ArrayList<TaskTreeNode>();
		results.put(COMPLETED_TASKS, completedInFirst);
		Boolean firstPeriod = true;
		Double leftOversForNextPeriod = (double)0;
		Double totalCostForNextPeriod = (double)0;
		DayDetails end1Detailes = null;
		DayDetails start2Detailes = null;
		for (Date date = p1Start; date.before(p2End); cal.add(Calendar.DATE,1), date = cal.getTime()) {
			if (date.compareTo(p1End)==0) {
				end1Detailes = new DayDetails(currentProjectDayDetails);
				results.put(P1_END, end1Detailes);
				firstPeriod = false;
				currentProjectDayDetails.setOverhead((double) 0);
				currentProjectDayDetails.setLeftOver((double) 0);
			}
			
			Boolean projectDone = isAfterProjectEnd(projectW,date);
			currentProjectDayDetails.setPayments(getPayment(date));
			currentProjectDayDetails.addBalance(currentProjectDayDetails.getPayments());
			if (firstPeriod) {
				removetPayment(date);
			}
			currentProjectDayDetails.setFinance(getFinanceAtDate(date));
			if (currentProjectDayDetails.getBalance()<0) {
				currentProjectDayDetails.setFinanceInterest(projectW.getProject().getInterestRate().doubleValue() * Math.abs(currentProjectDayDetails.getBalance()));
			}
			if (results.containsKey(P1_END) && !results.containsKey(P2_START)) {
				start2Detailes = new DayDetails(currentProjectDayDetails);
				results.put(P2_START, start2Detailes);
			}
			if (date.compareTo(p1Start)==0) {
				results.put(P1_START, new DayDetails(currentProjectDayDetails));
			}
			
			if (!projectDone && !date.before(psd)) {
				Double O = projectW.getProject().getOverheadPerDay().doubleValue();
				currentProjectDayDetails.addBalance(-O);
				currentProjectDayDetails.addOverhead(O);
			}
			// even if it is not feasible, we still calculate all the way to the end so we can find the best solution
			/*	double effectiveBalance = currentProjectDayDetails.getBalance() + currentProjectDayDetails.getPayments() -currentProjectDayDetails.getFinanceInterest() - currentProjectDayDetails.getOverhead()-currentProjectDayDetails.getLeftOver()-currentProjectDayDetails.getPenalty();
			if (effectiveBalance+currentProjectDayDetails.getFinance()<0) {
				return false;
			} */
			if (!TaskUtil.isWeekendDay(date, projectW.getProjectWeekends()) && !TaskUtil.isDayOff(date, projectW.getProjectVacations())) {
				Iterator<TaskTreeNode> taskIterator = leftOverTasks.iterator();
				Boolean iteratorInLeftOvers = true;
				if (!taskIterator.hasNext()) {
					taskIterator = eligibleTasks.iterator();
					iteratorInLeftOvers = false;
				}
				List<TaskTreeNode> addToCompleted = new ArrayList<TaskTreeNode>();
				while (taskIterator.hasNext()) {
					TaskTreeNode taskNode = taskIterator.next();
					if (!iteratorInLeftOvers && leftOverTasks.contains(taskNode)) {
						continue;
					}
					if (!finishedTasks.contains(taskNode)) {
						Boolean canStart = true;
						if (taskNode.getCalculatedTaskStart().after(date)) {
							canStart = false;
						} else {
							//verify if the task dependencies all already completed so it can start or not, if it is started already then no need to check as it must have been done already.
							for (TaskTreeNode parent:taskNode.getParents()) {
								if (finishedTasks.contains(parent)) {
									continue;
								}
								if (!parent.getCalculatedTaskEnd().before(date)) {
									canStart = false;
									break;
								}
							}
						}
						if (canStart) {
							if (firstPeriod) {
								startedInFirst.add(taskNode);
							}
							Boolean finished = taskNode.getCalculatedTaskEnd().equals(date);
							if (finished) {
								addToCompleted.add(taskNode);
								if (firstPeriod) {
									completedInFirst.add(taskNode);
								}
							}
							Double x = taskNode.getTask().getUniformDailyCost().doubleValue();
							if (!firstPeriod && startedInFirst.contains(taskNode)) {
								leftOversForNextPeriod += x;
							} else if (!firstPeriod) {
								totalCostForNextPeriod += x;
							}
							
							if (leftOverTasks.contains(taskNode)) {
								currentProjectDayDetails.addLeftOver(x);
							} else {
								currentProjectDayDetails.addPeriodCost(x);
							}
							currentProjectDayDetails.addBalance(-x);
							currentProjectDayDetails.addPeriodIncome(taskNode.getTask().getUniformDailyIncome().doubleValue());
						}
					}
					if (!taskIterator.hasNext() && iteratorInLeftOvers) {
						taskIterator = eligibleTasks.iterator();
						iteratorInLeftOvers = false;
					}
				}
				finishedTasks.addAll(addToCompleted);
				addToCompleted.clear();

			}
			if (firstPeriod) {
				daysSinceLastRequest ++;
				if (daysSinceLastRequest==requestPeriod && firstPeriod) {
					daysSinceLastRequest = 0;
					Calendar tempCalendar = Calendar.getInstance();
					tempCalendar.setTime(date);
					tempCalendar.add(Calendar.DATE,paymentPeriod+1);
					Date paymentDate = tempCalendar.getTime();
					Double paymentAmount = currentProjectDayDetails.getPeriodIncome();
					currentProjectDayDetails.addRetained(retainedPercentage * paymentAmount);
					currentProjectDayDetails.setPeriodIncome(Double.valueOf(0));
					paymentAmount = paymentAmount * (-retainedPercentage-advancedPercentage+1);
					payments.put(paymentDate,paymentAmount);
				}
			}
		}
		results.put(LEFTOVER_COST,leftOversForNextPeriod);
		results.put(P2_END, new DayDetails(currentProjectDayDetails));
		
		Boolean p1Feasible = (end1Detailes.getBalance() + end1Detailes.getFinance() - currentProjectDayDetails.getOtherProjectsCashOut()) >0;
		Boolean p2Feasible = ((currentProjectDayDetails.getBalance() + currentProjectDayDetails.getFinance() + totalCostForNextPeriod - currentProjectDayDetails.getOtherProjectsCashOut())) >0;
		Boolean feasible = p1Feasible && p2Feasible;
		results.put(FEASIBLE, feasible);
//		results.put(LEFTOVERS, completedDays);
		results.put(DAYSSINCELASTREQUEST2,daysSinceLastRequest);
		return results;
	}

	private void getEligibleTasks(ProjectWrapper projectW, Date p1Start, Date p2End, List<TaskTreeNode> eligibleTasks) {
		for (TaskTreeNode taskNode : projectW.tasks) {
			addEligibleTasks(taskNode, p1Start, p2End, eligibleTasks);
		}
	}

	void addEligibleTasks(TaskTreeNode taskNode, Date start, Date end, List<TaskTreeNode> tasks) {
		if (tasks.contains(taskNode)) {
			return;
		}
		Date taskStart = taskNode.getCalculatedTaskStart();
		if (taskStart.before(end)) {
			tasks.add(taskNode);
			for (TaskTreeNode child : taskNode.getChildren()) {
				addEligibleTasks(child, start, end, tasks);
			}
		}
	}

	private Date getPeriodEnd(Date periodStart) {
		Date periodEnd = null;
		for (ProjectWrapper pw : projects) {
			Project p = pw.getProject();
			Date psd = p.getPropusedStartDate();
			int requestPeriod = p.getPaymentRequestPeriod();
			int paymentPeriod = p.getCollectPaymentPeriod();

			if (!psd.after(periodStart)) {
				int daysToEnd = 0;
				int days = TaskUtil.daysBetween(psd, periodStart) + 1;
				if (days < requestPeriod) {
					daysToEnd = requestPeriod;
				} else {
					int temp = (int) Math.ceil(((double) (days - requestPeriod) / paymentPeriod));
					daysToEnd = requestPeriod + temp * paymentPeriod;
				}

				Date pe = TaskUtil.addDays(psd, daysToEnd);
				if (periodEnd == null || periodEnd.after(pe)) {
					periodEnd = pe;
				}
			} else {
				if (periodEnd == null || periodEnd.after(psd)) {
					periodEnd = psd;
				}
			}
		}
		for (Date financeDate : financeList) {
			if (financeDate.after(periodStart) && financeDate.before(periodEnd)) {
				periodEnd = financeDate;
			}
		}
		return periodEnd;
	}
}
