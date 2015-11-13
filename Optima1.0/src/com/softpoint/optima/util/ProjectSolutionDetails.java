package com.softpoint.optima.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.softpoint.optima.control.PortfolioController;
import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.WeekendDay;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;

public class ProjectSolutionDetails {
	// if true then origianl, if false then final
	private boolean originalOrFinal;

	private List<Project> projects;
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

	private List<ProjectTask> projectTasks;
	private Map<ProjectTask, Date> tasksEnd;
	Map<String, DailyCashFlowMapEntity> results;

	public ProjectSolutionDetails(boolean originalOrFinal, Project project) {
		super();
		this.originalOrFinal = originalOrFinal;

		projects = new ArrayList<Project>();
		tasksEnd = new HashMap<ProjectTask, Date>();
		results = new HashMap<String, DailyCashFlowMapEntity>();
		projectTasks = new ArrayList<ProjectTask>();

		// initialize all data for easy access later on
		List<ProjectTask> tasks = project.getProjectTasks();
		if (tasks != null && tasks.size() > 0) {
			projects.add(project);
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
			if (project.getAdvancedPaymentAmount().doubleValue() != 0) {
				paymentsCalendar.put(projectStart, project.getAdvancedPaymentAmount().doubleValue());
			}
			List<DaysOff> daysOff = project.getDaysOffs();
			WeekendDay weekEnds = project.getWeekendDays();

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
				entity.setPortfolioId(project.getPortfolio().getPortfolioId());
				entity.setProjectId(project.getProjectId());
				entity.setDay(date);

				double tasksCost = 0d;
				double tasksIncome = 0d;
				if (!(PaymentUtil.isDayOff(date, daysOff) || PaymentUtil.isWeekendDay(date, weekEnds))) {
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
					numberOfDaysSinceLastRequest = 0;
					currentRequestTotal = 0d;
				}

				if (!date.after(lastTaskDate) && !date.before(projectStart)) {
					tasksCost += project.getOverheadPerDay().doubleValue();
				}

				entity.setCashout(tasksCost);
				if (lastDayEntity.getBalance() < 0) {
					entity.setFinanceCost(lastDayEntity.getBalance() * project.getInterestRate().doubleValue());
				}

				Double payment = paymentsCalendar.get(date);
				if (payment == null) {
					payment = 0d;
				} else {
					paymentsCalendar.remove(date);
				}
				if (date.equals(projectEnd)) {
					payment += totalRetained;
				}
				entity.setPayments(payment);

				entity.setBalance(lastDayEntity.getBalance() + payment - tasksCost);
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
				lastDayEntity = entity;
			}
		}
	}

	private Date getTaskStart(ProjectTask currentTask, boolean originalOrFinal2) {
		if (!originalOrFinal2) {
			if (currentTask.getScheduledStartDate()==null) {
				return currentTask.getCalendarStartDate();
			} else {
				return currentTask.getScheduledStartDate();
			}
		} else {
			return currentTask.getTentativeStartDate();
		}
	}

	private void calculateDates(Project currentProject) {

		portfolioStart = currentProject.getPropusedStartDate();
		portfolioEnd = currentProject.getPropusedStartDate();

		for (Project project : currentProject.getPortfolio().getProjects()) {
			List<ProjectTask> tasks = project.getProjectTasks();
			int requestPeriod = project.getPaymentRequestPeriod();
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
			int daysRemainingInThePeriod = daysInLastPeriod == 0 ? 0 : requestPeriod - daysInLastPeriod;
			int shift = daysRemainingInThePeriod + paymentPeriod + 1;

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
			cal.setTime(task.getTentativeStartDate());
			List<DaysOff> daysOff = project.getDaysOffs();
			WeekendDay weekEnds = project.getWeekendDays();
			int duration = task.getDuration();
			Date theDate;
			do {
				theDate = cal.getTime();
				if (!(PaymentUtil.isDayOff(theDate, daysOff) || PaymentUtil.isWeekendDay(theDate, weekEnds))) {
					duration--;
				}
				cal.add(Calendar.DATE, 1);
			} while (duration > 0);
			return theDate;
		}
	}

}
