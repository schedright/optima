package com.softpoint.optima.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.ServerResponse;
import com.softpoint.optima.control.EntityController;
import com.softpoint.optima.control.EntityControllerException;
import com.softpoint.optima.control.PortfolioController;
import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.Payment;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectPayment;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;

public class ProjectSolutionDetails {
	// if true then origianl, if false then final
	private boolean originalOrFinal;

//	private List<Project> projects;
	private Date projectStart;
	private Date lastTaskDate;
	private Date projectEnd;
	private Date portfolioStart;
	public Date getPortfolioStart() {
		return portfolioStart;
	}

	public Date getPortfolioEnd() {
		return portfolioEnd;
	}

	private Date portfolioEnd;
	private int projectId;

	private List<ProjectTask> projectTasks;
	private Map<ProjectTask, Date> tasksEnd;
	Map<String, DailyCashFlowMapEntity> results;
	Map<Date, Double> allPayments = new HashMap<Date, Double>();

	public ProjectSolutionDetails(boolean originalOrFinal, Project project) {
		super();
		this.originalOrFinal = originalOrFinal;
		projectId = project.getProjectId();
	//	projects = new ArrayList<Project>();
		tasksEnd = new HashMap<ProjectTask, Date>();
		results = new HashMap<String, DailyCashFlowMapEntity>();
		projectTasks = new ArrayList<ProjectTask>();

		// initialize all data for easy access later on
		List<ProjectTask> tasks = project.getProjectTasks();
		if (tasks != null && tasks.size() > 0) {
		//	projects.add(project);
			int requestPeriod = project.getPaymentRequestPeriod();
			int paymentPeriod = project.getPaymentRequestPeriod();

			calculateDates(project);

			Calendar currentDate = Calendar.getInstance();
			currentDate.setTime(portfolioStart);

			double totalRetained = 0.0;
			DailyCashFlowMapEntity lastDayEntity = new DailyCashFlowMapEntity();
			double currentRequestTotal = 0d;
			int numberOfDaysSinceLastRequest = 0;
			Map<Date, Double> paymentsCalendar = new HashMap<Date, Double>();
			double ammount = getAdvancedPaymentAmmount(project);
			if (ammount != 0) {
				paymentsCalendar.put(projectStart, ammount);
				allPayments.put(projectStart, ammount);
			}
			List<DaysOff> daysOff = project.getDaysOffs();
			String weekEnds = project.getWeekend();

			boolean startIncrement = false;
			for (Date date = portfolioStart; !date.after(portfolioEnd); currentDate.add(Calendar.DATE,
					1), date = currentDate.getTime()) {
				if (date.equals(projectStart)) {
					startIncrement = true;
				}
				if (startIncrement) {
					numberOfDaysSinceLastRequest++;
				}

				DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
				if (project.getPortfolio()!=null) {
					entity.setPortfolioId(project.getPortfolio().getPortfolioId());
				} else {
					entity.setPortfolioId(0);
				}
				entity.setProjectId(project.getProjectId());
				entity.setDay(date);

				double tasksCost = 0d;
				double tasksIncome = 0d;
				if (!(PaymentUtil.isDayOff(date, daysOff) || TaskUtil.isWeekendDay(date, weekEnds))) {
					for (ProjectTask currentTask : projectTasks) {
						Date taskStart = getTaskStart(currentTask,originalOrFinal);
						if (!date.before(taskStart) && !date.after(tasksEnd.get(currentTask))) {
							tasksCost = tasksCost + currentTask.getUniformDailyCost().doubleValue();
							tasksIncome = tasksIncome + currentTask.getUniformDailyIncome().doubleValue();
						}
					}
				}

				currentRequestTotal += tasksIncome;
				if (numberOfDaysSinceLastRequest == requestPeriod) {
					Calendar paymentCal = Calendar.getInstance();
					paymentCal.setTime(date);
					paymentCal.add(Calendar.DATE, paymentPeriod + 1);

					totalRetained += currentRequestTotal * project.getRetainedPercentage().doubleValue();
					Double paymentValue = currentRequestTotal
							* (1 - project.getAdvancedPaymentPercentage().doubleValue()
									- project.getRetainedPercentage().doubleValue());
					paymentsCalendar.put(paymentCal.getTime(), paymentValue);
					allPayments.put(paymentCal.getTime(), paymentValue);
					numberOfDaysSinceLastRequest = 0;
					currentRequestTotal = 0d;
				}

				if (!date.after(lastTaskDate) && !date.before(projectStart)) {
					tasksCost += project.getOverheadPerDay().doubleValue();
				}
				if (project.getProposedFinishDate()!=null && project.getDelayPenaltyAmount()!=null) {
					if (date.after(project.getProposedFinishDate()) && !date.after(lastTaskDate)) {
						tasksCost += project.getDelayPenaltyAmount().doubleValue();
					}
				}

				entity.setCashout(tasksCost);
				if (lastDayEntity.getBalance() < 0) {
					entity.setFinanceCost(lastDayEntity.getBalance() * PaymentUtil.getInterestInDay(project,date));
				}

				Double payment = paymentsCalendar.get(date);
				if (payment == null) {
					payment = 0d;
				} else {
					paymentsCalendar.remove(date);
				}
				if (date.equals(projectEnd)) {
					payment += totalRetained;
					allPayments.put(date, payment);
				}
				entity.setPayments(payment);

				entity.setBalance(lastDayEntity.getBalance() + payment - tasksCost);
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
				lastDayEntity = entity;
			}
		}
	}

	public static double getAdvancedPaymentAmmount(Project project) {
		double res = 0;
		if (project.getAdvancedPaymentPercentage().doubleValue()!=0) {
			double totalIncome = 0;
			for (ProjectTask task : project.getProjectTasks()) {
				totalIncome += (task.getDuration() * task.getUniformDailyIncome().doubleValue());
			}
			res = totalIncome * project.getAdvancedPaymentPercentage().doubleValue();
		}
		return res;
	}

	private Date getTaskStart(ProjectTask currentTask, boolean originalOrFinal2) {
		if (!originalOrFinal2) {
			if (currentTask.getScheduledStartDate()==null) {
				return currentTask.getCalendarStartDate();
			} else {
				return currentTask.getScheduledStartDate();
			}
		} else {
			return currentTask.calculateEffectiveTentativeStartDate();
		}
	}

	private void calculateDates(Project currentProject) {

		portfolioStart = currentProject.getPropusedStartDate();
		portfolioEnd = currentProject.getPropusedStartDate();

		List<Project> allProjects = null;
		if (currentProject.getPortfolio()==null) {
			allProjects = new ArrayList<Project>();
			allProjects.add(currentProject);
		} else {
			allProjects = currentProject.getPortfolio().getProjects();
		}
		for (Project project : allProjects) {
			List<ProjectTask> tasks = project.getProjectTasks();
			int requestPeriod = project.getPaymentRequestPeriod();
			if (requestPeriod==0) {
				requestPeriod = 10;
			}
			int paymentPeriod = project.getPaymentRequestPeriod();

			Date s = project.getPropusedStartDate();
			Date lte = project.getPropusedStartDate();
			if (s!=null && lte!=null) {
				for (ProjectTask task : tasks) {
					Date taskStart = getTaskStart(task, originalOrFinal);
					if (taskStart.before(s)) {
						s = taskStart;
					}
					Date taskEnd = getTaskEnd(project, task);
					tasksEnd.put(task, taskEnd);
	
					if (taskEnd.after(lte)) {
						lte = taskEnd;
					}
	
					if (project == currentProject) {
						projectTasks.add(task);
					}
				}
			}

			// the actual end date should actually include the remaining days in
			// the period plus waiting until it is payed out.

			int diffInDays = (project.getPropusedStartDate()!=null && lte!=null)? PortfolioController.daysBetween(project.getPropusedStartDate(), lte):0;
			int daysInLastPeriod = diffInDays % requestPeriod;
			int daysRemainingInThePeriod = daysInLastPeriod == 0 ? 0 : (requestPeriod - daysInLastPeriod + 1);
			int shift = daysRemainingInThePeriod + paymentPeriod ;

			// for example
			// if request is 6 days, and payment is in 8 days, and the last day
			// of the last task is # 25, means we already have 4 full requests
			// and one day in the fifth
			// so will need to wait for 5 days for the next request and 8 days
			// for the payment

			Calendar cal = Calendar.getInstance();
			cal.setTime(lte);
			cal.add(Calendar.DATE, shift);
			Date end = cal.getTime();

			if (project == currentProject) {
				projectStart = s;
				projectEnd = end;
				lastTaskDate = lte;
			}

			if (portfolioStart.after(s)) {
				portfolioStart = s;
			}
			if (portfolioEnd.before(end)) {
				portfolioEnd = end;
			}
		}
	}

	public Date getProjectStart() {
		return projectStart;
	}

	public Date getProjectEnd() {
		return projectEnd;
	}

	public Map<String, DailyCashFlowMapEntity> getResults() {
		return results;
	}

	/*
	 * if original then we need to calculate the end of the task as it can have
	 * days off or weekends if final, then it is already calculated before, so
	 * we just add it.
	 */
	private Date getTaskEnd(Project project, ProjectTask task) {
		if (!originalOrFinal) {
			return PortfolioController.addDayes( task.getScheduledStartDate() != null?task.getScheduledStartDate():task.getCalendarStartDate(), task.getCalenderDuration() - 1);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(task.calculateEffectiveTentativeStartDate());
			List<DaysOff> daysOff = project.getDaysOffs();
			String weekEnds = project.getWeekend();
			int duration = task.getDuration();
			Date theDate;
			do {
				theDate = cal.getTime();
				if (!(PaymentUtil.isDayOff(theDate, daysOff) || TaskUtil.isWeekendDay(theDate, weekEnds))) {
					duration--;
				}
				cal.add(Calendar.DATE, 1);
			} while (duration > 0);
			return theDate;
		}
	}
	
	public void savePaymentToDB(HttpSession session) {
		List<Date> keys = new ArrayList<Date>(allPayments.keySet());
		Collections.sort(keys);
		try {
			ProjectSolutionDetails.removePaymentsByProjectId(session, projectId);
		} catch (OptimaException e1) {
		}
		EntityController<Payment> controller = new EntityController<Payment>(session.getServletContext());
		try {
			controller.mergeTransactionStart();
			for (Date k:keys) {
				Double val = allPayments.get(k);
				if (val!=0) { 
					Payment p = new Payment();
					p.setPaymentAmount(BigDecimal.valueOf(val));
					p.setPaymentDate(k);
					p.setProjectId(projectId);
					controller.merge(p);
				}
			}

			controller.mergeTransactionClose();
		} catch (EntityControllerException e) {
		}
	}
	
	public static ServerResponse removePaymentsByProjectId(HttpSession session , int projectId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			controller.dml(ProjectPayment.class, "Delete from Payment p Where p.projectId = ?1" , projectId);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PAYM0010" , String.format("Error loading projectPayments : %s" , e.getMessage() ), e);
		}
	}
	
	/*
	public void updateTask(EntityController<ProjectTask> taskController, TaskTreeNode task) throws EntityControllerException {
		Date d1 = task.getCalculatedTaskStart();
		Date d2 = task.getCalculatedTaskEnd();
		
		int diff = TaskUtil.daysBetween(d1, d2) + 1;
		if (task.task.getScheduledStartDate()==null || task.task.getCalendarStartDate()==null ||
				!task.task.getScheduledStartDate().equals(d1) || !task.task.getCalendarStartDate().equals(d1)) {
			task.task.setScheduledStartDate(d1);
			task.task.setCalendarStartDate(d1);
			task.task.setCalenderDuration(diff);
			taskController.mergeTransactionMerge(task.task);
			
		}
		for (TaskTreeNode child:task.getChildren()) {
			updateTask(taskController, child);
		}
	}
	
	public void commitSolution(EntityController<ProjectTask> taskController,ProjectWrapper project, ConcurrentMap<String, Object> solStatus) {
		try {
			Integer i = (Integer) solStatus.get(DONE);
			if (i==null) {
				i=0;
			}
			for (TaskTreeNode task:project.rootTasks) {
				i++;
				solStatus.put(DONE,i);
				updateTask(taskController, task);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
	}

	 * */

}
