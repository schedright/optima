/**
 * 
 */
package com.softpoint.optima.control;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.db.Payment;
import com.softpoint.optima.db.PlanProject;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectLight;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.Settings;
import com.softpoint.optima.db.WeekendDay;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;
import com.softpoint.optima.struct.Period;
import com.softpoint.optima.struct.SchedulePeriod;
import com.softpoint.optima.struct.SolvedTask;
import com.softpoint.optima.util.PaymentUtil;
import com.softpoint.optima.util.ProjectSolutionDetails;
import com.softpoint.optima.util.solution.PortfolioSolver;

/**
 * @author WDARWISH
 *
 */
public class ProjectController {

	private static final String PLAN_START = "plan_start";
	private static final String PLAN_END = "plan_end";

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
	public ServerResponse create(HttpSession session, String name, String code, String descritpion,
			Date proposedStartDate, Date proposedFinishDate, double interestRate, double overheadPerDay,
			int portfolioId, int weekendDaysId, double retainedPercentage, double advancedPaymentPercentage,
			double delayPenaltyAmount, int collectPaymentPeriod, int paymentRequestPeriod)
					throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());

		try {
			Project project = new Project();
			project.setProjectName(name);
			project.setProjectCode(code);
			project.setProjectDescription(descritpion);

			project.setPropusedStartDate(proposedStartDate);
			project.setProposedFinishDate(proposedFinishDate);
			project.setInterestRate(new BigDecimal(interestRate));
			project.setOverheadPerDay(new BigDecimal(overheadPerDay));

			project.setRetainedPercentage(new BigDecimal(retainedPercentage));
			project.setAdvancedPaymentPercentage(new BigDecimal(advancedPaymentPercentage));

			project.setDelayPenaltyAmount(new BigDecimal(delayPenaltyAmount));
			project.setPaymentRequestPeriod(paymentRequestPeriod);
			project.setCollectPaymentPeriod(collectPaymentPeriod);

			if (portfolioId != -1) {
				EntityController<Portfolio> portController = new EntityController<Portfolio>(
						session.getServletContext());
				Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
				project.setPortfolio(portfolio);
			}
			EntityController<WeekendDay> dayOffController = new EntityController<WeekendDay>(
					session.getServletContext());
			WeekendDay weekendDay = dayOffController.find(WeekendDay.class, weekendDaysId);
			project.setWeekendDays(weekendDay);
			controller.persist(project);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROG0001", String.format("Error creating project %s: %s", name, e.getMessage()),
					e);
		}
	}

	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session, int key, String name, String code, String descritpion,
			Date proposedStartDate, Date proposedFinishDate, double interestRate, double overheadPerDay,
			int portfolioId, int weekendDaysId, double retainedPercentage, double advancedPaymentPercentage,
			double delayPenaltyAmount, int collectPaymentPeriod, int paymentRequestPeriod)
					throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		Project project = null;
		try {
			project = controller.find(Project.class, key);
			project.setProjectName(name);
			project.setProjectCode(code);
			project.setProjectDescription(descritpion);
			project.setPropusedStartDate(proposedStartDate);
			project.setProposedFinishDate(proposedFinishDate);
			project.setInterestRate(new BigDecimal(interestRate));
			project.setOverheadPerDay(new BigDecimal(overheadPerDay));

			project.setRetainedPercentage(new BigDecimal(retainedPercentage));
			project.setAdvancedPaymentPercentage(new BigDecimal(advancedPaymentPercentage));

			project.setDelayPenaltyAmount(new BigDecimal(delayPenaltyAmount));
			project.setPaymentRequestPeriod(paymentRequestPeriod);
			project.setCollectPaymentPeriod(collectPaymentPeriod);

			if (portfolioId != -1) {
				EntityController<Portfolio> portController = new EntityController<Portfolio>(
						session.getServletContext());
				Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
				project.setPortfolio(portfolio);
			}

			EntityController<WeekendDay> dayOffController = new EntityController<WeekendDay>(
					session.getServletContext());
			WeekendDay weekendDay = dayOffController.find(WeekendDay.class, weekendDaysId);
			project.setWeekendDays(weekendDay);
			controller.merge(project);
			// because we might have changed the weekend or the days off
			// Bug#1 Shifting is not working correctly when changing weekends!
			// -- BassemVic
			TaskController taskController = new TaskController();
			taskController.adjustStartDateBasedOnTaskDependency(session, key, false);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0002", String.format("Error updating project %s: %s",
					project != null ? project.getProjectName() : "", e.getMessage()), e);
		}
	}

	public ServerResponse updateShort(HttpSession session, int key, String name, String code, String descritpion,
			int portfolioId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		Project project = null;
		try {
			project = controller.find(Project.class, key);
			project.setProjectName(name);
			project.setProjectCode(code);
			project.setProjectDescription(descritpion);

			if (portfolioId != -1) {
				EntityController<Portfolio> portController = new EntityController<Portfolio>(
						session.getServletContext());
				Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
				project.setPortfolio(portfolio);
			}
			controller.merge(project);
			TaskController taskController = new TaskController();
			taskController.adjustStartDateBasedOnTaskDependency(session, key, false);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0002", String.format("Error updating project %s: %s",
					project != null ? project.getProjectName() : "", e.getMessage()), e);
		}
	}

	public ServerResponse removePortfolio(HttpSession session, int key) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		Project project = null;
		try {
			project = controller.find(Project.class, key);
			project.setPortfolio(null);
			controller.merge(project);
			TaskController taskController = new TaskController();
			taskController.adjustStartDateBasedOnTaskDependency(session, key, false);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0002", String.format("Error updating project %s: %s",
					project != null ? project.getProjectName() : "", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session, Integer key) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			Project project = controller.find(Project.class, key);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, project);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003", String.format("Error looking up project %d: %s", key, e.getMessage()),
					e);
		}
	}

	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session, Integer key) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			controller.remove(Project.class, key);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0004", String.format("Error removing project %d: %s", key, e.getMessage()),
					e);
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
			return new ServerResponse("0", PortfolioSolver.SUCCESS, projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005", String.format("Error loading projects : %s", e.getMessage()), e);
		}
	}

	public List<Project> findAllInList(HttpSession session, Set<Integer> includedProjectsSet) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			String inClause = "";
			for (Integer i : includedProjectsSet) {
				if (!inClause.isEmpty()) {
					inClause += ",";
				}
				inClause += i;
			}
			inClause = "(" + inClause + ")";
			List<Project> dayOffs = controller.findAllQuery(Project.class,
					"Select d from Project d where d.projectId in  " + inClause);
			return dayOffs;
		} catch (EntityControllerException e) {
		}
		return new ArrayList<Project>();
	}

	public ServerResponse findAllLight(HttpSession session) throws OptimaException {
		EntityController<ProjectLight> controller = new EntityController<ProjectLight>(session.getServletContext());
		try {
			List<ProjectLight> projects = controller.findAll(ProjectLight.class);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005", String.format("Error loading projects : %s", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse findAllByPortfolio(HttpSession session, int portfolioId) throws OptimaException {
		try {
			EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, portfolio.getProjects());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0006", String.format("Error loading projects : %s", e.getMessage()), e);
		}
	}

	public List<Payment> findAllPaymentsByProjectId(HttpSession session, int projectId) throws OptimaException {
		try {
			EntityController<Payment> paymentController = new EntityController<Payment>(session.getServletContext());
			List<Payment> dayOffs = paymentController.findAll(Payment.class,
					"Select d from Payment d where d.projectId = ?1 ", projectId);
			return dayOffs;
		} catch (EntityControllerException e) {
		}
		return new ArrayList<Payment>();
	}

	/**
	 * @param session
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse getOtherProjectsCurrentPeriodCost(HttpSession session, int projectId, Date from, Date to) {

		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			Project project = controller.find(Project.class, projectId);
			double otherProjectsCurrentPeriodCost = PaymentUtil.getOtherProjectsCurrentPeriodCost(project, from, to);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, otherProjectsCurrentPeriodCost);

		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003",
					String.format("Error looking up project %d: %s", projectId, e.getMessage()), e);
		}
	}

	public ServerResponse getSolutionCurrentPeriodCost(HttpSession session, int projectId, Date from, Date to) {

		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {

			double taskCostCounterEligibleTasks = 0;
			Date taskEndDate;
			Date taskDate;
			Calendar calendar = Calendar.getInstance();

			int effictiveNumberOfDays;

			Project project = controller.find(Project.class, projectId);
			List<ProjectTask> projectTasks = project.getProjectTasks();
			Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
			@SuppressWarnings("unused")
			Date projectEndDate = projectDates[1];

			for (ProjectTask currentTask : projectTasks) {
				taskDate = PaymentUtil.getTaskDate(currentTask);
				calendar.setTime(taskDate);
				calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
				taskEndDate = calendar.getTime();

				if ((taskDate.after(from) || taskDate.equals(from)) && taskDate.before(to)) {

					effictiveNumberOfDays = Math.min(PaymentUtil.daysBetween(taskDate, taskEndDate) + 1,
							PaymentUtil.daysBetween(taskDate, to));

					Calendar endEffectiveDate = Calendar.getInstance();
					endEffectiveDate.setTime(taskDate);
					endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays);

					int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, taskDate,
							endEffectiveDate.getTime()); // What if taskEndDate
															// > to?

					effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
					double taskCost = currentTask.getUniformDailyCost().doubleValue() * effictiveNumberOfDays;
					taskCostCounterEligibleTasks += taskCost;

				}

			}

			return new ServerResponse("0", PortfolioSolver.SUCCESS, taskCostCounterEligibleTasks);

		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003",
					String.format("Error looking up project %d: %s", projectId, e.getMessage()), e);
		}
	}

	// private ReentrantLock solutionLock = new ReentrantLock();
	// private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd
	// MMM, yyyy");

	public class SolutionRunner implements Runnable {
		public PortfolioSolver solver;
		public Portfolio portfolio;
		public String projectsPriority;
		public HttpSession session;

		SolutionRunner(Portfolio portfolio, String projectsPriority, HttpSession session) {
			this.portfolio = portfolio;
			this.projectsPriority = projectsPriority;
			this.session = session;
		}

		public void run() {
			long millis1 = System.currentTimeMillis();
			PortfolioSolver solver = new PortfolioSolver(portfolio, projectsPriority);
			solver.solveIt(session);

			long millis2 = System.currentTimeMillis();
			System.out.println(millis2 - millis1);
		}

	}

	public ServerResponse getStatus(HttpSession session, int portfolioId) {

		if (PortfolioSolver.currentWorkingSolutions.containsKey(portfolioId)) {
			ConcurrentMap<String, Object> solStatus = PortfolioSolver.currentWorkingSolutions.get(portfolioId);
			if (solStatus.containsKey(PortfolioSolver.STATUS)) {
				return new ServerResponse("0", PortfolioSolver.SUCCESS, PortfolioSolver.formatMessage(solStatus));
			} else {
				return new ServerResponse("0", PortfolioSolver.SUCCESS, "running");
			}
		}
		return new ServerResponse("0", PortfolioSolver.SUCCESS, "notfound");
	}

	public ServerResponse getSolution(HttpSession session, int projectId, String outputFormat,
			String projectsPriority) {

		EntityController<Project> projectController = new EntityController<>(session.getServletContext());
		try {
			synchronized (this) {
				Project project = projectController.find(Project.class, projectId);
				int portfolioId = project.getPortfolio().getPortfolioId();
				if (PortfolioSolver.currentWorkingSolutions.containsKey(portfolioId)) {
					ConcurrentMap<String, Object> solStatus = PortfolioSolver.currentWorkingSolutions.get(portfolioId);
					if (!solStatus.containsKey(PortfolioSolver.SOLVER)) {
						PortfolioSolver.currentWorkingSolutions.remove(portfolioId);
					}
				}

				if (PortfolioSolver.currentWorkingSolutions.containsKey(portfolioId)) {
					return new ServerResponse("0", PortfolioSolver.SUCCESS, "running");
				} else {
					int running = 0;
					for (Integer pid : PortfolioSolver.currentWorkingSolutions.keySet()) {
						ConcurrentMap<String, Object> s = PortfolioSolver.currentWorkingSolutions.get(pid);
						if (s.containsKey(PortfolioSolver.SOLVER)) {
							running++;
						}
					}
					if (running < PortfolioSolver.MAX_RUNNING_SOLUTIONS) {
						SolutionRunner runner = new SolutionRunner(project.getPortfolio(), projectsPriority, session);
						ConcurrentMap<String, Object> x = new ConcurrentHashMap<String, Object>();
						x.put(PortfolioSolver.SOLVER, runner);
						x.put(PortfolioSolver.TOTAL, 100); // temp result, will
															// be updated from
															// the solver
						x.put(PortfolioSolver.DONE, 0); // temp result, will be
														// updated from the
														// solver
						x.put(PortfolioSolver.STATUS, "running"); // temp
																	// result,
																	// will be
																	// updated
																	// from the
																	// solver
						PortfolioSolver.currentWorkingSolutions.put(portfolioId, x);

						Thread t = new Thread(runner);
						t.start();
					} else {
						return new ServerResponse("0", "Busy", "server is running other solutions, try again later");
					}
				}
			}
			return new ServerResponse("0", PortfolioSolver.SUCCESS, "running");

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003", String.format("Error finding solution: %s", e.getMessage()), e);
		}

	}

	/*
	 * private Date getlastDayBeforeScheduling(Project project) { Date lastDay =
	 * new Date(0); List<ProjectTask> tasks = project.getProjectTasks(); for
	 * (ProjectTask task : tasks) { Date taskEndDate = null; if
	 * (task.getCalendarStartDate() != null) taskEndDate =
	 * task.getCalendarStartDate();
	 * 
	 * Calendar calendar = Calendar.getInstance();
	 * calendar.setTime(taskEndDate); calendar.add(Calendar.DATE,
	 * task.getDuration()); taskEndDate = calendar.getTime();
	 * 
	 * if (lastDay.getTime() < taskEndDate.getTime()) lastDay = taskEndDate;
	 * 
	 * } return lastDay; }
	 * 
	 * private Date getlastDayAfterScheduling(Project project) { Date lastDay =
	 * new Date(0); List<ProjectTask> tasks = project.getProjectTasks(); for
	 * (ProjectTask task : tasks) { Date taskEndDate; if
	 * (task.getScheduledStartDate() != null) taskEndDate =
	 * task.getScheduledStartDate(); else taskEndDate =
	 * task.getCalendarStartDate();
	 * 
	 * Calendar calendar = Calendar.getInstance();
	 * calendar.setTime(taskEndDate); calendar.add(Calendar.DATE,
	 * task.getDuration()); taskEndDate = calendar.getTime();
	 * 
	 * if (lastDay.getTime() < taskEndDate.getTime()) lastDay = taskEndDate;
	 * 
	 * } return lastDay; }
	 * 
	 * private boolean atLeastOneTaskAtCurrentPeriod(List<SolvedTask>
	 * solvedTasks, Date to) {
	 * 
	 * for (SolvedTask task : solvedTasks) { if
	 * (task.getScheduledStartDate().getTime() < to.getTime()) return true; }
	 * 
	 * return false; }
	 * 
	 * private int getScheduledTasksCount(Project project, Date to) {
	 * List<ProjectTask> projectTasks = project.getProjectTasks(); int count =
	 * 0; for (ProjectTask task : projectTasks) { Date startDate =
	 * task.getCalendarStartDate(); if (startDate == null) { startDate =
	 * task.getActualStartDate(); } if (startDate == null) { startDate =
	 * task.getScheduledStartDate(); } if (startDate == null) { startDate =
	 * task.getTentativeStartDate(); } if (startDate == null) { startDate = new
	 * Date(); }
	 * 
	 * int duration = task.getCalenderDuration(); if (duration == 0) { duration
	 * = task.getDuration(); }
	 * 
	 * long endDate = startDate.getTime() + (duration - 1) * 86400000;
	 * 
	 * if (endDate <= to.getTime()) count++; } return count; }
	 * 
	 * private SchedulePeriod getCurrentPeriodBoundries(HttpSession session,
	 * Date startDate, int portfolioId) { SchedulePeriod schedulePeriod = new
	 * SchedulePeriod(); try {
	 * 
	 * Period paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session,
	 * startDate, portfolioId); Period financeSchedulePeriod =
	 * PaymentUtil.findFinanceSchedule(session, startDate, portfolioId);
	 * 
	 * // Construct the current period Period current = new Period();
	 * 
	 * if (paymentSchedulePeriod.getDateFrom().getTime() >=
	 * financeSchedulePeriod.getDateFrom().getTime()) {
	 * current.setDateFrom(paymentSchedulePeriod.getDateFrom()); } else {
	 * current.setDateFrom(financeSchedulePeriod.getDateFrom()); }
	 * 
	 * if (paymentSchedulePeriod.getDateTo().getTime() <=
	 * financeSchedulePeriod.getDateTo().getTime()) {
	 * current.setDateTo(paymentSchedulePeriod.getDateTo()); } else {
	 * current.setDateTo(financeSchedulePeriod.getDateTo()); }
	 * 
	 * // Construct the current to and the next period Period next = new
	 * Period();
	 * 
	 * Calendar calendar = Calendar.getInstance();
	 * calendar.setTime(current.getDateTo()); calendar.add(Calendar.DATE, 1);
	 * 
	 * paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session,
	 * calendar.getTime(), portfolioId); financeSchedulePeriod =
	 * PaymentUtil.findFinanceSchedule(session, calendar.getTime(),
	 * portfolioId);
	 * 
	 * if (paymentSchedulePeriod.getDateFrom().getTime() >=
	 * financeSchedulePeriod.getDateFrom().getTime()) {
	 * next.setDateFrom(paymentSchedulePeriod.getDateFrom()); } else {
	 * next.setDateFrom(financeSchedulePeriod.getDateFrom()); }
	 * 
	 * if (paymentSchedulePeriod.getDateTo().getTime() <=
	 * financeSchedulePeriod.getDateTo().getTime()) {
	 * next.setDateTo(paymentSchedulePeriod.getDateTo()); } else {
	 * next.setDateTo(financeSchedulePeriod.getDateTo()); }
	 * 
	 * schedulePeriod.setCurrent(current); schedulePeriod.setNext(next);
	 * 
	 * return schedulePeriod;
	 * 
	 * } catch (OptimaException e) {
	 * 
	 * e.printStackTrace(); return null; }
	 * 
	 * }
	 */
	public SchedulePeriod getCurrentPeriodBoundriesNew(HttpSession session, Date startDate, int portfolioId) {
		SchedulePeriod schedulePeriod = new SchedulePeriod();
		try {

			Period paymentSchedulePeriod = PaymentUtil.findPaymentScheduleNew(session, startDate, portfolioId);
			Period financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, startDate, portfolioId);

			// Construct the current period
			Period current = new Period();

			if (paymentSchedulePeriod.getDateFrom().getTime() >= financeSchedulePeriod.getDateFrom().getTime()) {
				current.setDateFrom(paymentSchedulePeriod.getDateFrom());
			} else {
				current.setDateFrom(financeSchedulePeriod.getDateFrom());
			}

			if (paymentSchedulePeriod.getDateTo().getTime() <= financeSchedulePeriod.getDateTo().getTime()) {
				current.setDateTo(paymentSchedulePeriod.getDateTo());
			} else {
				current.setDateTo(financeSchedulePeriod.getDateTo());
			}

			// Construct the current to and the next period
			Period next = new Period();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(current.getDateTo());
			calendar.add(Calendar.DATE, 1);

			paymentSchedulePeriod = PaymentUtil.findPaymentScheduleNew(session, calendar.getTime(), portfolioId);
			financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, calendar.getTime(), portfolioId);

			if (paymentSchedulePeriod.getDateFrom().getTime() >= financeSchedulePeriod.getDateFrom().getTime()) {
				next.setDateFrom(paymentSchedulePeriod.getDateFrom());
			} else {
				next.setDateFrom(financeSchedulePeriod.getDateFrom());
			}

			if (paymentSchedulePeriod.getDateTo().getTime() <= financeSchedulePeriod.getDateTo().getTime()) {
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

	/*
	 * public ServerResponse getPeriodSolutionNew(HttpSession session, int
	 * projectId, Date from, Date to, Date next, String solvedProjects, String
	 * outputFormat, Hashtable<Integer, Double> totalRetainedAmount,
	 * Hashtable<Integer, Double> totalAdvancedPaymentAmount, HashMap<Integer,
	 * Integer> completedProjects, Hashtable<Integer, String>
	 * solutionInformation, String timeStamp) { Map<Integer,
	 * ProjectPaymentDetail> paymentDetails =
	 * PaymentUtil.getPaymentDetailsNew(session, projectId, from, to);
	 * 
	 * EntityController<Project> projectController = new
	 * EntityController<>(session.getServletContext());
	 * EntityController<Portfolio> portController = new
	 * EntityController<Portfolio>(session.getServletContext());
	 * EntityController<ProjectPayment> controller = new
	 * EntityController<ProjectPayment>(session.getServletContext());
	 * 
	 * double paymentCurrent = 0; try {
	 * 
	 * solutionLock.lock();
	 * 
	 * Project project = projectController.find(Project.class, projectId);
	 * 
	 * double advancedPaymentAmount =
	 * ProjectSolutionDetails.getAdvancedPaymentAmmount(project); double
	 * advancedPaymentPercentage =
	 * project.getAdvancedPaymentPercentage().doubleValue(); double
	 * retainedPercentage = project.getRetainedPercentage().doubleValue();
	 * 
	 * double retainedAmountDeduction = 0.0; double advancedPaymentDeduction =
	 * 0.0;
	 * 
	 * PeriodLogGenerator solutionReport = new
	 * PeriodLogGenerator(session.getServletContext(), project.getProjectCode()
	 * + "_" + timeStamp, DATE_FORMATTER.format(from),
	 * DATE_FORMATTER.format(to));
	 * 
	 * solutionReport.setProject(project.getPortfolio().getPortfolioName(),
	 * project.getProjectCode() + "-" + project.getProjectName());
	 * 
	 * double leftOverCost =
	 * PaymentUtil.getPortfolioLeftOverCost(portController,
	 * project.getPortfolio(), from, to, completedProjects); // Overhead current
	 * + cost of any task // starts before this period and still // not finished
	 * double openBalance = PaymentUtil.getPortfolioOpenBalanceNew(session,
	 * project.getPortfolio(), from); // balance // that // is // accumulated //
	 * from // the // previous // period
	 * 
	 * Date[] projectBoundaries =
	 * PaymentUtil.getPortofolioDateRanges(controller,
	 * project.getPortfolio().getPortfolioId());
	 * 
	 * double payment = PaymentUtil.getPortfolioPaymentNew(session, from, to,
	 * paymentDetails, project.getPortfolio(), false, projectBoundaries);
	 * 
	 * paymentCurrent = payment;
	 * 
	 * double financeLimit = PaymentUtil.getFinanceLimit(session,
	 * project.getPortfolio().getPortfolioId(), from); double extraPayment =
	 * PaymentUtil.getExtraPayment(session,
	 * project.getPortfolio().getPortfolioId(), from); financeLimit +=
	 * extraPayment;
	 * 
	 * double financeLimitNextPeriod = PaymentUtil.getFinanceLimit(session,
	 * project.getPortfolio().getPortfolioId(), to); double
	 * extraPaymentNextPeriod = PaymentUtil.getExtraPayment(session,
	 * project.getPortfolio().getPortfolioId(), to); financeLimitNextPeriod +=
	 * extraPaymentNextPeriod;
	 * 
	 * double cashOutOthers = PaymentUtil.getCashOutOtherProjects(session,
	 * project, from, to, solvedProjects); // cash // out // for // solved //
	 * projects
	 * 
	 * List<ProjectTask> eligibleTasks = null; boolean completed = false;
	 * 
	 * if (isPaymentAtThisPeriod(session, from, to, project, controller,
	 * projectBoundaries)) { retainedAmountDeduction = retainedPercentage *
	 * payment; advancedPaymentDeduction = advancedPaymentAmount *
	 * advancedPaymentPercentage; }
	 * 
	 * double cashAvailable = financeLimit + openBalance + payment -
	 * leftOverCost - cashOutOthers; double cashAvailableNextPeriod =
	 * financeLimitNextPeriod + openBalance + payment - leftOverCost -
	 * cashOutOthers - retainedAmountDeduction - advancedPaymentDeduction;
	 * 
	 * Map<Integer, TaskState> taskStates = initTaskState(project);
	 * eligibleTasks = PaymentUtil.getEligibleTasks(project, from, to, true);
	 * int iteration = 0; boolean initial = true; String initialInfo = "";
	 * String solInfo = ""; while (!completed) { List<ProjectTask>
	 * currentEligibleSet = PaymentUtil.getEligibleTasks(project, from, to,
	 * true); double eligibleTasksCurrentPeroidCost = PaymentUtil
	 * .getEligiableTaskCurrentPeriodCost(currentEligibleSet, from, to);
	 * 
	 * // Bug#4 double otherPojectsEligibleTasksCurrentPeroidCost = PaymentUtil
	 * .getOtherProjectsCurrentPeriodCost(project, from, to); double
	 * otherPojectsEligibleTasksLeftOverCost =
	 * PaymentUtil.getOtherProjectsLeftOverCostNew(session, project, from, to,
	 * projectBoundaries[1], completedProjects); // -------------
	 * 
	 * // TODO Input Parameter should consider all tasks going through // the
	 * current period
	 * 
	 * List<ProjectTask> currentTaskSet = PaymentUtil.getCurrentTasks(project,
	 * from, to, true);
	 * 
	 * double eligibleTasksLeftOverCost =
	 * PaymentUtil.getEligiableTaskLeftOverCostNew(session, currentTaskSet,
	 * project, from, to, projectBoundaries[1]);
	 * 
	 * paymentDetails = PaymentUtil.getPaymentDetailsNew(session, projectId, to,
	 * next); double expectedCashIn =
	 * PaymentUtil.getPortfolioPaymentNew(session, to, next, paymentDetails,
	 * project.getPortfolio(), false, projectBoundaries);
	 * 
	 * paymentCurrent = expectedCashIn;
	 * 
	 * double totalCostCurrent = eligibleTasksCurrentPeroidCost; // - //
	 * eligiableTaskFinanceCost; double leftOverNextCost =
	 * eligibleTasksLeftOverCost + otherPojectsEligibleTasksLeftOverCost; // -
	 * // nextPeriodLeftoverFinanceCost;
	 * 
	 * int projLength = PaymentUtil.getProjectLength(project); String
	 * shortVersion = String .format(
	 * "Selected: Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n"
	 * , iteration, cashAvailable,
	 * PaymentUtil.getEligibaleTaskNameList(currentEligibleSet),
	 * PaymentUtil.getTaskListStart(currentEligibleSet), projLength,
	 * totalCostCurrent, cashAvailableNextPeriod - totalCostCurrent +
	 * expectedCashIn - leftOverNextCost, totalCostCurrent <= cashAvailable &&
	 * cashAvailableNextPeriod - totalCostCurrent + expectedCashIn >=
	 * leftOverNextCost ? "Yes" : "No", cashAvailable - totalCostCurrent < 0 ? 0
	 * : cashAvailable - totalCostCurrent);
	 * 
	 * writeTrialToHTMLLogFile(solutionReport, iteration, shortVersion, from,
	 * to, project, PaymentUtil.getProjectExpectedEndDate(project),
	 * totalCostCurrent, payment, paymentCurrent, financeLimit,
	 * financeLimitNextPeriod, leftOverCost, leftOverNextCost, openBalance,
	 * cashOutOthers);
	 * 
	 * if (initial) { initialInfo = cashAvailable + "," + totalCostCurrent + ","
	 * + (cashAvailableNextPeriod - totalCostCurrent + expectedCashIn -
	 * leftOverNextCost) + "," + (cashAvailable - totalCostCurrent); solInfo =
	 * cashAvailable + "," + totalCostCurrent + "," + (cashAvailableNextPeriod -
	 * totalCostCurrent + expectedCashIn - leftOverNextCost) + "," +
	 * (cashAvailable - totalCostCurrent); initial = false; } else solInfo =
	 * cashAvailable + "," + totalCostCurrent + "," + (cashAvailableNextPeriod -
	 * totalCostCurrent + expectedCashIn - leftOverNextCost) + "," +
	 * (cashAvailable - totalCostCurrent);
	 * 
	 * if (totalCostCurrent <= cashAvailable && cashAvailableNextPeriod -
	 * totalCostCurrent + expectedCashIn >= leftOverNextCost) { completed =
	 * true; } else {
	 * 
	 * List<TaskSolution> solutions = new LinkedList<>(); boolean noChange =
	 * true; for (ProjectTask task : currentEligibleSet) {
	 * task.setScheduledStartDate(null); Date taskDate =
	 * PaymentUtil.getTaskDate(task); Calendar calendar =
	 * Calendar.getInstance(); calendar.setTime(taskDate); do {
	 * calendar.add(Calendar.DATE, 1); } while
	 * (PaymentUtil.isDayOff(calendar.getTime(), project.getDaysOffs()) ||
	 * PaymentUtil.isWeekendDay(calendar.getTime(), project.getWeekendDays()));
	 * 
	 * // Bug#2 Not checking all the cases // if (to.equals(calendar.getTime())
	 * || // to.after(calendar.getTime())) { noChange = false;
	 * task.setCalendarStartDate(calendar.getTime());
	 * PaymentUtil.adjustStartDateBasedOnTaskDependency(project);
	 * updateScheduledState(project, taskStates);
	 * 
	 * TaskSolution solution = new TaskSolution(task); List<ProjectTask>
	 * solutionCurrenteligibleTasks = PaymentUtil.getEligibleTasks(project,
	 * from, to, true);
	 * 
	 * double solutionEligibleTasksCurrentPeroidCost = PaymentUtil
	 * .getEligiableTaskCurrentPeriodCost(solutionCurrenteligibleTasks, from,
	 * to);
	 * 
	 * // TODO Input Parameter should consider all tasks going // through the
	 * current period List<ProjectTask> solutionCurrentTaskSet =
	 * PaymentUtil.getCurrentTasks(project, from, to, true); double
	 * solutionEligibleTasksLeftOverCost =
	 * PaymentUtil.getEligiableTaskLeftOverCostNew(session,
	 * solutionCurrentTaskSet, project, from, to, projectBoundaries[1]);
	 * 
	 * paymentDetails = PaymentUtil.getPaymentDetailsNew(session, projectId, to,
	 * next); double solutionExpectedCashIn =
	 * PaymentUtil.getPortfolioPaymentNew(session, to, next, paymentDetails,
	 * project.getPortfolio(), false, projectBoundaries);
	 * 
	 * double solutionTotalCostCurrent =
	 * solutionEligibleTasksCurrentPeroidCost;// - //
	 * solutionEligiableTaskFinanceCost;
	 * 
	 * double solutionOtherPojectsEligibleTasksLeftOverCost = PaymentUtil
	 * .getOtherProjectsLeftOverCostNew(session, project, from, to,
	 * projectBoundaries[1], completedProjects);
	 * 
	 * int projectLength = PaymentUtil.getProjectLength(project);
	 * solution.setCurrentPeriodCost(solutionTotalCostCurrent);
	 * solution.setLeftOversCost( solutionEligibleTasksLeftOverCost +
	 * solutionOtherPojectsEligibleTasksLeftOverCost); // - //
	 * solutionNextPeriodLeftOverFinanceCost);
	 * 
	 * solution.setIncome(solutionExpectedCashIn);
	 * solution.setProjectLength(projectLength);
	 * solution.setStartDate(calendar.getTime());
	 * 
	 * String shortVerion = String .format(
	 * "Iteration [%d] R[Current]:[%.2f] Activities:[%s] Start [%s] Project Duration: [%d] C[Current]: [%f] R[Next]:[%f] Feasible: %s Raimining cash:[%f]%n"
	 * , iteration, cashAvailable,
	 * PaymentUtil.getEligibaleTaskNameList(solutionCurrenteligibleTasks),
	 * PaymentUtil .getTaskListStart(solutionCurrenteligibleTasks),
	 * projectLength, solutionTotalCostCurrent, cashAvailableNextPeriod -
	 * solutionTotalCostCurrent + solutionExpectedCashIn -
	 * solution.getLeftOversCost(), solutionTotalCostCurrent <= cashAvailable &&
	 * cashAvailableNextPeriod - solutionTotalCostCurrent +
	 * solutionExpectedCashIn >= solution.getLeftOversCost() ? "Yes" : "No",
	 * cashAvailable - solutionTotalCostCurrent < 0 ? 0 : cashAvailable -
	 * solutionTotalCostCurrent);
	 * 
	 * writeTrialToHTMLLogFile(solutionReport, iteration, shortVerion, from, to,
	 * project, PaymentUtil.getProjectExpectedEndDate(project),
	 * solutionEligibleTasksCurrentPeroidCost, payment, paymentCurrent,
	 * financeLimit, financeLimitNextPeriod, leftOverCost,
	 * solutionEligibleTasksLeftOverCost +
	 * solutionOtherPojectsEligibleTasksLeftOverCost, openBalance,
	 * cashOutOthers);
	 * 
	 * solutions.add(solution); task.setCalendarStartDate(taskDate);
	 * resetTaskState(project, taskStates);
	 * 
	 * // } } if (noChange) { completed = true; // no tasks can be scheduled
	 * this // period. Stop here } else {
	 * 
	 * TaskSolution nextIteration = PaymentUtil.findSolutionIteration(solutions,
	 * cashAvailable, cashAvailableNextPeriod); if (nextIteration != null) {
	 * nextIteration.getTask().setCalendarStartDate(nextIteration.getStartDate()
	 * );
	 * 
	 * PaymentUtil.adjustStartDateBasedOnTaskDependency(project); taskStates =
	 * initTaskState(project); } else { return new ServerResponse("0",
	 * PortfolioSolver.SUCCESS, null); } }
	 * 
	 * } iteration++; } solutionInformation.put(projectId, initialInfo + "," +
	 * solInfo + "," + paymentCurrent);
	 * 
	 * if (from.getTime() != project.getPropusedStartDate().getTime()) { double
	 * originalPayment = payment / (1.0 -
	 * project.getRetainedPercentage().doubleValue() -
	 * project.getAdvancedPaymentPercentage().doubleValue());
	 * 
	 * if (totalRetainedAmount.get(projectId) == null)
	 * totalRetainedAmount.put(projectId, originalPayment *
	 * project.getRetainedPercentage().doubleValue()); else
	 * totalRetainedAmount.replace(projectId, totalRetainedAmount.get(projectId)
	 * + originalPayment * project.getRetainedPercentage().doubleValue());
	 * 
	 * if (totalAdvancedPaymentAmount.get(projectId) == null)
	 * totalAdvancedPaymentAmount.put(projectId, originalPayment *
	 * project.getAdvancedPaymentPercentage().doubleValue()); else
	 * totalAdvancedPaymentAmount.replace(projectId,
	 * totalAdvancedPaymentAmount.get(projectId) + originalPayment *
	 * project.getAdvancedPaymentAmount().doubleValue());
	 * 
	 * }
	 * 
	 * List<SolvedTask> solvedTasks = new ArrayList<>(); for (ProjectTask task :
	 * eligibleTasks) { SolvedTask solvedTask = new SolvedTask();
	 * solvedTask.setTaskId(task.getTaskId());
	 * solvedTask.setTaskName(task.getTaskName());
	 * solvedTask.setTaskDescription(task.getTaskDescription());
	 * solvedTask.setScheduledStartDate(task.getCalendarStartDate());
	 * solvedTask.setCalenderDuration(task.getCalenderDuration());
	 * solvedTasks.add(solvedTask); }
	 * 
	 * solutionReport.flushFile(); return new ServerResponse("0",
	 * PortfolioSolver.SUCCESS, solvedTasks);
	 * 
	 * } catch (EntityControllerException | OptimaException e) {
	 * e.printStackTrace(); return new ServerResponse("PROJ0003", String.format(
	 * "Error solving period for project %d: %s", projectId, e.getMessage()),
	 * e); } finally { solutionLock.unlock(); }
	 * 
	 * }
	 * 
	 * private static boolean SHOW_COMING_TASKS = true;
	 * 
	 * private void writeTrialToHTMLLogFile(PeriodLogGenerator report, int
	 * iteration, String shortVersion, Date from, Date to, Project project, Date
	 * projectEnd, double totalCostCurrent, double payment, double
	 * extraPaymentNextPeriod, double financeLimit, double
	 * financeLimitNextPeriod, double leftOverCost, double leftOverNextCost,
	 * double openBalance, double cashOutOthers) { try { Date start = from; Date
	 * end = to; if (SHOW_COMING_TASKS) { end = projectEnd; }
	 * 
	 * long daysCount = PortfolioSolver.differenceInDays(start, end) + 1;
	 * 
	 * // write header String header = "<td></td>"; Date index = start; while
	 * (PortfolioSolver.differenceInDays(index, end) > -1) { boolean offDay =
	 * PortfolioSolver.isIffDay(project, index); if (!index.before(to)) { //
	 * future header = header + "<td bgcolor=\"CC9900\">" + (offDay ?
	 * "<div class=\"stripedDiv\"></div>" : "") + new
	 * SimpleDateFormat("dd/MM").format(index) + "</td>"; } else { // current
	 * header = header + "<td bgcolor=\"lightgreen\">" + (offDay ?
	 * "<div class=\"stripedDiv\"></div>" : "") + new
	 * SimpleDateFormat("dd/MM").format(index) + "</td>"; } index =
	 * PortfolioSolver.addDays(index, 1); }
	 * 
	 * report.startIteration(iteration, shortVersion);
	 * report.setIterationDates(header);
	 * 
	 * for (ProjectTask task : project.getProjectTasks()) { Date taskStart =
	 * task.getCalendarStartDate(); Date taskEnd =
	 * PortfolioSolver.addDays(task.getCalendarStartDate(),
	 * task.getCalenderDuration() - 1); if (!taskEnd.before(from)) { if
	 * (SHOW_COMING_TASKS || !taskStart.after(to)) { String line = "<tr><td>" +
	 * task.getTaskDescription() + "</td>"; Date index2 = start; while
	 * (PortfolioSolver.differenceInDays(index2, end) > -1) { boolean offDay =
	 * PortfolioSolver.isIffDay(project, index2); String color = "lightgreen";
	 * if (taskStart.before(start)) { color = "lightgrey"; } else if
	 * (!index2.before(to)) { if (!taskStart.before(to)) { color = "orange"; }
	 * else { color = "yellow"; } } if (index2.before(taskStart) ||
	 * index2.after(taskEnd)) { line += "<td>" + (offDay ?
	 * "<div class=\"stripedDiv\"></div>" : "") + "</td>"; } else { line +=
	 * "<td bgcolor=\"" + color + "\">" + (offDay ?
	 * "<div class=\"stripedDiv\"></div>" : "") + "</td>"; } index2 =
	 * PortfolioSolver.addDays(index2, 1); } report.addIterationTask(line); } }
	 * } report.setDetails(totalCostCurrent, payment, extraPaymentNextPeriod,
	 * financeLimit, financeLimitNextPeriod, leftOverCost, leftOverNextCost,
	 * openBalance, cashOutOthers);
	 * 
	 * report.finishTask(); // solutionReport.info(","); } catch (Exception ex)
	 * { // solutionReport.error("error in reporting iteration"); }
	 * 
	 * }
	 * 
	 * private boolean isPaymentAtThisPeriod(HttpSession session, Date from,
	 * Date to, Project project, EntityController<ProjectPayment> controller,
	 * Date[] projectBoundaries) { try { List<ProjectPayment> projectPayments =
	 * PaymentUtil.getProjectPayments(session, project, projectBoundaries[1]);
	 * for (ProjectPayment projectPayment : projectPayments) { if
	 * (projectPayment.getPaymentDate() == to) return true; } } catch
	 * (EntityControllerException e) { e.printStackTrace(); } return false; }
	 * 
	 * 
	 * private Map<Integer, TaskState> initTaskState(Project project) {
	 * Map<Integer, TaskState> taskStates = new HashMap<Integer, TaskState>();
	 * for (ProjectTask task : project.getProjectTasks()) {
	 * taskStates.put(task.getTaskId(), new
	 * TaskState(task.getCalendarStartDate(), task.getCalendarStartDate(),
	 * task.getCalenderDuration(), task.getCalenderDuration(),
	 * task.getScheduledStartDate(), task.getScheduledStartDate())); } return
	 * taskStates; }
	 * 
	 * private void resetTaskState(Project project, Map<Integer, TaskState>
	 * taskStates) { for (ProjectTask task : project.getProjectTasks()) {
	 * TaskState state = taskStates.get(task.getTaskId());
	 * task.setCalendarStartDate(state.getOriginalStartDate());
	 * task.setCalenderDuration(state.getOriginalCalendarDuration());
	 * task.setScheduledStartDate(state.getOriginalScheduledStartDate()); }
	 * 
	 * }
	 * 
	 * private void updateScheduledState(Project project, Map<Integer,
	 * TaskState> taskStates) { for (ProjectTask task :
	 * project.getProjectTasks()) { TaskState state =
	 * taskStates.get(task.getTaskId());
	 * state.setShiftedCalendarDuration(task.getCalenderDuration());
	 * state.setShiftedStartDate(task.getCalendarStartDate());
	 * state.setShiftedScheduledStartData(task.getScheduledStartDate()); }
	 * 
	 * }
	 */
	public ServerResponse commitSolution(HttpSession session, int projectId, SolvedTask[] tasks) {

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

		return new ServerResponse("0", PortfolioSolver.SUCCESS, null);
	}

	public ServerResponse getProjectCost(HttpSession session, int portfolioId, Date fromDate, Date toDate,
			int projectId) {
		// System.out.println("Inside getProjectCost()");
		return new ServerResponse("0", PortfolioSolver.SUCCESS, 10.0);
	}

	SimpleDateFormat yearDF = new SimpleDateFormat("yyyy");
	SimpleDateFormat monthDF = new SimpleDateFormat("MMM");

	static DateFormat planDateFormatter = new SimpleDateFormat("MM/dd/yyyy");
	static DateFormat dmyFormatter = new SimpleDateFormat("dd/MM/yyyy");

	public ServerResponse getPlan(HttpSession session) throws OptimaException {
		// PortfolioCashFlow cashFlow = new PortfolioCashFlow();
		try {
			Map<String, String> settings = getSettingsMap(session);
			String sds = settings.get(PLAN_START);
			Date planStart = null;
			if (sds == null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, -1); // to get previous year add -1
				planStart = cal.getTime();
				addSetting(session, PLAN_START, planDateFormatter.format(planStart));
			} else {
				planStart = planDateFormatter.parse(sds);
			}

			sds = settings.get(PLAN_END);
			Date planEnd = null;
			if (sds == null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 1); // to get previous year add -1
				planEnd = cal.getTime();
				addSetting(session, PLAN_END, planDateFormatter.format(planEnd));
			} else {
				planEnd = planDateFormatter.parse(sds);
			}

			EntityController<PlanProject> ppController = new EntityController<PlanProject>(session.getServletContext());
			List<PlanProject> includedProjects = ppController.findAll(PlanProject.class);
			Set<Integer> includedProjectsSet = new HashSet<Integer>();
			for (PlanProject pp : includedProjects) {
				includedProjectsSet.add(pp.getProjectId());
			}

			EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
			List<Project> allProjects = findAllInList(session, includedProjectsSet);
			List<Map<String, Object>> selectProjectDetails = new ArrayList<Map<String, Object>>();
			StringBuilder errorMessage = new StringBuilder();

			for (Project proj : allProjects) {
				try {
					if (includedProjectsSet.contains(proj.getProjectId()) && proj.getPropusedStartDate() != null) {
						Date[] dates = PaymentUtil.getProjectExtendedDateRanges(controller, proj);
						if (!(dates[0].after(planEnd) || planStart.after(dates[1]))) {
							List<Payment> paymentList = findAllPaymentsByProjectId(session, proj.getProjectId());
							Map<String, Object> projDetails = new HashMap<String, Object>();

							selectProjectDetails.add(projDetails);
							projDetails.put("Project", proj);
							projDetails.put("Start", dates[0]);
							projDetails.put("End", dates[1]);

							if (paymentList == null || paymentList.size() == 0) {
								ProjectSolutionDetails details = new ProjectSolutionDetails(false, proj);
								details.savePaymentToDB(session);

								for (String dateString : details.getResults().keySet()) {
									DailyCashFlowMapEntity det = details.getResults().get(dateString);
									if (det.getPayments() != 0) {
										if (dateString.indexOf(",") != -1) {
											dateString = dateString.substring(0, dateString.indexOf(","));
										}
										Date date = dmyFormatter.parse(dateString);
										if (!date.before(planStart) && !date.after(planEnd)) {
											addPayment(projDetails, date, det.getPayments());
										}
									}
								}
							} else {
								for (Payment payment : paymentList) {
									Date date = payment.getPaymentDate();
									if (!date.before(planStart) && !date.after(planEnd)) {
										addPayment(projDetails, date, payment.getPaymentAmount().doubleValue());
									}
								}
							}
							if (!projDetails.containsKey("Details")) {
								errorMessage.append("<p>Failed to get the solution details for project \"")
										.append(proj.getProjectName()).append("\"</p>");
							}
						}
					}
				} catch (Exception e) {
					errorMessage.append("<p>Failed to get the solution details for project \"")
							.append(proj.getProjectName()).append("\"</p>");
				}
			}
			Map<String, Object> ret = new HashMap<String, Object>();
			ret.put("data", selectProjectDetails);
			ret.put("errors", errorMessage.toString());
			return new ServerResponse("0", "Success", ret);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	private void addPayment(Map<String, Object> projDetails, Date date, double payments) {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Double>> yearMonthPayment = (Map<String, Map<String, Double>>) projDetails
				.get("Details");
		if (yearMonthPayment == null) {
			yearMonthPayment = new HashMap<String, Map<String, Double>>();
			projDetails.put("Details", yearMonthPayment);
		}

		String year = yearDF.format(date);
		String month = monthDF.format(date);
		Map<String, Double> yearMap = yearMonthPayment.get(year);
		if (yearMap == null) {
			yearMap = new HashMap<String, Double>();
			yearMonthPayment.put(year, yearMap);
		}
		Double d = yearMap.get(month);
		if (d == null) {
			d = (double) 0;
		}
		d += payments;
		yearMap.put(month, d);
	}

	public static Map<String, String> getSettingsMap(HttpSession session) {
		Map<String, String> settings = new HashMap<String, String>();
		try {
			EntityController<Settings> controller = new EntityController<Settings>(session.getServletContext());
			List<Settings> allSettings = controller.findAll(Settings.class);
			for (Settings set : allSettings) {
				settings.put(set.getName(), set.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settings;
	}

	public ServerResponse getPlanDates(HttpSession session) throws OptimaException {
		try {
			Map<String, String> settings = getSettingsMap(session);
			Map<String, String> result = new HashMap<String, String>();
			String sds = settings.get(PLAN_START);
			Date date = null;
			if (sds == null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, -1); // to get previous year add -1
				date = cal.getTime();
				sds = planDateFormatter.format(date);
				addSetting(session, PLAN_START, sds);
			}
			result.put(PLAN_START, sds);

			sds = settings.get(PLAN_END);
			if (sds == null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 1); // to get previous year add -1
				date = cal.getTime();
				sds = planDateFormatter.format(date);
				addSetting(session, PLAN_END, sds);
			}
			result.put(PLAN_END, sds);

			return new ServerResponse("0", "Success", result);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", "Failed to get plan dates .. ", e);
		}
	}

	public ServerResponse savePlanDates(HttpSession session, String start, String end) throws OptimaException {
		try {
			addSetting(session, PLAN_START, start);
			addSetting(session, PLAN_END, end);
			return new ServerResponse("0", "Success", "");
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", "Failed to get plan dates .. ", e);
		}
	}

	public void addSetting(HttpSession session, String name, String value) {
		try {
			EntityController<Settings> controller = new EntityController<Settings>(session.getServletContext());
			List<Settings> allSettings = controller.findAll(Settings.class);
			Settings settings = null;
			for (Settings set : allSettings) {
				if (set.getName().equals(name)) {
					settings = set;
					break;
				}
			}
			if (settings == null) {
				settings = new Settings();
				settings.setName(name);
			}

			settings.setValue(value);
			controller.merge(settings);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ServerResponse findPlanProjectIds(HttpSession session) throws OptimaException {
		EntityController<PlanProject> controller = new EntityController<PlanProject>(session.getServletContext());
		try {
			List<PlanProject> projects = controller.findAll(PlanProject.class);
			return new ServerResponse("0", PortfolioSolver.SUCCESS, projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005", String.format("Error loading plan projects : %s", e.getMessage()), e);
		}
	}

	public ServerResponse changePlanProject(HttpSession session, int projectId, boolean include)
			throws OptimaException {
		EntityController<PlanProject> controller = new EntityController<PlanProject>(session.getServletContext());
		try {
			List<PlanProject> projects = controller.findAllQuery(PlanProject.class,
					String.format("SELECT o FROM %s o where o.projectId=%d", PlanProject.class.getName(), projectId));
			if (include && projects.size() == 0) {
				PlanProject pp = new PlanProject();
				pp.setProjectId(projectId);
				controller.merge(pp);
			} else if (!include && projects.size() > 0) {
				for (PlanProject pp : projects) {
					controller.remove(PlanProject.class, pp.getPlanId());
				}
			}
			return new ServerResponse("0", PortfolioSolver.SUCCESS, projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005", String.format("Error loading plan projects : %s", e.getMessage()), e);
		}
	}

	public ServerResponse getUnlinkedProjects(HttpSession session)
			throws OptimaException {
		EntityController<ProjectLight> controller = new EntityController<ProjectLight>(session.getServletContext());
		try {
			List<ProjectLight> projects = controller.findAllQuery(ProjectLight.class,"SELECT o FROM ProjectLight o where o.portfolio is null");
			return new ServerResponse("0", PortfolioSolver.SUCCESS, projects);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0005", String.format("Error getting unlinked projects : %s", e.getMessage()), e);
		}
	}

}
