package com.softpoint.optima.util.solution;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.control.EntityController;
import com.softpoint.optima.control.EntityControllerException;
import com.softpoint.optima.control.ProjectController;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioFinance;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.util.PaymentUtil;
import com.softpoint.optima.util.PeriodLogGeneratorNew;
import com.softpoint.optima.util.ProjectSolutionDetails;
import com.softpoint.optima.util.TaskUtil;

public class PortfolioSolver {
	private static final String ERROR_MESSAGE = "ERROR_MESSAGE";
	private static final String SAVING = "SAVING";
	private static final String P2_START = "P2_START";
	private static final String PAYMENTS = "PAYMENTS";
	private static final String COMPLETED_TASKS = "COMPLETED_TASKS";
	private static final String DAYSSINCELASTREQUEST2 = "DAYSSINCELASTREQUEST";
	private static final String FEASIBLE = "FEASIBLE";
	private static final String P2_END = "P2_END";
	private static final String LEFTOVER_COST = "LEFTOVER_COST";
	private static final String P1_START = "P1_START";
	private static final String P1_PRE_START = "P1_PRE_START";
	private static final String P1_END = "P1_END";

	private String logLevel = null; // off , short, detailed

	private String getLogLevel() {
		if (logLevel == null) {
			logLevel = "detailed";
			Map<String, String> map = ProjectController.getSettingsMap(session);
			if (map != null && map.containsKey("loglevel")) {
				String l = map.get("loglevel");
				if (l.equals("off") || l.equals("short") || l.equals("detailed")) {
					logLevel = l;
				}
			}
		}
		return logLevel;
	}

	SimpleDateFormat fileNameDateFormatter = new SimpleDateFormat("yyyyMMdd_Hm");
	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM, yyyy");

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
	Map<ProjectWrapper, Integer> numberOfDaysSinceLastRequest;
	public static String STATUS_JSON = "{\"STATUS\":\"%s\",\"DONE\":%d,\"TOTAL\":%d,\"MESSAGE\":\"%s\",\"ERROR_MESSAGE\":\"%s\"}";
	public static final int MAX_RUNNING_SOLUTIONS = 1;
	public static ConcurrentMap<String, ConcurrentMap<String, Object>> currentWorkingSolutions = new ConcurrentHashMap<String, ConcurrentMap<String, Object>>();
	public static final String STARTING = "STARTING";
	public static final String TOTAL = "TOTAL";
	public static final String SOLVER = "SOLVER";
	public static final String DONE = "DONE";
	public static final String STATUS = "STATUS";
	public static final String SUCCESS = "Success";
	public static final Object MESSAGE = "MESSAGE";

	String timestamp;
	private HttpSession session;

	public static class DayNumbers {
		double payment;
		double cost;
		double overhead;
		double finance;
	}

	public PortfolioSolver(Portfolio portfolio, String projectsPriority, HttpSession session) {
		super();
		this.session = session;
		this.portfolio = portfolio;
		finishedProjects = 0;
		projects = new ArrayList<ProjectWrapper>();
		allProjects = new ArrayList<ProjectWrapper>();
		leftOverTasks = new ArrayList<TaskTreeNode>();
		numberOfDaysSinceLastRequest = new HashMap<ProjectWrapper, Integer>();

		List<Project> subProjects = null;
		if (portfolio != null) {
			subProjects = portfolio.getProjects();
		} else {
			String[] projectIds = projectsPriority.split(",");
			EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
			subProjects = new ArrayList<Project>();
			for (String temp : projectIds) {
				int id = Integer.valueOf(temp);
				try {
					Project proj = controller.find(Project.class, id);
					subProjects.add(proj);
				} catch (EntityControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

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
		List<PortfolioFinance> finances = null;
		if (portfolio != null) {
			finances = portfolio.getPortfolioFinances();
		} else {
			finances = allProjects.get(0).getProject().getPortfolioFinances();
		}
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
			double advancedPayment = ProjectSolutionDetails.getAdvancedPaymentAmmount(proj);
			addPayment(proj.getPropusedStartDate(), advancedPayment);
		}

		projectLeftovers = new HashMap<ProjectWrapper, List<TaskTreeNode>>();

		timestamp = fileNameDateFormatter.format(new Date());
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

	public void updateTask(EntityController<ProjectTask> taskController, TaskTreeNode task, Set<TaskTreeNode> commitedTasks) throws EntityControllerException {
		if (commitedTasks.contains(task)) {
			return;
		}
		commitedTasks.add(task);
		Date d1 = task.getCalculatedTaskStart();
		Date d2 = task.getCalculatedTaskEnd();

		int diff = TaskUtil.daysBetween(d1, d2) + 1;
		if (task.task.getScheduledStartDate() == null || task.task.getCalendarStartDate() == null || !task.task.getScheduledStartDate().equals(d1) || !task.task.getCalendarStartDate().equals(d1)) {
			task.task.setScheduledStartDate(d1);
			task.task.setCalendarStartDate(d1);
			task.task.setCalenderDuration(diff);
			taskController.mergeTransactionMerge(task.task);

		}
		for (TaskTreeNode child : task.getChildren()) {
			updateTask(taskController, child, commitedTasks);
		}
	}

	public void commitSolution(EntityController<ProjectTask> taskController, ProjectWrapper project, ConcurrentMap<String, Object> solStatus) {
		try {
			Integer i = (Integer) solStatus.get(DONE);
			if (i == null) {
				i = 0;
			}
			Set<TaskTreeNode> commitedTasks = new HashSet<TaskTreeNode>();
			for (TaskTreeNode task : project.rootTasks) {
				i++;
				solStatus.put(DONE, i);
				updateTask(taskController, task, commitedTasks);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
	}

	private String getSolutionKey() {
		if (portfolio != null) {
			return "Port" + portfolio.getPortfolioId();
		} else if (projects.size() == 1) {
			return "Proj" + projects.get(0).getProject().getProjectId();
		} else {
			return "";
		}
	}

	public String solveIt() {
		Date startDate = null;
		int totalTask = 0;
		// find the portfolio starting date
		for (ProjectWrapper pw : projects) {
			Project p = pw.getProject();
			totalTask += pw.getTotalTasks();
			Date d = p.getPropusedStartDate();
			if (startDate == null || d.before(startDate)) {
				startDate = d;
			}
		}

		if (!PortfolioSolver.currentWorkingSolutions.containsKey(getSolutionKey())) {
			ConcurrentMap<String, Object> solStatus = new ConcurrentHashMap<String, Object>();
			PortfolioSolver.currentWorkingSolutions.put(getSolutionKey(), solStatus);
		}

		ConcurrentMap<String, Object> solStatus = PortfolioSolver.currentWorkingSolutions.get(getSolutionKey());
		try {
			solStatus.put(STATUS, "RUNNING");
			solStatus.put(TOTAL, totalTask);

			currentPeriodStart = startDate;

			// the heart of the calculations,
			// it goes through the period then verify each project feasibility,
			// and
			// it does the shifting if not feasible
			DayDetails dayDetails = new DayDetails();
			/*
			 * Double payment = payments.get(currentPeriodStart); if (payment != null) { dayDetails.setPayments(payment); }
			 */
			dayDetails.setFinance(getFinanceAtDate(currentPeriodStart));

			Map<ProjectWrapper, Double> totalIncome = new HashMap<ProjectWrapper, Double>();
			for (ProjectWrapper p : projects) {
				totalIncome.put(p, Double.valueOf(0));
			}
			int totalDone = 0;
			int totalNumberOfProjects = projects.size();
			while (finishedProjects < totalNumberOfProjects) {
				Date p1Start = currentPeriodStart;
				Date p1End = getPeriodEnd(p1Start);
				Date p2End = getPeriodEnd(TaskUtil.addDays(p1End, 1));

				// DayDetails p1EndDetails = new DayDetails();
				// DayDetails p2EndDefails = new DayDetails();
				// int twoPeriodDuration = TaskUtil.daysBetween(p1Start, p2End);

				for (int projectIndex = 0; projectIndex < projects.size(); projectIndex++) {
					ProjectWrapper projectW = projects.get(projectIndex);
					DayDetails currentProjectDayDetails = new DayDetails(dayDetails);
					currentProjectDayDetails.setLeftOver((double) 0);

					// Add overhead from all next project, we dont add current
					// or
					// previous cause they are already calculated
					double totalCashoutOther = 0;
					double totalCashoutOtherNext = 0;
					for (int i = 0; i < projectIndex; i++) {
						ProjectWrapper next = projects.get(i);

						DayDetails tmp = new DayDetails();
						Map<Date, Double> pamymentClone = cloneMap(payments);
						// fake call just to find the cash out
						List<TaskTreeNode> LO = projectLeftovers.get(next);
						List<TaskTreeNode> tmpLO = new ArrayList<TaskTreeNode>();
						if (LO != null) {
							tmpLO.addAll(LO);
						}
						Map<String, Object> tmpResult = isValidPeriod(next, tmpLO, tmpLO, tmp, p1Start, p1End, p2End, 0);
						payments = cloneMap(pamymentClone);

						tmp = (DayDetails) tmpResult.get(P2_END);
						totalCashoutOtherNext += tmp.getOverhead() + (Double) tmpResult.get(LEFTOVER_COST);
					}
					for (int i = projectIndex + 1; i < projects.size(); i++) {
						ProjectWrapper next = projects.get(i);

						DayDetails tmp = new DayDetails();
						Map<Date, Double> pamymentClone = cloneMap(payments);
						// fake call just to find the cash out
						List<TaskTreeNode> LO = projectLeftovers.get(next);
						List<TaskTreeNode> tmpLO = new ArrayList<TaskTreeNode>();
						if (LO != null) {
							tmpLO.addAll(LO);
						}
						Map<String, Object> tmpResult = isValidPeriod(next, tmpLO, tmpLO, tmp, p1Start, p1End, p2End, 0);
						payments = cloneMap(pamymentClone);

						tmp = (DayDetails) tmpResult.get(P1_END);
						totalCashoutOther += tmp.getOverhead() + tmp.getLeftOver();

						tmp = (DayDetails) tmpResult.get(P2_END);
						totalCashoutOtherNext += tmp.getOverhead() + (Double) tmpResult.get(LEFTOVER_COST);
					}
					currentProjectDayDetails.setOtherProjectsCashOut(totalCashoutOther);
					currentProjectDayDetails.setOtherProjectsCashOutNext(totalCashoutOtherNext);

					// list of left over tasks for this project
					List<TaskTreeNode> leftOverTasks = projectLeftovers.get(projectW);
					if (leftOverTasks == null) {
						leftOverTasks = new ArrayList<TaskTreeNode>();
					}

					List<TaskTreeNode> eligibleTasks = new ArrayList<TaskTreeNode>();
					getEligibleTasks(projectW, p1Start, p2End, eligibleTasks);

					currentProjectDayDetails.setPeriodIncome(totalIncome.get(projectW));
					Map<String, Object> details = getPeriodSolutionPerProject(projectW, eligibleTasks, leftOverTasks, currentProjectDayDetails, p1Start, p1End, p2End);
					if (details.get(FEASIBLE) == Boolean.FALSE) {
						solStatus.remove(SOLVER);
						solStatus.put(STATUS, "FAILED");
						solStatus.put(ERROR_MESSAGE, details.get(ERROR_MESSAGE));
						solStatus.put(DONE, totalTask);

						return "FAILED"; // check if we can add more details
					}

					@SuppressWarnings("unchecked")
					List<TaskTreeNode> finishedTasks = (List<TaskTreeNode>) details.get(COMPLETED_TASKS);
					projectW.completedTasks.addAll(finishedTasks);
					for (TaskTreeNode completed : finishedTasks) {
						totalDone++;
						projectW.tasks.remove(completed);
						for (TaskTreeNode dep : completed.getChildren()) {
							if (!finishedTasks.contains(dep) && !projectW.tasks.contains(dep)) {
								projectW.tasks.add(dep);
							}
						}
					}
					/*
					 * try { Thread.sleep(2000); } catch (InterruptedException e) { }
					 */
					solStatus.put(DONE, totalDone);
					@SuppressWarnings("unchecked")
					Map<Date, Double> pms = (Map<Date, Double>) details.get(PAYMENTS);
					if (pms != null) {
						for (Date d : pms.keySet()) {
							Double v = pms.get(d);
							addPayment(d, v);
						}
					}

					List<TaskTreeNode> leftoverTasks = new ArrayList<TaskTreeNode>();
					for (TaskTreeNode tsk : leftOverTasks) {
						if (!tsk.getCalculatedTaskEnd().before(p1End) && !leftoverTasks.contains(tsk)) {
							leftoverTasks.add(tsk);
						}
					}
					for (TaskTreeNode tsk : eligibleTasks) {
						if (tsk.getCalculatedTaskStart().before(p1End) && !tsk.getCalculatedTaskEnd().before(p1End) && !leftoverTasks.contains(tsk)) {
							leftoverTasks.add(tsk);
						}
					}
					dayDetails = (DayDetails) details.get(P1_END);
					totalIncome.put(projectW, dayDetails.getPeriodIncome());

					dayDetails.setOverhead((double) 0);
					dayDetails.setPeriodCost((double) 0);
					projectLeftovers.put(projectW, leftoverTasks);
				}

				for (int i = projects.size() - 1; i > -1; i--) {
					ProjectWrapper p = projects.get(i);
					if (p.tasks.size() == 0) {
						finishedProjects++;
						projects.remove(p);
					}
				}

				currentPeriodStart = p1End;
			}
			solStatus.put(STATUS, SAVING);
			solStatus.put(DONE, 0);

			EntityController<ProjectTask> taskController = new EntityController<>(session.getServletContext());
			try {
				taskController.mergeTransactionStart();
				for (ProjectWrapper p : allProjects) {
					commitSolution(taskController, p, solStatus);
				}
				taskController.mergeTransactionClose();
			} catch (EntityControllerException e) {
			}

			try {
				if (portfolio != null) {
					EntityController<Portfolio> paymentController = new EntityController<Portfolio>(session.getServletContext());
					String query = "update portfolio set portfolio.solve_date=now() where portfolio.portfolio_id=?1";
					paymentController.nativeUpdate(query, portfolio.getPortfolioId());
				} else {
					EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
					for (ProjectWrapper p : allProjects) {
						String query = "update project set project.solve_date=now() where project.project_id=?1";
						controller.nativeUpdate(query, p.getProject().getProjectId());
					}
				}
			} catch (Exception e) {

			}
			solStatus.remove(SOLVER);
			solStatus.put(STATUS, SUCCESS);
			solStatus.put(DONE, totalTask);

			try {
				new Thread() {
					public void run() {
						for (ProjectWrapper p : allProjects) {
							ProjectSolutionDetails details = new ProjectSolutionDetails(false, p.getProject());
							details.savePaymentToDB(session);
						}
					}
				}.start();
			} catch (Exception e) {

			}

			return "SOLVED";
		} finally {
			// for any unexpected return
			if (solStatus.containsKey(SOLVER)) {
				solStatus.remove(SOLVER);
				solStatus.put(STATUS, "FAILED");
			}
		}
	}

	Map<Date, Double> cloneMap(Map<Date, Double> original) {
		HashMap<Date, Double> clone = new HashMap<Date, Double>(original);
		return clone;
	}

	Map<ProjectWrapper, Integer> cloneP2IMap(Map<ProjectWrapper, Integer> original) {
		HashMap<ProjectWrapper, Integer> clone = new HashMap<ProjectWrapper, Integer>(original);
		return clone;
	}

	private Map<String, Object> getPeriodSolutionPerProject(ProjectWrapper projectW, List<TaskTreeNode> eligibleTasks, List<TaskTreeNode> leftOverTasks, DayDetails currentProjectDayDetails, Date p1Start, Date p1End, Date p2End) {
		DayDetails tempDayDetails = new DayDetails(currentProjectDayDetails);
		Map<Date, Double> pamymentClone = cloneMap(payments);
		Map<String, Object> result = isValidPeriod(projectW, eligibleTasks, leftOverTasks, tempDayDetails, p1Start, p1End, p2End, numberOfDaysSinceLastRequest.get(projectW));
		PeriodLogGeneratorNew logGenerator = null;
		String shortVersion = "";
		int iterationIndex = 0;
		if (getLogLevel().equals("short") || getLogLevel().equals("detailed")) {
			logGenerator = new PeriodLogGeneratorNew(session.getServletContext(), projectW.getProject().getProjectCode() + "_" + timestamp, dateFormatter.format(p1Start), dateFormatter.format(p1End));
			logGenerator.setProject(portfolio == null ? "" : portfolio.getPortfolioName(), projectW.getProject().getProjectCode() + "-" + projectW.getProject().getProjectName());
			if (result.get(FEASIBLE) == Boolean.TRUE) {
				shortVersion = getShortVersion(result, projectW, eligibleTasks, p1Start, p1End, iterationIndex, null);
				Date psd = projectW.getProject().getPropusedStartDate();
				Date ped = TaskUtil.addDays(psd, projectW.getProjectDuratoin());
				writeTrialToHTMLLogFile(logGenerator, iterationIndex, shortVersion, p1Start, p1End, projectW, ped, result, null);
			}
		}
		if (result.get(FEASIBLE) == Boolean.TRUE) {
			numberOfDaysSinceLastRequest.put(projectW, (Integer) result.get(DAYSSINCELASTREQUEST2));
			List<TaskTreeNode> newLeftOvers = new ArrayList<TaskTreeNode>();
			for (TaskTreeNode tsk : leftOverTasks) {
				if (!tsk.getCalculatedTaskEnd().before(p1End) && !newLeftOvers.contains(tsk)) {
					newLeftOvers.add(tsk);
				}
			}
			for (TaskTreeNode tsk : eligibleTasks) {
				if (tsk.getCalculatedTaskStart().before(p1End) && !tsk.getCalculatedTaskEnd().before(p1End) && !newLeftOvers.contains(tsk)) {
					newLeftOvers.add(tsk);
				}
			}

			projectLeftovers.put(projectW, newLeftOvers);

		} else {
			Integer nod = numberOfDaysSinceLastRequest.get(projectW);
			Map<String, Object> bestResult = null;

			TaskTreeNode shiftedTask = null;
			// do the shifting
			while (result.get(FEASIBLE) != Boolean.TRUE) {
				if (logGenerator != null) {
					shortVersion = getShortVersion(result, projectW, eligibleTasks, p1Start, p1End, iterationIndex, null);
					shortVersion = shortVersion.substring(0, 3) + "Selected: " + shortVersion.substring(3);
					Date psd = projectW.getProject().getPropusedStartDate();
					Date ped = TaskUtil.addDays(psd, projectW.getProjectDuratoin());
					writeTrialToHTMLLogFile(logGenerator, iterationIndex, shortVersion, p1Start, p1End, projectW, ped, result, null);
				}

				boolean bestIsFeasible = false;

				Boolean first = true;
				int bestLength = Integer.MAX_VALUE;
				double bestP1Cost = 0;
				Map<Date, Double> bestPamymentClone = null;
				boolean shiftHappens = false;
				shiftedTask = null;
				for (TaskTreeNode task : eligibleTasks) {

					// left overs doesn't move, and we dont need to push any
					// task further than outside the period
					if (leftOverTasks.contains(task) || !task.getCalculatedTaskStart().before(p1End) || task.getCalculatedTaskStart().before(p1Start) || task.getTask().getStatus() != ProjectTask.STATUS_NOT_STARTED) {
						continue;
					}
					if (!task.getCalculatedTaskStart().before(p1End)) {
						continue;
					}
					tempDayDetails = new DayDetails(currentProjectDayDetails);
					shiftHappens = true;
					int actualShift = task.shift(1);
					payments = cloneMap(pamymentClone);
					result = isValidPeriod(projectW, eligibleTasks, leftOverTasks, tempDayDetails, p1Start, p1End, p2End, nod);

					if (logGenerator != null) {
						shortVersion = getShortVersion(result, projectW, eligibleTasks, p1Start, p1End, iterationIndex, task);
						Date psd = projectW.getProject().getPropusedStartDate();
						Date ped = TaskUtil.addDays(psd, projectW.getProjectDuratoin());
						writeTrialToHTMLLogFile(logGenerator, iterationIndex, shortVersion, p1Start, p1End, projectW, ped, result, task);
					}

					boolean resultFeasible = result.get(FEASIBLE) == Boolean.TRUE;
					DayDetails p1EndDetails = (DayDetails) result.get(P1_END);
					Boolean newIsBetter = false;
					if (first) {
						first = false;
						bestIsFeasible = resultFeasible;
						bestLength = projectW.getProjectDuratoin();
						newIsBetter = true;
					} else {
						// our best bet is if the solution will take shorter
						int d2 = projectW.getProjectDuratoin();
						if (d2 > bestLength) {
							// if the new option is taking longer, then ignore it
						} else if (d2 < bestLength) {
							bestLength = d2;
							newIsBetter = true;
							bestIsFeasible = resultFeasible;
						} else {
							if (bestIsFeasible && !resultFeasible) {
								// if equal duration but best is feasible, then ifnore the current option
							} else if (bestIsFeasible && resultFeasible) {
								if (bestP1Cost < p1EndDetails.getPeriodCost()) {
									newIsBetter = true;
									bestIsFeasible = resultFeasible;
								} else if (bestP1Cost < p1EndDetails.getPeriodCost()) {
									if (task.getChildren().contains(shiftedTask)) {
										newIsBetter = true;
										bestIsFeasible = resultFeasible;
									}
								}
							} else if (!bestIsFeasible && !resultFeasible) {
								if (bestP1Cost > p1EndDetails.getPeriodCost()) {
									newIsBetter = true;
									bestIsFeasible = resultFeasible;
								} else if (bestP1Cost == p1EndDetails.getPeriodCost()) {
									if (task.getChildren().contains(shiftedTask)) {
										newIsBetter = true;
										bestIsFeasible = resultFeasible;
									}
								}
							} else {
								newIsBetter = true;
								bestIsFeasible = resultFeasible;
							}
						}

						/*
						 * if (bestIsFeasible && !resultFeasible) { // do nothing, as there is a better option } else if (bestIsFeasible && resultFeasible) { int d2 = projectW.getProjectDuratoin(); if (d2 < bestLength) { bestLength = d2;
						 * newIsBetter = true; } else if (d2 == bestLength) { if (bestP1Cost < p1EndDetails.getPeriodCost()) { newIsBetter = true; } else if (bestP1Cost < p1EndDetails.getPeriodCost()) { if
						 * (task.getChildren().contains(shiftedTask)) { newIsBetter = true; } } } // compare which one is better } else if (resultFeasible) { newIsBetter = true; bestLength = projectW.getProjectDuratoin(); bestIsFeasible =
						 * true; } else { // best is not feasible and result is not feasible, // get the best of them for another round of // shifting if (d2 < bestLength) { newIsBetter = true; bestLength = d2; } else if (d2 == bestLength)
						 * { if (bestP1Cost > p1EndDetails.getPeriodCost()) { newIsBetter = true; } else if (bestP1Cost == p1EndDetails.getPeriodCost()) { if (task.getChildren().contains(shiftedTask)) { newIsBetter = true; } } } }
						 */
					}
					if (newIsBetter) {
						shiftedTask = task;
						bestP1Cost = p1EndDetails.getPeriodCost();
						bestResult = result;
						bestPamymentClone = cloneMap(payments);
					}

					task.shift(-actualShift);
				}

				if (bestResult != null) {
					result = bestResult;
				}
				if (!shiftHappens) {
					break;
				}
				shiftedTask.shift(1);
				payments = cloneMap(bestPamymentClone);
				iterationIndex++;
			}
			if (bestResult != null) {
				numberOfDaysSinceLastRequest.put(projectW, (Integer) bestResult.get(DAYSSINCELASTREQUEST2));
			}

			if (logGenerator != null && shiftedTask != null) {
				shortVersion = getShortVersion(result, projectW, eligibleTasks, p1Start, p1End, iterationIndex, shiftedTask);
				Date psd = projectW.getProject().getPropusedStartDate();
				/* Date ped = */TaskUtil.addDays(psd, projectW.getProjectDuratoin());
				writeShortVersionToHTMLLogFile(logGenerator, iterationIndex, shortVersion);
			}

		}
		if (logGenerator != null) {
			logGenerator.flushFile();
		}
		return result;
	}

	private String getShortVersion(Map<String, Object> result, ProjectWrapper project, List<TaskTreeNode> eligibleTasks, Date p1Start, Date p1End, int iteration, TaskTreeNode shifterTask) {
		String tasks = "";
		String dates = "";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM");

		for (TaskTreeNode task : eligibleTasks) {
			Date date = task.getCalculatedTaskStart();
			if (date.before(p1Start) || !date.before(p1End)) {
				continue;
			}
			if (!tasks.isEmpty()) {
				tasks += ",";
			}
			if (task == shifterTask) {
				tasks += ">>";
			}
			tasks += task.getTask().getTaskName();
			if (!dates.isEmpty()) {
				dates += ",";
			}
			dates += df.format(date);
		}
		int d = project.getProjectDuratoin();
		DayDetails preStart = (DayDetails) result.get(P1_PRE_START);
		DayDetails p1StartDetails = (DayDetails) result.get(P1_START);
		DayDetails p1EndDetails = (DayDetails) result.get(P1_END);
		DayDetails p2StartDetails = (DayDetails) result.get(P2_START);
		DayDetails p2EndDetails = (DayDetails) result.get(P2_END);
		double rc = (preStart.getBalance() + p1StartDetails.getFinance() + p1StartDetails.getPayments() - p1EndDetails.getOverhead() - p1EndDetails.getLeftOver() - p1EndDetails.getOtherProjectsCashOut());
		double rn = (p1EndDetails.getBalance() + p2StartDetails.getFinance() + p2StartDetails.getPayments() - p2EndDetails.getOverhead() - (Double) result.get(LEFTOVER_COST) - p2EndDetails.getOtherProjectsCashOutNext()
				- p1EndDetails.getOtherProjectsCashOut());
		double cc = p1EndDetails.getPeriodCost();
		String SHORT_TEMPLATE = "<p>Iteration [%d] Activities:[%s] Start [%s] Project Duration: [%d] R[Current]=[%.2f] C[Current]=[%.2f] R[NEXT]=[%.2f] Remaining Cash=[%.2f] Feasible: %s</p>";
		String f = (result.get(FEASIBLE) == Boolean.TRUE) ? "YES" : "NO";
		return String.format(SHORT_TEMPLATE, iteration, tasks, dates, d, rc, cc, rn, rc - cc, f);
	}

	Date getMaxProjectEnd(ProjectWrapper p) {
		Date maxDate = null;
		for (TaskTreeNode tsk : p.getAllTasks()) {
			if (maxDate == null || maxDate.before(tsk.getCalculatedTaskEnd())) {
				maxDate = tsk.getCalculatedTaskEnd();
			}
		}
		return maxDate;
		/*
		 * if (maxDate!=null) { return date.after(maxDate); } if (task.getChildren().size() == 0) { return task.getCalculatedTaskEnd(); } else { Date ret = null; for (TaskTreeNode child : task.getChildren()) { Date d =
		 * getMaxProjectEnd(child); if (ret == null || ret.before(d)) { ret = d; } } return ret; }
		 */ }

	Boolean isAfterProjectEnd(ProjectWrapper projectW, Date date) {
		// for (TaskTreeNode tsk : projectW.getRootTasks()) {
		if (date.after(getMaxProjectEnd(projectW))) {
			return true;
		}
		// }
		return false;
	}

	private Map<String, Object> isValidPeriod(ProjectWrapper projectW, List<TaskTreeNode> eligibleTasks, List<TaskTreeNode> leftOverTasks, DayDetails currentProjectDayDetails, Date p1Start, Date p1End, Date p2End,
			Integer daysSinceLastRequest) {
		Map<String, Object> results = new HashMap<String, Object>();
		results.put(P1_PRE_START, new DayDetails(currentProjectDayDetails));
		Calendar cal = Calendar.getInstance();
		cal.setTime(p1Start);
		Date psd = projectW.getProject().getPropusedStartDate();
		Date pfd = projectW.getProject().getProposedFinishDate();
		int requestPeriod = projectW.getProject().getPaymentRequestPeriod();
		int paymentPeriod = projectW.getProject().getCollectPaymentPeriod();
		double advancedPercentage = projectW.getProject().getAdvancedPaymentPercentage().doubleValue();
		double retainedPercentage = projectW.getProject().getRetainedPercentage().doubleValue();

		// Map to indicate how many days are done, this will be used to know if
		// task is done or not and if a dependent can start or not
		// initialize with the temp days done, as it can varry as we shift the
		// solution
		Set<TaskTreeNode> finishedTasks = new HashSet<TaskTreeNode>();

		HashSet<TaskTreeNode> startedInFirst = new HashSet<TaskTreeNode>();
		Map<Date, Double> payments = new HashMap<Date, Double>();
		results.put(PAYMENTS, payments);
		List<TaskTreeNode> completedInFirst = new ArrayList<TaskTreeNode>();
		results.put(COMPLETED_TASKS, completedInFirst);
		Boolean firstPeriod = true;
		Double leftOversForNextPeriod = (double) 0;
		Double totalCostForNextPeriod = (double) 0;
		DayDetails end1Detailes = null;
		DayDetails start2Detailes = null;
		for (Date date = p1Start; date.before(p2End); cal.add(Calendar.DATE, 1), date = cal.getTime()) {
			if (date.compareTo(p1End) == 0) {
				end1Detailes = new DayDetails(currentProjectDayDetails);
				results.put(P1_END, end1Detailes);
				firstPeriod = false;
				currentProjectDayDetails.setOverhead((double) 0);
				currentProjectDayDetails.setLeftOver((double) 0);
			}

			Boolean projectDone = isAfterProjectEnd(projectW, date);
			if (projectDone && !firstPeriod) {
				for (TaskTreeNode tsk : projectW.getAllTasks()) {
					if (!tsk.getCalculatedTaskStart().before(p1End)) {
						projectDone = false;
						break;
					}
				}
			}
			currentProjectDayDetails.setPayments(getPayment(date));
			currentProjectDayDetails.addBalance(currentProjectDayDetails.getPayments());
			if (firstPeriod) {
				removetPayment(date);
			}
			currentProjectDayDetails.setFinance(getFinanceAtDate(date));
			if (currentProjectDayDetails.getBalance() < 0) {
				currentProjectDayDetails.setFinanceInterest(PaymentUtil.getInterestInDay(projectW.getProject(), date) * Math.abs(currentProjectDayDetails.getBalance()));
			}
			if (results.containsKey(P1_END) && !results.containsKey(P2_START)) {
				start2Detailes = new DayDetails(currentProjectDayDetails);
				results.put(P2_START, start2Detailes);
			}
			if (date.compareTo(p1Start) == 0) {
				results.put(P1_START, new DayDetails(currentProjectDayDetails));
			}

			if (!projectDone && !date.before(psd)) {
				Double O = projectW.getProject().getOverheadPerDay().doubleValue();
				currentProjectDayDetails.addBalance(-O);
				currentProjectDayDetails.addOverhead(O);
			}
			
			if (!projectDone && pfd!=null && date.after(pfd)) {
				Double p = projectW.getProject().getDelayPenaltyAmount().doubleValue();
				currentProjectDayDetails.addBalance(-p);
			}
			// even if it is not feasible, we still calculate all the way to the
			// end so we can find the best solution
			/*
			 * double effectiveBalance = currentProjectDayDetails.getBalance() + currentProjectDayDetails.getPayments() -currentProjectDayDetails.getFinanceInterest() - currentProjectDayDetails.getOverhead()-currentProjectDayDetails.
			 * getLeftOver()-currentProjectDayDetails.getPenalty(); if (effectiveBalance+currentProjectDayDetails.getFinance()<0) { return false; }
			 */
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
							// verify if the task dependencies all already
							// completed so it can start or not, if it is
							// started already then no need to check as it must
							// have been done already.
							for (TaskTreeNode parent : taskNode.getParents()) {
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
				if (!date.before(projectW.getProject().getPropusedStartDate())) {
					daysSinceLastRequest++;
				}
				if (daysSinceLastRequest == requestPeriod && firstPeriod) {
					daysSinceLastRequest = 0;
					Calendar tempCalendar = Calendar.getInstance();
					tempCalendar.setTime(date);
					tempCalendar.add(Calendar.DATE, paymentPeriod + 1);
					Date paymentDate = tempCalendar.getTime();
					Double paymentAmount = currentProjectDayDetails.getPeriodIncome();
					currentProjectDayDetails.addRetained(retainedPercentage * paymentAmount);
					currentProjectDayDetails.setPeriodIncome(Double.valueOf(0));
					paymentAmount = paymentAmount * (-retainedPercentage - advancedPercentage + 1);
					payments.put(paymentDate, paymentAmount);
				}
			}
		}
		results.put(LEFTOVER_COST, leftOversForNextPeriod);
		results.put(P2_END, new DayDetails(currentProjectDayDetails));

		double p1Diff = end1Detailes.getBalance() + end1Detailes.getFinance() - currentProjectDayDetails.getOtherProjectsCashOut();
		Boolean p1Feasible = p1Diff >= 0.0;
		double p2Diff = (currentProjectDayDetails.getBalance() + currentProjectDayDetails.getFinance() + totalCostForNextPeriod - currentProjectDayDetails.getOtherProjectsCashOut() - currentProjectDayDetails.getOtherProjectsCashOutNext());
		Boolean p2Feasible = p2Diff >= 0.0;
		Boolean feasible = p1Feasible && p2Feasible;
		String msg1 = "Planning from %s to %s for project (%s), Short (%.2f)$ to cover the minimum required expenses.";
		if (!p1Feasible) {
			String error = String.format(msg1, dateFormatter.format(p1Start), dateFormatter.format(p1End), projectW.getProject().getProjectCode(), -p1Diff);
			results.put(ERROR_MESSAGE, error);
		} else if (!p2Feasible) {
			String error = String.format(msg1, dateFormatter.format(p1End), dateFormatter.format(p2End), projectW.getProject().getProjectCode(), -p2Diff);
			results.put(ERROR_MESSAGE, error);
		}

		results.put(FEASIBLE, feasible);
		// results.put(LEFTOVERS, completedDays);
		results.put(DAYSSINCELASTREQUEST2, daysSinceLastRequest);
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

	public static String formatMessage(ConcurrentMap<String, Object> solStatus) {

		String status = "";
		if (solStatus.containsKey(STATUS)) {
			status = (String) solStatus.get(STATUS);
		}
		Integer done = 0;
		if (solStatus.containsKey(DONE)) {
			done = (Integer) solStatus.get(DONE);
		}
		Integer total = 100;
		if (solStatus.containsKey(TOTAL)) {
			total = (Integer) solStatus.get(TOTAL);
		}
		String message = "";
		if (solStatus.containsKey(MESSAGE)) {
			message = (String) solStatus.get(MESSAGE);
		}
		String errorMessage = "";
		if (solStatus.containsKey(ERROR_MESSAGE)) {
			errorMessage = (String) solStatus.get(ERROR_MESSAGE);
		}
		return String.format(STATUS_JSON, status, done, total, message, errorMessage);
	}

	private void writeShortVersionToHTMLLogFile(PeriodLogGeneratorNew report, int iteration, String shortVersion) {
		try {
			report.startIteration(iteration, shortVersion);
			report.setIterationDates("");
			report.finishTask();
		} catch (Exception e) {

		}
	}

	private void writeTrialToHTMLLogFile(PeriodLogGeneratorNew report, int iteration, String shortVersion, Date from, Date to, ProjectWrapper projectW, Date projectEnd, Map<String, Object> result, TaskTreeNode shiftedTask) {

		DayDetails p1StartDetails = (DayDetails) result.get(P1_START);
		DayDetails p1EndDetails = (DayDetails) result.get(P1_END);
		DayDetails p2StartDetails = (DayDetails) result.get(P2_START);
		DayDetails p2EndDetails = (DayDetails) result.get(P2_END);
		DayDetails p1PreStart = (DayDetails) result.get(P1_PRE_START);

		double totalCostCurrent = p1EndDetails.getPeriodCost();
		double payment = p1StartDetails.getPayments();
		double extraPaymentNextPeriod = p2StartDetails.getPayments();
		double financeLimit = p1StartDetails.getFinance();
		double financeLimitNextPeriod = p2StartDetails.getFinance();
		double leftOverCost = p1EndDetails.getLeftOver() + p1EndDetails.getOverhead();
		double leftOverNextCost = p2EndDetails.getOverhead() + (Double) result.get(LEFTOVER_COST);
		double openBalance = p1PreStart.getBalance();
		double cashOutOthers = p1PreStart.getOtherProjectsCashOut();
		double cashOutOthersNext = p1PreStart.getOtherProjectsCashOutNext();

		try {
			Date start = from;
			Date end = to;
			if (getLogLevel().equals("detailed")) {
				end = projectEnd;
			}

			// write header
			String header = "<td></td>";
			Date index = start;
			while (index.before(end)) {
				boolean offDay = TaskUtil.isWeekendDay(index, projectW.getProjectWeekends()) || TaskUtil.isDayOff(index, projectW.getProjectVacations());
				if (!index.before(to)) {
					// future
					header = header + "<td bgcolor=\"CC9900\">" + (offDay ? "<div class=\"stripedDiv\"></div>" : "") + new SimpleDateFormat("dd/MM").format(index) + "</td>";
				} else {
					// current
					header = header + "<td bgcolor=\"lightgreen\">" + (offDay ? "<div class=\"stripedDiv\"></div>" : "") + new SimpleDateFormat("dd/MM").format(index) + "</td>";
				}
				index = addDays(index, 1);
			}

			report.startIteration(iteration, shortVersion);
			report.setIterationDates(header);

			for (TaskTreeNode task : projectW.getAllTasks()) {
				Date taskStart = task.getCalculatedTaskStart();
				Date taskEnd = task.getCalculatedTaskEnd();
				if (!taskEnd.before(from)) {
					if (getLogLevel().equals("detailed") || taskStart.before(to)) {

						String line = "<tr><td>" + task.getTask().getTaskName() + ((shiftedTask == task) ? ">>" : "") + "</td>";
						Date index2 = start;
						while (index2.before(end)) {
							boolean offDay = TaskUtil.isWeekendDay(index2, projectW.getProjectWeekends()) || TaskUtil.isDayOff(index2, projectW.getProjectVacations());
							;
							String color = "lightgreen";
							if (taskStart.before(start)) {
								color = "lightgrey";
							} else if (!index2.before(to)) {
								if (!taskStart.before(to)) {
									color = "orange";
								} else {
									color = "yellow";
								}
							}
							if (index2.before(taskStart) || index2.after(taskEnd)) {
								line += "<td>" + (offDay ? "<div class=\"stripedDiv\"></div>" : "") + "</td>";
							} else {
								line += "<td bgcolor=\"" + color + "\">" + (offDay ? "<div class=\"stripedDiv\"></div>" : "") + "</td>";
							}
							index2 = addDays(index2, 1);
						}
						report.addIterationTask(line);
					}
				}
			}
			report.setDetails(totalCostCurrent, payment, extraPaymentNextPeriod, financeLimit, financeLimitNextPeriod, leftOverCost, leftOverNextCost, openBalance, cashOutOthers, cashOutOthersNext);
			report.finishTask();
			// solutionReport.info(",");
		} catch (Exception ex) {
			// solutionReport.error("error in reporting iteration");
		}

	}

	public static boolean isIffDay(Project project, Date date) {
		return PaymentUtil.isDayOff(date, project.getDaysOffs()) || TaskUtil.isWeekendDay(date, project.getWeekend());
	}

	public static long differenceInDays(Date start, Date end) {
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(start);
		calendar2.setTime(end);
		long milliseconds1 = calendar1.getTimeInMillis();
		long milliseconds2 = calendar2.getTimeInMillis();
		long diff = milliseconds2 - milliseconds1;
		long diffDays = diff / (24 * 60 * 60 * 1000);
		return diffDays;
	}

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days); // minus number would decrement the days
		return cal.getTime();
	}

}
