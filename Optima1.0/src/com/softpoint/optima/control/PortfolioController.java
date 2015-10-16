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
import com.softpoint.optima.util.PaymentUtilBeforeSolving;

/**
 * @author WDARWISH
 *
 */
public class PortfolioController {

	private static final int MAX_CASHFLOW_CELLS = 1095; // Projects range 3years
														// maximum

	public ServerResponse getSchedulePeriod(HttpSession session, Date date, int portfolioId) {

		SchedulePeriod schedulePeriod = new SchedulePeriod();

		try {

			Period paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session, date, portfolioId);
			Period financeSchedulePeriod = PaymentUtil.findFinanceSchedule(session, date, portfolioId);

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

			paymentSchedulePeriod = PaymentUtil.findPaymentSchedule(session, calendar.getTime(), portfolioId);
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

			return new ServerResponse("0", "Success", schedulePeriod);

		} catch (OptimaException e) {

			e.printStackTrace();
			return new ServerResponse("9004", "Error: " + e.getMessage(), null);
		}
	}

	boolean dateExistInDateSet(List<Date> dateSet, Date date) {
		for (Iterator<Date> dateIter = dateSet.iterator(); dateIter.hasNext();) {
			if (date.getTime() == dateIter.next().getTime()) {
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
	public ServerResponse create(HttpSession session, String name, String description) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = new Portfolio();
		portfolio.setPortfolioDescreption(description);
		portfolio.setPortfolioName(name);
		try {
			controller.persist(portfolio);
			return new ServerResponse("0", "Success", portfolio);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0001",
					String.format("Error creating Portfolio %s: %s", name, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param portfolio
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse update(HttpSession session, int key, String name, String description) throws OptimaException {
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
			return new ServerResponse("PORT0002", String.format("Error updating Portfolio %s: %s",
					portfolio != null ? portfolio.getPortfolioName() : "", e.getMessage()), e);
		}
	}

	private boolean isSolved(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = null;
		try {
			portfolio = controller.find(Portfolio.class, portfolioId);
			// verify it is already solved
			List<Project> projects = portfolio.getProjects();
			for (Project project : projects) {
				List<ProjectTask> tasks = project.getProjectTasks();
				for (ProjectTask task : tasks) {
					if (task.getScheduledStartDate() == null) {
						// if at least one task doesn't have scheduled start
						// date then it is not really solved yet
						return false;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	public ServerResponse hasSolution(HttpSession session, int portfolioId) throws OptimaException {
		return new ServerResponse("0", "Success", isSolved(session, portfolioId) ? "TRUE" : "FALSE");
	}

	public ServerResponse getSolution(HttpSession session, int portfolioId) throws OptimaException {
		if (!isSolved(session, portfolioId)) {
			return new ServerResponse("0", "Success", "");
		}
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = null;
		try {
			portfolio = controller.find(Portfolio.class, portfolioId);

			// if all tasks have scheduled start date it means that it is
			// already been solved before
			SimpleDateFormat format = new SimpleDateFormat("dd MMM, yyyy");

			StringBuilder sb = new StringBuilder();
			sb.append("<table class=\"solutionTable\">");
			StringBuilder projSB = new StringBuilder();
			projSB.append("<table class=\"solutionTable\">\r")
					.append("<tr><td>Project Name</td><td>change</td><td>Profit before</td><td>Profit After</td></tr>");

			List<Project> projects = portfolio.getProjects();
			for (Project project : projects) {
				double totalCost = 0;
				double totalIncome = 0;

				double overHeadBefore = 0;
				double overHeadAfter = 0;

				double penaltiesBefore = 0;
				double penaltiesAfter = 0;
				Date lastDate = null;

				sb.append("<tr><td  colspan=\"5\"><b>").append(project.getProjectCode()).append("</b></td></tr>");
				List<ProjectTask> tasks = project.getProjectTasks();
				for (ProjectTask task : tasks) {

					totalCost += task.getUniformDailyCost().doubleValue() * task.getDuration();
					totalIncome += task.getUniformDailyIncome().doubleValue() * task.getDuration();

					sb.append("<tr><td  width=\"30px\"></td><td  width=\"10px\">");
					if (task.getTentativeStartDate().compareTo(task.getScheduledStartDate()) == 0) {
						sb.append("<div style=\"width:16px;height:16px\" class=\"notShiftedTaskLogo\"></div>");
					} else {
						sb.append("<div style=\"width:16px;height:16px\" class=\"shiftedTaskInLogo\"></div>");
					}
					sb.append("</td><td>").append(task.getTaskDescription()).append("</td><td>")
							.append(format.format(task.getTentativeStartDate())).append("</td><td>")
							.append(format.format(task.getScheduledStartDate())).append("</td></tr>\r");

					if (lastDate == null) {
						int duration = TaskController.getDuration(project, task.getTentativeStartDate(),
								task.getDuration());
						lastDate = addDayes(task.getTentativeStartDate(), duration);
					} else {
						int duration = TaskController.getDuration(project, task.getTentativeStartDate(),
								task.getDuration());
						Date newDate = addDayes(task.getTentativeStartDate(), duration);
						if (lastDate.before(newDate)) {
							lastDate = newDate;
						}
					}

				}

				// overhead
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				Date scheduledStart = projectDates[0];
				Date scheduledEnd = projectDates[1];
				overHeadAfter += daysBetween(scheduledStart, scheduledEnd) * project.getOverheadPerDay().doubleValue();

				Date d1 = project.getPropusedStartDate();
				Date d2 = lastDate;
				overHeadBefore += daysBetween(d1, d2) * project.getOverheadPerDay().doubleValue();

				// calculate penalties
				Date finishDate = project.getProposedFinishDate();
				if (finishDate != null) {
					if (finishDate.after(scheduledEnd)) {
						int extraDayes = daysBetween(finishDate, scheduledEnd);
						penaltiesAfter += extraDayes * project.getDelayPenaltyAmount().doubleValue();
					}
					if (finishDate.after(lastDate)) {
						int extraDayes = daysBetween(finishDate, lastDate);
						penaltiesBefore += extraDayes * project.getDelayPenaltyAmount().doubleValue();
					}
				}
				double profitBefore = totalIncome - totalCost - penaltiesBefore - overHeadBefore;
				double profitAfter = totalIncome - totalCost - penaltiesAfter - overHeadAfter;

				projSB.append("<tr><td>").append(project.getProjectCode());

				projSB.append("</td><td>");

				if (profitBefore > profitAfter) {
					projSB.append("<div style=\"width:16px;height:16px\" class=\"decreasetProjectProfitLogo\"></div>");
				} else {
					projSB.append("<div style=\"width:16px;height:16px\" class=\"sameProjectProfitLogo\"></div>");
				}
				projSB.append("</td><td>").append(profitBefore).append("</td><td>").append(profitAfter)
						.append("</td></tr>");
			}
			projSB.append("</table>");

			sb.append("</table>\r").append(projSB.toString());
			return new ServerResponse("0", "Success", sb.toString());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0002", String.format("Error updating Portfolio %s: %s",
					portfolio != null ? portfolio.getPortfolioName() : "", e.getMessage()), e);
		}
	}

	public static Date addDayes(Date date, int numOfDays) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, numOfDays);
		return calendar.getTime();
	}

	private static int daysBetween(Date d1, Date d2) {
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

	public ServerResponse getSolutionAsCSV(HttpSession session, int portfolioId) throws OptimaException {
		if (!isSolved(session, portfolioId)) {
			return new ServerResponse("0", "Success", "");
		}
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Portfolio portfolio = null;
		try {
			portfolio = controller.find(Portfolio.class, portfolioId);

			// if all tasks have scheduled start date it means that it is
			// already been solved before
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");

			StringBuilder sb = new StringBuilder();
			StringBuilder projSB = new StringBuilder();
			projSB.append("\rProject Name,change,Profit before,Profit After\r");

			List<Project> projects = portfolio.getProjects();
			for (Project project : projects) {
				double totalCost = 0;
				double totalIncome = 0;

				double overHeadBefore = 0;
				double overHeadAfter = 0;

				double penaltiesBefore = 0;
				double penaltiesAfter = 0;
				Date lastDate = null;

				sb.append(project.getProjectCode()).append("\r");
				List<ProjectTask> tasks = project.getProjectTasks();
				for (ProjectTask task : tasks) {
					totalCost += task.getUniformDailyCost().doubleValue() * task.getDuration();
					totalIncome += task.getUniformDailyIncome().doubleValue() * task.getDuration();

					sb.append(",");
					if (task.getTentativeStartDate().compareTo(task.getScheduledStartDate()) == 0) {
						sb.append("✔,");
					} else {
						sb.append(">,");
					}
					sb.append(task.getTaskDescription()).append(",").append(format.format(task.getTentativeStartDate()))
							.append(",").append(format.format(task.getScheduledStartDate())).append("\r");

					if (lastDate == null) {
						int duration = TaskController.getDuration(project, task.getTentativeStartDate(),
								task.getDuration());
						lastDate = addDayes(task.getTentativeStartDate(), duration);
					} else {
						int duration = TaskController.getDuration(project, task.getTentativeStartDate(),
								task.getDuration());
						Date newDate = addDayes(task.getTentativeStartDate(), duration);
						if (lastDate.before(newDate)) {
							lastDate = newDate;
						}
					}

				}

				// overhead
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				Date scheduledStart = projectDates[0];
				Date scheduledEnd = projectDates[1];
				overHeadAfter += daysBetween(scheduledStart, scheduledEnd) * project.getOverheadPerDay().doubleValue();

				Date d1 = project.getPropusedStartDate();
				Date d2 = lastDate;
				overHeadBefore += daysBetween(d1, d2) * project.getOverheadPerDay().doubleValue();

				// calculate penalties
				Date finishDate = project.getProposedFinishDate();
				if (finishDate != null) {
					if (finishDate.after(scheduledEnd)) {
						int extraDayes = daysBetween(finishDate, scheduledEnd);
						penaltiesAfter += extraDayes * project.getDelayPenaltyAmount().doubleValue();
					}
					if (finishDate.after(lastDate)) {
						int extraDayes = daysBetween(finishDate, lastDate);
						penaltiesBefore += extraDayes * project.getDelayPenaltyAmount().doubleValue();
					}
				}
				double profitBefore = totalIncome - totalCost - penaltiesBefore - overHeadBefore;
				double profitAfter = totalIncome - totalCost - penaltiesAfter - overHeadAfter;

				projSB.append(project.getProjectCode());
				projSB.append(",");

				if (profitBefore > profitAfter) {
					projSB.append("▽");
				} else {
					projSB.append("✔");
				}
				projSB.append(",").append(profitBefore).append(",").append(profitAfter).append("\r");

			}

			sb.append("\r").append(projSB.toString());
			return new ServerResponse("0", "Success", sb.toString());
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0002", String.format("Error updating Portfolio %s: %s",
					portfolio != null ? portfolio.getPortfolioName() : "", e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse find(HttpSession session, Integer key) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class, key);
			return new ServerResponse("0", "Success", portfolio);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0003",
					String.format("Error looking up Portfolio %d: %s", key, e.getMessage()), e);
		}
	}

	/**
	 * @param session
	 * @param key
	 * @return
	 * @throws OptimaException
	 */
	public ServerResponse remove(HttpSession session, Integer key) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			controller.remove(Portfolio.class, key);
			return new ServerResponse("0", "Success", null);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0004", String.format("Error removing Portfolio %d: %s", key, e.getMessage()),
					e);
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
			return new ServerResponse("PORT0005", String.format("Error loading portfolios : %s", e.getMessage()), e);
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
			return new ServerResponse("PORT0006", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	public Date[] getPortfolioDateRangeWithLastPayment(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Date[] portofolioDateRanges;
		try {
			portofolioDateRanges = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
			Date endDate = portofolioDateRanges[1];
			ProjectController projectController = new ProjectController();
			SchedulePeriod currentPeriod = projectController.getCurrentPeriodBoundriesNew(session, endDate,
					portfolioId);
			portofolioDateRanges[1] = currentPeriod.getCurrent().getDateTo();

			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			List<Project> projects = portfolio.getProjects();
			for (Project project : projects) {
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				if (projectDates[1].getTime() > currentPeriod.getCurrent().getDateFrom().getTime()) {
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
			return new ServerResponse("PORT0006", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	public ServerResponse getPortfolioCashFlowDataNew(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();

			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);

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

				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
					entity.setPortfolioId(portfolioId);
					entity.setProjectId(currentProject.getProjectId());
					entity.setDay(date);
					boolean includeOverhead = false;
					if ((date.equals(projectStartDate) || date.after(projectStartDate))
							&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
						includeOverhead = true;
					}
					entity.setCashout(PaymentUtil.getDateTasksCashout(currentProject, date, includeOverhead));
					entity.setFinanceCost(PaymentUtil.getDateFinanceCost(currentProject, date, results));
					entity.setPayments(PaymentUtil.getProjectPaymentstNew(session, currentProject, date));
					entity.setBalance(PaymentUtil.getBalance(date, entity, results));
					entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

					results.put(PaymentUtil.getDatesOnly(date) + "," + currentProject.getProjectId(), entity);
				}
			}

			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0007", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}

	}

	public ServerResponse getPortfolioCashFlowData(HttpSession session, int portfolioId) throws OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();

			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);

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

				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
					entity.setPortfolioId(portfolioId);
					entity.setProjectId(currentProject.getProjectId());
					entity.setDay(date);
					boolean includeOverhead = false;
					if ((date.equals(projectStartDate) || date.after(projectStartDate))
							&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
						includeOverhead = true;
					}
					entity.setCashout(PaymentUtil.getDateTasksCashout(currentProject, date, includeOverhead));
					entity.setFinanceCost(PaymentUtil.getDateFinanceCost(currentProject, date, results));
					entity.setPayments(PaymentUtil.getProjectPaymentst(currentProject, date));
					entity.setBalance(PaymentUtil.getBalance(date, entity, results));
					entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

					results.put(PaymentUtil.getDatesOnly(date) + "," + currentProject.getProjectId(), entity);
				}
			}

			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0007", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}

	}

	public ServerResponse getProjectCashFlowDataNew(HttpSession session, int projectId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();

			Project project = controller.find(Project.class, projectId);

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
				if ((date.equals(projectStartDate) || date.after(projectStartDate))
						&& (date.before(lastTaskEndDate) || date.equals(lastTaskEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date, includeOverhead));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				double payment = PaymentUtil.getProjectPaymentstNew(session, project, date);
				double originalPayment = payment / (1.0 - project.getRetainedPercentage().doubleValue()
						- project.getAdvancedPaymentPercentage().doubleValue());

				if (!start.equals(end)) {
					double retainedAmount = originalPayment * project.getRetainedPercentage().doubleValue();
					double advancedAmount = originalPayment * project.getAdvancedPaymentPercentage().doubleValue();
					totalRetained = totalRetained + retainedAmount;
					entity.setPayments(payment);
				} else
					entity.setPayments(payment + totalRetained);

				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}

	}

	public ServerResponse getProjectCashFlowChart(HttpSession session, int projectId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();

			Project project = controller.find(Project.class, projectId);

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
				if ((date.equals(projectStartDate) || date.after(projectStartDate))
						&& (date.before(lastTaskEndDate) || date.equals(lastTaskEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date, includeOverhead));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				double payment = PaymentUtil.getProjectPaymentstNew(session, project, date);
				double originalPayment = payment / (1.0 - project.getRetainedPercentage().doubleValue()
						- project.getAdvancedPaymentPercentage().doubleValue());

				if (!start.equals(end)) {
					double retainedAmount = originalPayment * project.getRetainedPercentage().doubleValue();
					double advancedAmount = originalPayment * project.getAdvancedPaymentPercentage().doubleValue();
					totalRetained = totalRetained + retainedAmount;
					entity.setPayments(payment);
				} else
					entity.setPayments(payment + totalRetained);

				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}

	}

	public ServerResponse getProjectCashFlowData(HttpSession session, int projectId) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();

			Project project = controller.find(Project.class, projectId);

			Date[] portoflioDateRange = PaymentUtil.getPortofolioDateRanges(controller,
					project.getPortfolio().getPortfolioId());

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
				if ((date.equals(projectStartDate) || date.after(projectStartDate))
						&& (date.before(lastTaskEndDate) || date.equals(lastTaskEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date, includeOverhead));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				entity.setPayments(PaymentUtil.getProjectPaymentst(project, date));
				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());

				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			return new ServerResponse("0", "Success", results);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0008", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}

	}

	public ServerResponse getCashoutPreviousPeriodNew(HttpSession session, int portfolioId, Date from) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			double openBalance = PaymentUtil.getPortfolioOpenBalanceNew(session, portfolio, from);

			return new ServerResponse("0", "Success", openBalance);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	public ServerResponse getCashoutPreviousPeriod(HttpSession session, int portfolioId, Date from) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			double openBalance = PaymentUtil.getPortfolioOpenBalance(session, portfolio, from);

			return new ServerResponse("0", "Success", openBalance);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	public ServerResponse getCashoutCurrentPeriodNew(HttpSession session, int portfolioId, Date from, Date to) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			List<PeriodCashout> periodCashouts = new ArrayList<PeriodCashout>();
			int numberOfDays = PaymentUtil.daysBetween(from, to);
			int effictiveNumberOfDays;
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			List<Project> projects = portfolio.getProjects();
			Calendar calendar = Calendar.getInstance();
			List<ProjectTask> tasks = null;
			Date taskEndDate;
			Date taskDate;

			Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;

			for (Project currentProject : projects) {
				// reset for each project
				double taskCostCounter = 0;
				double cashoutCounter = 0;
				calendar.setTime(from);
				calendar.add(Calendar.DATE, -1);
				Date[] projectDates = PaymentUtil.getProjectExtendedDateRanges(controller,
						currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				if (projectStartDate != null) { // Null means there are no tasks
												// for that project
					cashFlowInfo = PaymentUtil.getProjectCashFlowDataNew(session, currentProject.getProjectId(),
							projectStartDate, calendar.getTime());
					tasks = currentProject.getProjectTasks();

					// 1. Task already started. schedule start date > start
					// 2. task end date (schedule start date + calendar
					// duration) > end
					for (ProjectTask currentTask : tasks) {

						taskDate = PaymentUtil.getTaskDate(currentTask);
						calendar.setTime(taskDate);
						calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
						taskEndDate = calendar.getTime();

						if (taskDate.before(from) && (taskEndDate.after(from) || taskEndDate.equals(from))) {

							int taskDaysAfterPeriodStart = PaymentUtil.daysBetween(from, taskEndDate) + 1; // daysBetween
																											// exclude
																											// last
																											// day.
																											// It
																											// should
																											// be
																											// included
																											// here
							effictiveNumberOfDays = taskDaysAfterPeriodStart;
							if (numberOfDays < taskDaysAfterPeriodStart) {
								effictiveNumberOfDays = numberOfDays;
							}
							Calendar endEffectiveDate = Calendar.getInstance();
							endEffectiveDate.setTime(from);
							endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays);
							int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, from,
									endEffectiveDate.getTime()); // What if
																	// taskEndDate
																	// > to?
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
						overheadDays = PaymentUtil.daysBetween(PaymentUtil.maxDate(from, projectStartDate), to);
					}
					double overhead = currentProject.getOverheadPerDay().doubleValue() * overheadDays;
					cashoutCounter += overhead;

					Calendar prevDay = Calendar.getInstance();
					prevDay.setTime(from);
					DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from, cashFlowInfo,
							currentProject.getProjectId());

					double firstDayBalance = previousDay == null ? 0 : previousDay.getBalance();
					double firstDayBalanceToReturn = firstDayBalance;
					double projectPayment = PaymentUtil.getProjectPaymentstNew(session, currentProject, from);

					periodCashouts.add(new PeriodCashout(currentProject.getProjectId(), currentProject.getProjectCode(),
							taskCostCounter, overhead, cashoutCounter, firstDayBalanceToReturn, projectPayment));
				}
			}

			return new ServerResponse("0", "Success", periodCashouts);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	public ServerResponse getCashoutCurrentPeriod(HttpSession session, int portfolioId, Date from, Date to) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {
			List<PeriodCashout> periodCashouts = new ArrayList<PeriodCashout>();
			int numberOfDays = PaymentUtil.daysBetween(from, to);
			int effictiveNumberOfDays;
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			List<Project> projects = portfolio.getProjects();
			Calendar calendar = Calendar.getInstance();
			List<ProjectTask> tasks = null;
			Date taskEndDate;
			Date taskDate;

			Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;

			for (Project currentProject : projects) {
				// reset for each project
				double taskCostCounter = 0;
				double cashoutCounter = 0;
				calendar.setTime(from);
				calendar.add(Calendar.DATE, -1);
				Date[] projectDates = PaymentUtil.getProjectExtendedDateRanges(controller,
						currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				if (projectStartDate != null) { // Null means there are no tasks
												// for that project
					cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, currentProject.getProjectId(),
							projectStartDate, calendar.getTime());
					tasks = currentProject.getProjectTasks();

					// 1. Task already started. schedule start date > start
					// 2. task end date (schedule start date + calendar
					// duration) > end
					for (ProjectTask currentTask : tasks) {

						taskDate = PaymentUtil.getTaskDate(currentTask);
						calendar.setTime(taskDate);
						calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
						taskEndDate = calendar.getTime();

						if (taskDate.before(from) && (taskEndDate.after(from) || taskEndDate.equals(from))) {

							int taskDaysAfterPeriodStart = PaymentUtil.daysBetween(from, taskEndDate) + 1; // daysBetween
																											// exclude
																											// last
																											// day.
																											// It
																											// should
																											// be
																											// included
																											// here
							effictiveNumberOfDays = taskDaysAfterPeriodStart;
							if (numberOfDays < taskDaysAfterPeriodStart) {
								effictiveNumberOfDays = numberOfDays;
							}
							Calendar endEffectiveDate = Calendar.getInstance();
							endEffectiveDate.setTime(from);
							endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays);
							int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, from,
									endEffectiveDate.getTime()); // What if
																	// taskEndDate
																	// > to?
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
						overheadDays = PaymentUtil.daysBetween(PaymentUtil.maxDate(from, projectStartDate), to);
					}
					double overhead = currentProject.getOverheadPerDay().doubleValue() * overheadDays;
					cashoutCounter += overhead;

					Calendar prevDay = Calendar.getInstance();
					prevDay.setTime(from);
					DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from, cashFlowInfo,
							currentProject.getProjectId());

					double firstDayBalance = previousDay == null ? 0 : previousDay.getBalance();
					double firstDayBalanceToReturn = firstDayBalance;
					double projectPayment = PaymentUtil.getProjectPaymentst(currentProject, from);

					periodCashouts.add(new PeriodCashout(currentProject.getProjectId(), currentProject.getProjectCode(),
							taskCostCounter, overhead, cashoutCounter, firstDayBalanceToReturn, projectPayment));
				}
			}

			return new ServerResponse("0", "Success", periodCashouts);
		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PORT0009", String.format("Error loading portfolios : %s", e.getMessage()), e);
		}
	}

	private String getHorizontalIndexCellAt(int index) {
		String horizontalIndex = "";
		int x;
		char c;
		while (index > 0) {
			x = index % 26;
			index = index / 26;
			c = (char) ('A' + (x - 1));
			horizontalIndex = c + horizontalIndex;
		}
		return horizontalIndex;
	}

	public ServerResponse downloadCashflowGraph(HttpSession session, int portfolioId) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Workbook workbook = null;
		try {

			// Load graph template
			workbook = new XSSFWorkbook(OPCPackage.open(new FileInputStream(session.getServletContext()
					.getRealPath("/WEB-INF" + File.separator + "/files" + File.separator + "template.xlsx"))));
			CreationHelper createHelper = workbook.getCreationHelper();

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			Date[] portoflioDateRange = PaymentUtil.getPortofolioDateRanges(controller, portfolioId);
			List<Project> projects = portfolio.getProjects();

			Sheet sheet = workbook.getSheetAt(0);
			Row portfolioNameRow = sheet.getRow(0);
			Cell portfolioNameCell = portfolioNameRow.getCell(4);
			portfolioNameCell.setCellValue(portfolio.getPortfolioName());

			Map<Date, Double> totalCashOut = new HashMap<Date, Double>();
			Map<Date, Double> totalFinanceCost = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceSolution = new HashMap<Date, Double>();
			Map<Date, Double> totalPayments = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceInitial = new HashMap<Date, Double>();
			Map<Date, Double> totalFinance = new HashMap<Date, Double>();

			int proj = 1;
			for (Project currentProject : projects) {

				sheet = workbook.getSheetAt(proj);
				Row projectNameRow = sheet.getRow(0);
				Cell projectNameCell = projectNameRow.getCell(4);
				projectNameCell.setCellValue(currentProject.getProjectCode());

				workbook.setSheetName(workbook.getSheetIndex(sheet), currentProject.getProjectCode());
				CellStyle cellStyle = workbook.createCellStyle();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
				for (int row = 2; row < 9; row++)
					for (int col = 2; col < MAX_CASHFLOW_CELLS; col++) {
						Cell currentCell = sheet.getRow(row).getCell(col);
						if (currentCell != null)
							sheet.getRow(row).getCell(col).setCellValue("");
					}

				Row dayRow = sheet.getRow(2);
				Row cashOutRow = sheet.getRow(3);
				Row financeCostRow = sheet.getRow(4);
				Row balanceSolutionRow = sheet.getRow(5);
				Row paymentsRow = sheet.getRow(6);
				Row financeRow = sheet.getRow(7);
				Row balanceInitialRow = sheet.getRow(8);

				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);
				Calendar end = Calendar.getInstance();
				end.setTime(portoflioDateRange[1]);

				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				Date projectEndDate = projectDates[1];

				int index = 2;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
					entity.setPortfolioId(portfolioId);
					entity.setProjectId(currentProject.getProjectId());
					entity.setDay(date);
					boolean includeOverhead = false;
					if ((date.equals(projectStartDate) || date.after(projectStartDate))
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

					if (dayCell == null)
						dayCell = dayRow.createCell(index);
					if (cashOutCell == null)
						cashOutCell = cashOutRow.createCell(index);
					if (financeCostCell == null)
						financeCostCell = financeCostRow.createCell(index);
					if (balanceSolutionCell == null)
						balanceSolutionCell = balanceSolutionRow.createCell(index);
					if (paymentsCell == null)
						paymentsCell = paymentsRow.createCell(index);
					if (financeCell == null)
						financeCell = financeRow.createCell(index);
					if (balanceInitialCell == null)
						balanceInitialCell = balanceInitialRow.createCell(index);

					dayCell.setCellStyle(cellStyle);
					dayCell.setCellValue(date);

					double cashOutValue = PaymentUtil.getDateTasksCashout(currentProject, date, includeOverhead);
					cashOutCell.setCellValue(cashOutValue);
					entity.setCashout(cashOutValue);
					if (totalCashOut.get(date) == null)
						totalCashOut.put(date, cashOutValue);
					else
						totalCashOut.put(date, totalCashOut.get(date) + cashOutValue);

					double financeCostValue = PaymentUtil.getDateFinanceCost(currentProject, date, results);
					financeCostCell.setCellValue(financeCostValue);
					entity.setFinanceCost(financeCostValue);
					if (totalFinanceCost.get(date) == null)
						totalFinanceCost.put(date, financeCostValue);
					else
						totalFinanceCost.put(date, totalFinanceCost.get(date) + financeCostValue);

					double paymentsValue = PaymentUtil.getProjectPaymentst(currentProject, date);
					paymentsCell.setCellValue(paymentsValue);
					entity.setPayments(paymentsValue);
					if (totalPayments.get(date) == null)
						totalPayments.put(date, paymentsValue);
					else
						totalPayments.put(date, totalPayments.get(date) + paymentsValue);

					double balanceSolutionValue = PaymentUtil.getBalance(date, entity, results);
					balanceSolutionCell.setCellValue(balanceSolutionValue);
					entity.setBalance(balanceSolutionValue);
					if (totalBalanceSolution.get(date) == null)
						totalBalanceSolution.put(date, balanceSolutionValue);
					else
						totalBalanceSolution.put(date, totalBalanceSolution.get(date) + balanceSolutionValue);

					double finance = PaymentUtil.getFinanceLimit(session, portfolioId, date);
					financeCell.setCellValue(-1 * finance);
					totalFinance.put(date, -1 * finance);

					balanceInitialCell.setCellValue(entity.getBalance() + entity.getFinanceCost());
					if (totalBalanceInitial.get(date) == null)
						totalBalanceInitial.put(date, entity.getBalance() + entity.getFinanceCost());
					else
						totalBalanceInitial.put(date,
								totalBalanceInitial.get(date) + entity.getBalance() + entity.getFinanceCost());

					results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(), entity);

					index++;
				}

				String horizontalIndex = getHorizontalIndexCellAt(index);
				String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index - 1);

				Name rangeDays = workbook.getName("Days_Proj" + proj);
				Name rangeDaysX = workbook.getName("Days_Proj" + proj + "X");
				String reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndex + "$3";
				String referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$"
						+ horizontalIndexBeforeLast + "$3";
				rangeDays.setRefersToFormula(reference);
				rangeDaysX.setRefersToFormula(referenceX);

				Name rangeBalanceSol = workbook.getName("Balance_Solution_Proj" + proj);
				Name rangeBalanceSolX = workbook.getName("Balance_Solution_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$6:$" + horizontalIndex + "$6";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$6:$" + horizontalIndex + "$6";
				rangeBalanceSol.setRefersToFormula(reference);
				rangeBalanceSolX.setRefersToFormula(referenceX);

				Name rangeFinance = workbook.getName("Finance_Proj" + proj);
				Name rangeFinanceX = workbook.getName("Finance_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$8:$" + horizontalIndex + "$8";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$8:$" + horizontalIndex + "$8";
				rangeFinance.setRefersToFormula(reference);
				rangeFinanceX.setRefersToFormula(referenceX);

				Name rangeBalanceInit = workbook.getName("Initial_Balance_Proj" + proj);
				Name rangeBalanceInitX = workbook.getName("Initial_Balance_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$9:$" + horizontalIndex + "$9";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$9:$" + horizontalIndex + "$9";
				rangeBalanceInit.setRefersToFormula(reference);
				rangeBalanceInitX.setRefersToFormula(referenceX);

				proj++;
			}

			// Portfolio
			sheet = workbook.getSheetAt(0);
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
			for (int row = 2; row < 9; row++)
				for (int col = 2; col < MAX_CASHFLOW_CELLS; col++) {
					Cell currentCell = sheet.getRow(row).getCell(col);
					if (currentCell != null)
						sheet.getRow(row).getCell(col).setCellValue("");
				}

			Row dayRow = sheet.getRow(2);
			Row cashOutRow = sheet.getRow(3);
			Row financeCostRow = sheet.getRow(4);
			Row balanceSolutionRow = sheet.getRow(5);
			Row paymentsRow = sheet.getRow(6);
			Row financeRow = sheet.getRow(7);
			Row balanceInitialRow = sheet.getRow(8);

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

				if (dayCell == null)
					dayCell = dayRow.createCell(index);
				if (cashOutCell == null)
					cashOutCell = cashOutRow.createCell(index);
				if (financeCostCell == null)
					financeCostCell = financeCostRow.createCell(index);
				if (balanceSolutionCell == null)
					balanceSolutionCell = balanceSolutionRow.createCell(index);
				if (paymentsCell == null)
					paymentsCell = paymentsRow.createCell(index);
				if (financeCell == null)
					financeCell = financeRow.createCell(index);
				if (balanceInitialCell == null)
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
			String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index - 1);

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

			// Remove extra projects from template
			for (int p = 10; p >= proj; p--) {
				if (workbook.getSheetAt(p) != null)
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
			return new ServerResponse("PROJ0003",
					String.format("Error looking up portfolio %d: %s", portfolioId, e.getMessage()), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public ServerResponse downloadCashflowGraphNew(HttpSession session, int portfolioId) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Workbook workbook = null;
		try {

			// Load graph template
			workbook = new XSSFWorkbook(OPCPackage.open(new FileInputStream(session.getServletContext()
					.getRealPath("/WEB-INF" + File.separator + "/files" + File.separator + "template.xlsx"))));
			CreationHelper createHelper = workbook.getCreationHelper();

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			Date[] portoflioDateRange = getPortfolioDateRangeWithLastPayment(session, portfolioId);
			List<Project> projects = portfolio.getProjects();

			Sheet sheet = workbook.getSheetAt(0);
			Row portfolioNameRow = sheet.getRow(0);
			Cell portfolioNameCell = portfolioNameRow.getCell(4);
			portfolioNameCell.setCellValue(portfolio.getPortfolioName());

			Map<Date, Double> totalCashOut = new HashMap<Date, Double>();
			Map<Date, Double> totalFinanceCost = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceSolution = new HashMap<Date, Double>();
			Map<Date, Double> totalPayments = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceInitial = new HashMap<Date, Double>();
			Map<Date, Double> totalFinance = new HashMap<Date, Double>();

			int proj = 1;
			for (Project currentProject : projects) {

				sheet = workbook.getSheetAt(proj);
				Row projectNameRow = sheet.getRow(0);
				Cell projectNameCell = projectNameRow.getCell(4);
				projectNameCell.setCellValue(currentProject.getProjectCode());

				workbook.setSheetName(workbook.getSheetIndex(sheet), currentProject.getProjectCode());
				CellStyle cellStyle = workbook.createCellStyle();
				cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
				for (int row = 2; row < 9; row++)
					for (int col = 2; col < MAX_CASHFLOW_CELLS; col++) {
						Cell currentCell = sheet.getRow(row).getCell(col);
						if (currentCell != null)
							sheet.getRow(row).getCell(col).setCellValue("");
					}

				Row dayRow = sheet.getRow(2);
				Row cashOutRow = sheet.getRow(3);
				Row financeCostRow = sheet.getRow(4);
				Row balanceSolutionRow = sheet.getRow(5);
				Row paymentsRow = sheet.getRow(6);
				Row financeRow = sheet.getRow(7);
				Row balanceInitialRow = sheet.getRow(8);

				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);
				Calendar end = Calendar.getInstance();
				end.setTime(portoflioDateRange[1]);

				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId());
				Date projectStartDate = projectDates[0];
				Date projectEndDate = projectDates[1];

				int index = 2;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
					entity.setPortfolioId(portfolioId);
					entity.setProjectId(currentProject.getProjectId());
					entity.setDay(date);
					boolean includeOverhead = false;
					if ((date.equals(projectStartDate) || date.after(projectStartDate))
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

					if (dayCell == null)
						dayCell = dayRow.createCell(index);
					if (cashOutCell == null)
						cashOutCell = cashOutRow.createCell(index);
					if (financeCostCell == null)
						financeCostCell = financeCostRow.createCell(index);
					if (balanceSolutionCell == null)
						balanceSolutionCell = balanceSolutionRow.createCell(index);
					if (paymentsCell == null)
						paymentsCell = paymentsRow.createCell(index);
					if (financeCell == null)
						financeCell = financeRow.createCell(index);
					if (balanceInitialCell == null)
						balanceInitialCell = balanceInitialRow.createCell(index);

					dayCell.setCellStyle(cellStyle);
					dayCell.setCellValue(date);

					double cashOutValue = PaymentUtil.getDateTasksCashout(currentProject, date, includeOverhead);
					cashOutCell.setCellValue(cashOutValue);
					entity.setCashout(cashOutValue);
					if (totalCashOut.get(date) == null)
						totalCashOut.put(date, cashOutValue);
					else
						totalCashOut.put(date, totalCashOut.get(date) + cashOutValue);

					double financeCostValue = PaymentUtil.getDateFinanceCost(currentProject, date, results);
					financeCostCell.setCellValue(financeCostValue);
					entity.setFinanceCost(financeCostValue);
					if (totalFinanceCost.get(date) == null)
						totalFinanceCost.put(date, financeCostValue);
					else
						totalFinanceCost.put(date, totalFinanceCost.get(date) + financeCostValue);

					double paymentsValue = PaymentUtil.getProjectPaymentstNew(session, currentProject, date);
					paymentsCell.setCellValue(paymentsValue);
					entity.setPayments(paymentsValue);
					if (totalPayments.get(date) == null)
						totalPayments.put(date, paymentsValue);
					else
						totalPayments.put(date, totalPayments.get(date) + paymentsValue);

					double balanceSolutionValue = PaymentUtil.getBalance(date, entity, results);
					balanceSolutionCell.setCellValue(balanceSolutionValue);
					entity.setBalance(balanceSolutionValue);
					if (totalBalanceSolution.get(date) == null)
						totalBalanceSolution.put(date, balanceSolutionValue);
					else
						totalBalanceSolution.put(date, totalBalanceSolution.get(date) + balanceSolutionValue);

					double finance = PaymentUtil.getFinanceLimit(session, portfolioId, date);
					financeCell.setCellValue(-1 * finance);
					totalFinance.put(date, -1 * finance);

					balanceInitialCell.setCellValue(entity.getBalance() + entity.getFinanceCost());
					if (totalBalanceInitial.get(date) == null)
						totalBalanceInitial.put(date, entity.getBalance() + entity.getFinanceCost());
					else
						totalBalanceInitial.put(date,
								totalBalanceInitial.get(date) + entity.getBalance() + entity.getFinanceCost());

					results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(), entity);

					index++;
				}

				String horizontalIndex = getHorizontalIndexCellAt(index);
				String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index - 1);

				Name rangeDays = workbook.getName("Days_Proj" + proj);
				Name rangeDaysX = workbook.getName("Days_Proj" + proj + "X");
				String reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$" + horizontalIndex + "$3";
				String referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$C$3:$"
						+ horizontalIndexBeforeLast + "$3";
				rangeDays.setRefersToFormula(reference);
				rangeDaysX.setRefersToFormula(referenceX);

				Name rangeBalanceSol = workbook.getName("Balance_Solution_Proj" + proj);
				Name rangeBalanceSolX = workbook.getName("Balance_Solution_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$6:$" + horizontalIndex + "$6";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$6:$" + horizontalIndex + "$6";
				rangeBalanceSol.setRefersToFormula(reference);
				rangeBalanceSolX.setRefersToFormula(referenceX);

				Name rangeFinance = workbook.getName("Finance_Proj" + proj);
				Name rangeFinanceX = workbook.getName("Finance_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$8:$" + horizontalIndex + "$8";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$8:$" + horizontalIndex + "$8";
				rangeFinance.setRefersToFormula(reference);
				rangeFinanceX.setRefersToFormula(referenceX);

				Name rangeBalanceInit = workbook.getName("Initial_Balance_Proj" + proj);
				Name rangeBalanceInitX = workbook.getName("Initial_Balance_Proj" + proj + "X");
				reference = "\'" + currentProject.getProjectCode() + "\'" + "!$C$9:$" + horizontalIndex + "$9";
				referenceX = "\'" + currentProject.getProjectCode() + "\'" + "!$D$9:$" + horizontalIndex + "$9";
				rangeBalanceInit.setRefersToFormula(reference);
				rangeBalanceInitX.setRefersToFormula(referenceX);

				proj++;
			}

			// Portfolio
			sheet = workbook.getSheetAt(0);
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd mmm yyyy"));
			for (int row = 2; row < 9; row++)
				for (int col = 2; col < MAX_CASHFLOW_CELLS; col++) {
					Cell currentCell = sheet.getRow(row).getCell(col);
					if (currentCell != null)
						sheet.getRow(row).getCell(col).setCellValue("");
				}

			Row dayRow = sheet.getRow(2);
			Row cashOutRow = sheet.getRow(3);
			Row financeCostRow = sheet.getRow(4);
			Row balanceSolutionRow = sheet.getRow(5);
			Row paymentsRow = sheet.getRow(6);
			Row financeRow = sheet.getRow(7);
			Row balanceInitialRow = sheet.getRow(8);

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

				if (dayCell == null)
					dayCell = dayRow.createCell(index);
				if (cashOutCell == null)
					cashOutCell = cashOutRow.createCell(index);
				if (financeCostCell == null)
					financeCostCell = financeCostRow.createCell(index);
				if (balanceSolutionCell == null)
					balanceSolutionCell = balanceSolutionRow.createCell(index);
				if (paymentsCell == null)
					paymentsCell = paymentsRow.createCell(index);
				if (financeCell == null)
					financeCell = financeRow.createCell(index);
				if (balanceInitialCell == null)
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
			String horizontalIndexBeforeLast = getHorizontalIndexCellAt(index - 1);

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

			// Remove extra projects from template
			for (int p = 10; p >= proj; p--) {
				if (workbook.getSheetAt(p) != null)
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
			return new ServerResponse("PROJ0003",
					String.format("Error looking up portfolio %d: %s", portfolioId, e.getMessage()), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static final int SVG_HEIGHT = 500;
	private static final int SVG_PADDING = 30;
	private static final int DAY_WIDTH = 10;
	private static final int FIRST_DAY = 120;

	private static final String FINANCE_HORIZONTAL_LINE = "<path d=\"M %d %d l %d 0\" stroke=\"lightgrey\" stroke-width=\"1\" fill=\"none\"></path><text x=\"10\" y=\"%d\" font-size=\"12\" stroke=\"black\">%.2f</text>\r";
	private static final String DATE_VERTICAL = "<text transform=\"translate(%d %d) rotate(-90)\" font-size=\"12\" fill=\"gray\" fill-opacity=\"0.7\">%s</text>\r";
	
	private static final String FINANCE_PATH = "<path d=\"%s\" stroke=\"orange\" stroke-width=\"3\" fill=\"none\"></path><path d=\"M 250 498 l 75 0\" stroke=\"orange\" stroke-width=\"3\" fill=\"none\"></path><text x=\"250\" y=\"492\" font-size=\"18\" stroke=\"orange\">Finance</text>";
	private static final String ORIGINAL_PATH = "<path d=\"%s\" stroke=\"lightblue\" stroke-width=\"4\" stroke-dasharray=\"5,5\" stroke-opacity=\"0.8\" fill=\"none\"></path><path d=\"M 400 498 l 75 0\" stroke=\"lightblue\" stroke-width=\"4\" stroke-dasharray=\"5,5\" stroke-opacity=\"0.6\" fill=\"none\"></path><text x=\"400\" y=\"492\" font-size=\"18\" stroke=\"lightblue\">Original</text>";
	private static final String CURRENT_PATH = "<path d=\"%s\" stroke=\"green\" stroke-width=\"2\" fill=\"none\"></path><path d=\"M 550 498 l 75 0\" stroke=\"green\" stroke-width=\"2\" fill=\"none\"></path><text x=\"550\" y=\"492\" font-size=\"18\" stroke=\"green\">Final</text>";
	
	/*
	 * returns SVG for the flow chart
	 */
	public ServerResponse getCashFlowSVGGraph(HttpSession session, int portfolioId) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		try {

			boolean solved = isSolved(session, portfolioId);

			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			Map<String, DailyCashFlowMapEntity> results2 = new HashMap<String, DailyCashFlowMapEntity>();
			Portfolio portfolio = controller.find(Portfolio.class, portfolioId);
			Date[] portoflioDateRange = getPortfolioDateRangeWithLastPayment(session, portfolioId);
			List<Project> projects = portfolio.getProjects();

			Map<Date, Double> totalCashOut = new HashMap<Date, Double>();
			Map<Date, Double> totalFinanceCost = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceSolution = new HashMap<Date, Double>();
			Map<Date, Double> totalPayments = new HashMap<Date, Double>();
			Map<Date, Double> totalBalanceInitial = new HashMap<Date, Double>();
			Map<Date, Double> totalFinance = new HashMap<Date, Double>();

			Map<Date, Double> totalOriginalCashOut = new HashMap<Date, Double>();
			Map<Date, Double> totalOriginalFinanceCost = new HashMap<Date, Double>();
			Map<Date, Double> totalOriginalBalanceSolution = new HashMap<Date, Double>();
			Map<Date, Double> totalOriginalPayments = new HashMap<Date, Double>();
			Map<Date, Double> totalOriginalBalanceInitial = new HashMap<Date, Double>();
			Map<Date, Double> totalOriginalFinance = new HashMap<Date, Double>();

			for (Project currentProject : projects) {
				calculateProjectVars(currentProject, portoflioDateRange, controller, portfolioId, totalCashOut,
						totalFinanceCost, session, totalPayments, totalBalanceSolution, totalFinance,
						totalBalanceInitial, results, true);
				if (solved) {
					calculateProjectVars(currentProject, portoflioDateRange, controller, portfolioId,
							totalOriginalCashOut, totalOriginalFinanceCost, session, totalOriginalPayments,
							totalOriginalBalanceSolution, totalOriginalFinance, totalOriginalBalanceInitial, results2,
							false);
				}
			}

			// get the top and bottom values
			Double cashTop = null;
			Double cashBottom = null;

			for (Double val : totalBalanceSolution.values()) {
				if (cashTop == null) {
					cashTop = val;
				} else if (cashTop < val) {
					cashTop = val;
				}
				if (cashBottom == null) {
					cashBottom = val;
				} else if (cashBottom > val) {
					cashBottom = val;
				}
			}
			for (Double val : totalFinance.values()) {
				if (cashTop == null) {
					cashTop = val;
				} else if (cashTop < val) {
					cashTop = val;
				}
				if (cashBottom == null) {
					cashBottom = val;
				} else if (cashBottom > val) {
					cashBottom = val;
				}
			}
			for (Double val : totalOriginalBalanceSolution.values()) {
				if (cashTop == null) {
					cashTop = val;
				} else if (cashTop < val) {
					cashTop = val;
				}
				if (cashBottom == null) {
					cashBottom = val;
				} else if (cashBottom > val) {
					cashBottom = val;
				}
			}

			long numOfDayes = ProjectController.differenceInDays(portoflioDateRange[0], portoflioDateRange[1]);
			long width = (numOfDayes + 2) * DAY_WIDTH + 3 * SVG_PADDING; //

			// need to display a bit more in the top and bottom
			double range = Math.abs(cashTop - cashBottom);
			int numOfDijits = (int) Math.floor(Math.log10(range * 1.2));
			double cashStep = Math.pow(10, numOfDijits);
			int startCash = (int) ((int) Math.floor(cashBottom / cashStep) * cashStep);
			// number of horizontal lines we will draw
			int numberOfCashes = (int) (Math.floor(Math.abs(cashTop - startCash) / cashStep) + 2); 
			
			int cashHeight = (SVG_HEIGHT - 2 * SVG_PADDING) / numberOfCashes;

			StringBuilder sb = new StringBuilder();
			sb.append("<svg height='").append(SVG_HEIGHT).append("' width='").append(width).append("'>\r");
			// draw the horizontal lines
			for (int i = 0; i < numberOfCashes; i++) {
				int x = 3 * SVG_PADDING;
				int y = SVG_HEIGHT - SVG_PADDING - i * cashHeight;
				sb.append(String.format(FINANCE_HORIZONTAL_LINE, x,y,width - SVG_PADDING - x,y,startCash + cashStep * i));
			}

			Calendar end = Calendar.getInstance();
			end.setTime(portoflioDateRange[1]);

			{//add dates vertically, only do the even number not to clutter the UI
				SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);
				int index = 0;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					int x = FIRST_DAY + index * DAY_WIDTH;
					if ((index % 2) == 0) {
						sb.append(String.format(DATE_VERTICAL,x,(SVG_HEIGHT - 2 * SVG_PADDING),format.format(date)));
					}
					index++;
				}				
			}
			
			{ // Add Finance line

				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);

				int index = 0;
				int financeY = 0;
				StringBuilder financeSB = new StringBuilder();

				double ratio = cashHeight / cashStep;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					Double cF = totalFinance.get(date);

					int yf = (int) (SVG_HEIGHT - (cF - startCash) * ratio - SVG_PADDING);
					if (index == 0) {
						financeSB.append("M ").append(FIRST_DAY).append(" ").append(yf);
						financeY = yf;
					} else {
						if (financeY == yf) {
							financeSB.append(" l ").append(DAY_WIDTH).append(" 0");
						} else {
							financeSB.append(" l ").append(DAY_WIDTH).append(" 0 l 0 ").append(yf - financeY);
							financeY = yf;
						}
					}
					index++;
				}
				sb.append(String.format(FINANCE_PATH, financeSB.toString()));
			}
			
			double ratio = cashHeight / cashStep;
			if (solved) { // Original solution

					Calendar start = Calendar.getInstance();
					start.setTime(portoflioDateRange[0]);

					int index = 0;
					int currentY = 0;
					StringBuilder tempSB = new StringBuilder();

					double prevBalance = 0;
					for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
							1), date = start.getTime()) {
						Double cF = totalOriginalFinance.get(date);

						int yStart = (int) (SVG_HEIGHT - (prevBalance + totalOriginalPayments.get(date) - startCash) * ratio
								- SVG_PADDING);
						int yEnd = (int) (SVG_HEIGHT
								- (prevBalance + totalOriginalPayments.get(date) - totalOriginalCashOut.get(date) - startCash) * ratio
								- SVG_PADDING);
						if (index == 0) {
							tempSB.append("M ").append(FIRST_DAY).append(" ").append(yStart);
							tempSB.append(" l 0 ").append(yEnd - yStart);
							currentY = yEnd;
						} else {
							tempSB.append(" l ").append(DAY_WIDTH).append(" 0");
							tempSB.append(" l 0 ").append(yEnd - currentY);
							currentY = yEnd;
						}
						prevBalance = totalOriginalBalanceSolution.get(date);
						index++;
					}
					sb.append(String.format(ORIGINAL_PATH, tempSB.toString()));
				}
			
			{ // current solution

				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);

				int index = 0;
				int currentY = 0;
				StringBuilder tempSB = new StringBuilder();

				double prevBalance = 0;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					Double cF = totalFinance.get(date);

					int yStart = (int) (SVG_HEIGHT - (prevBalance + totalPayments.get(date) - startCash) * ratio
							- SVG_PADDING);
					int yEnd = (int) (SVG_HEIGHT
							- (prevBalance + totalPayments.get(date) - totalCashOut.get(date) - startCash) * ratio
							- SVG_PADDING);
					if (index == 0) {
						tempSB.append("M ").append(FIRST_DAY).append(" ").append(yStart);
						tempSB.append(" l 0 ").append(yEnd - yStart);
						currentY = yEnd;
					} else {
						tempSB.append(" l ").append(DAY_WIDTH).append(" 0");
						tempSB.append(" l 0 ").append(yEnd - currentY);
						currentY = yEnd;
					}
					prevBalance = totalBalanceSolution.get(date);
					index++;
				}
				sb.append(String.format(CURRENT_PATH, tempSB.toString()));
			}

			
/*			{

				Calendar start = Calendar.getInstance();
				start.setTime(portoflioDateRange[0]);

				int index = 0;
				int financeY = 0;
				int currentY = 0;
				int currentY2 = 0;
				StringBuilder financeSB = new StringBuilder();
				StringBuilder currentSB = new StringBuilder();
				StringBuilder originalSB = new StringBuilder();

				double prevBalance = 0;
				double prevBalance2 = 0;
				double ratio = cashHeight / cashStep;
				for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE,
						1), date = start.getTime()) {
					Double cF = totalFinance.get(date);

					int yf = (int) (SVG_HEIGHT - (cF - startCash) * ratio - SVG_PADDING);
					int ys = (int) (SVG_HEIGHT - (prevBalance + totalPayments.get(date) - startCash) * ratio
							- SVG_PADDING);
					int yE = (int) (SVG_HEIGHT
							- (prevBalance + totalPayments.get(date) - totalCashOut.get(date) - startCash) * ratio
							- SVG_PADDING);
					int ysO = solved ? (int) (SVG_HEIGHT
							- (prevBalance2 + totalOriginalPayments.get(date) - startCash) * ratio - SVG_PADDING) : 0;
					int yEO = solved ? (int) (SVG_HEIGHT - (prevBalance2 + totalOriginalPayments.get(date)
							- totalOriginalCashOut.get(date) - startCash) * ratio - SVG_PADDING) : 0;
					if (index == 0) {
						financeSB.append("<path d=\"M ").append(FIRST_DAY).append(" ").append(yf);
						financeY = yf;

						currentSB.append("<path d=\"M ").append(FIRST_DAY).append(" ").append(ys);
						currentSB.append(" l 0 ").append(yE - ys);
						currentY = yE;

						originalSB.append("<path d=\"M ").append(FIRST_DAY).append(" ").append(ys);
						originalSB.append(" l 0 ").append(yEO - ysO);
						currentY2 = yEO;
					} else {
						if (financeY == yf) {
							financeSB.append(" l ").append(DAY_WIDTH).append(" 0");
						} else {
							financeSB.append(" l ").append(DAY_WIDTH).append(" 0 l 0 ").append(yf - financeY);
							financeY = yf;
						}

						currentSB.append(" l ").append(DAY_WIDTH).append(" 0");
						currentSB.append(" l 0 ").append(yE - currentY);
						currentY = yE;

						originalSB.append(" l ").append(DAY_WIDTH).append(" 0");
						originalSB.append(" l 0 ").append(yEO - currentY2);
						currentY2 = yEO;

					}
					prevBalance = totalBalanceSolution.get(date);
					prevBalance2 = solved ? totalOriginalBalanceSolution.get(date) : 0;
					index++;
				}
				financeSB.append("\" stroke=\"orange\" stroke-width=\"2\" fill=\"none\" />\r");
				sb.append(financeSB.toString());

				currentSB.append("\" stroke=\"green\" stroke-width=\"2\" fill=\"none\" />\r");
				sb.append(currentSB.toString());

				if (solved) {
					originalSB.append("\" stroke=\"blue\" stroke-width=\"2\" fill=\"none\" />\r");
					sb.append(originalSB.toString());
				}
			}
*/			
			sb.append("</svg>");
			return new ServerResponse("0", "Success", sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003",
					String.format("Error looking up portfolio %d: %s", portfolioId, e.getMessage()), e);
		}
	}

	private void calculateProjectVars(Project currentProject, Date[] portoflioDateRange, EntityController<?> controller,
			int portfolioId, Map<Date, Double> totalCashOut, Map<Date, Double> totalFinanceCost, HttpSession session,
			Map<Date, Double> totalPayments, Map<Date, Double> totalBalanceSolution, Map<Date, Double> totalFinance,
			Map<Date, Double> totalBalanceInitial, Map<String, DailyCashFlowMapEntity> results,
			boolean currentOrOriginal) throws EntityControllerException {
		Calendar start = Calendar.getInstance();
		start.setTime(portoflioDateRange[0]);
		Calendar end = Calendar.getInstance();
		end.setTime(portoflioDateRange[1]);

		Date[] projectDates = currentOrOriginal
				? PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId())
				: PaymentUtilBeforeSolving.getProjectDateRanges(controller, currentProject.getProjectId());
		Date projectStartDate = projectDates[0];
		Date projectEndDate = projectDates[1];

		for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
			entity.setPortfolioId(portfolioId);
			entity.setProjectId(currentProject.getProjectId());
			entity.setDay(date);
			boolean includeOverhead = false;
			if ((date.equals(projectStartDate) || date.after(projectStartDate))
					&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
				includeOverhead = true;
			}

			double cashOutValue = currentOrOriginal
					? PaymentUtil.getDateTasksCashout(currentProject, date, includeOverhead)
					: PaymentUtilBeforeSolving.getDateTasksCashout(currentProject, date, includeOverhead);
			entity.setCashout(cashOutValue);
			if (totalCashOut.get(date) == null)
				totalCashOut.put(date, cashOutValue);
			else
				totalCashOut.put(date, totalCashOut.get(date) + cashOutValue);

			double financeCostValue = currentOrOriginal ? PaymentUtil.getDateFinanceCost(currentProject, date, results)
					: PaymentUtilBeforeSolving.getDateFinanceCost(currentProject, date, results);
			entity.setFinanceCost(financeCostValue);
			if (totalFinanceCost.get(date) == null)
				totalFinanceCost.put(date, financeCostValue);
			else
				totalFinanceCost.put(date, totalFinanceCost.get(date) + financeCostValue);

			double paymentsValue = currentOrOriginal ? PaymentUtil.getProjectPaymentstNew(session, currentProject, date)
					: PaymentUtilBeforeSolving.getProjectPaymentstNew(session, currentProject, date);
			entity.setPayments(paymentsValue);
			if (totalPayments.get(date) == null)
				totalPayments.put(date, paymentsValue);
			else
				totalPayments.put(date, totalPayments.get(date) + paymentsValue);

			double balanceSolutionValue = currentOrOriginal ? PaymentUtil.getBalance(date, entity, results)
					: PaymentUtilBeforeSolving.getBalance(date, entity, results);
			entity.setBalance(balanceSolutionValue);
			if (totalBalanceSolution.get(date) == null)
				totalBalanceSolution.put(date, balanceSolutionValue);
			else
				totalBalanceSolution.put(date, totalBalanceSolution.get(date) + balanceSolutionValue);

			double finance = currentOrOriginal ? PaymentUtil.getFinanceLimit(session, portfolioId, date)
					: PaymentUtil.getFinanceLimit(session, portfolioId, date);
			totalFinance.put(date, -1 * finance);

			if (totalBalanceInitial.get(date) == null)
				totalBalanceInitial.put(date, entity.getBalance() + entity.getFinanceCost());
			else
				totalBalanceInitial.put(date,
						totalBalanceInitial.get(date) + entity.getBalance() + entity.getFinanceCost());

			if (currentOrOriginal) {
				results.put(PaymentUtil.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(), entity);
			} else {
				results.put(PaymentUtilBeforeSolving.dateOnlyFormat.format(date) + "," + currentProject.getProjectId(),
						entity);
			}
		}
	}

	private static byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			try {
				is.close();
			} catch (Exception e) {
			}
			throw new IOException("Could not completely read file " + file.getName());
		}

		is.close();
		return bytes;
	}

	public ServerResponse getPortfolioLeftOverCost(HttpSession session, int portfolioId, Date from, Date to) {

		EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
		try {
			Portfolio protfolio = portController.find(Portfolio.class, portfolioId);
			double totalPortfolioLeftOverCost = PaymentUtil.getPortfolioLeftOverCost(portController, protfolio, from,
					to, null);
			return new ServerResponse("0", "Success", totalPortfolioLeftOverCost);

		} catch (Exception e) {
			e.printStackTrace();
			return new ServerResponse("PROJ0003",
					String.format("Error looking up portfolio %d: %s", portfolioId, e.getMessage()), e);
		}
	}

	public synchronized ServerResponse getExtraCachCurrentPeriod(HttpSession session, int portfolioId, Date from,
			Date to) {
		EntityController<ProjectPayment> paymentController = new EntityController<>(session.getServletContext());
		try {
			@SuppressWarnings("unchecked")
			List<Double> total = (List<Double>) paymentController.nativeQuery(
					"Select coalesce(sum(coalesce(payment_amount , 0 )) , 0 ) from project_payment where project_id in ( select project_id from project where portfolio_id = ? ) and payment_date >= ? and payment_date < ?",
					portfolioId, from, to);
			return new ServerResponse("0", "Succes", total.get(0));

		} catch (EntityControllerException e) {

			e.printStackTrace();
			return new ServerResponse("PORT0011",
					String.format("Error getting extra cash for protfolio %d : %s", portfolioId, e.getMessage()), e);
		}

	}

	public synchronized ServerResponse getExtraCachNextPeriod(HttpSession session, int portfolioId, Date from, Date to,
			String paymentDetailsJson) {
		Map<Integer, ProjectPaymentDetail> paymentDetails = PaymentUtil.getPaymentDetailsMap(paymentDetailsJson);

		EntityController<Portfolio> porController = new EntityController<>(session.getServletContext());
		try {
			Portfolio portfolio = porController.find(Portfolio.class, portfolioId);
			if (portfolio == null) {
				return new ServerResponse("0", "Succes", 0);

			} else {
				double expectedPayment = PaymentUtil.getPortfolioPayment(from, to, paymentDetails, portfolio, false);
				return new ServerResponse("0", "Succes", expectedPayment);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0011",
					String.format("Error getting extra cash for protfolio %d : %s", portfolioId, e.getMessage()), e);
		}

	}

	public synchronized ServerResponse getExtraCachNextPeriodByProject(HttpSession session, int projectId, Date from,
			Date to, double advanceRepayment, double retainPercent, double extraPayment, Date paymentFrom,
			Date paymentTo) {
		EntityController<Project> projController = new EntityController<>(session.getServletContext());
		try {
			Project project = projController.find(Project.class, projectId);
			if (project == null) {
				return new ServerResponse("0", "Succes", 0);

			} else {
				double expectedPayment = PaymentUtil.getProjectPayment(from, to, advanceRepayment, retainPercent,
						extraPayment, paymentFrom, paymentTo, project);
				return new ServerResponse("0", "Succes", expectedPayment);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0014",
					String.format("Error getting extra cash for project %d : %s", projectId, e.getMessage()), e);
		}

	}

	public synchronized ServerResponse getExtraCachNextPeriodByProjectNoAdjustments(HttpSession session, int projectId,
			Date from, Date to, Date paymentFrom, Date paymentTo) {
		EntityController<Project> projController = new EntityController<>(session.getServletContext());
		try {
			Project project = projController.find(Project.class, projectId);
			if (project == null) {
				return new ServerResponse("0", "Succes", 0);

			} else {
				double expectedPayment = PaymentUtil.getProjectPaymentNoAdjustments(from, to, paymentFrom, paymentTo,
						project);
				return new ServerResponse("0", "Succes", expectedPayment);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0014",
					String.format("Error getting extra cash for project %d : %s", projectId, e.getMessage()), e);
		}

	}

	public synchronized ServerResponse getProjectsByPaymentDate(HttpSession session, int portfolioId, Date from) {
		EntityController<Portfolio> porController = new EntityController<>(session.getServletContext());
		try {
			Portfolio portfolio = porController.find(Portfolio.class, portfolioId);
			if (portfolio == null) {
				return new ServerResponse("0", "Succes", null);

			} else {
				List<Project> projects = new ArrayList<Project>();
				for (Project project : portfolio.getProjects()) {
					for (ProjectPayment payment : project.getProjectPayments()) {
						if (payment.getPaymentDate().equals(from)) {
							projects.add(project);
						}

					}
				}
				return new ServerResponse("0", "Success", projects);
			}

		} catch (EntityControllerException e) {
			e.printStackTrace();
			return new ServerResponse("PORT0012", String.format(
					"Error getting projects by payment date for protfolio %d : %s", portfolioId, e.getMessage()), e);
		}

	}

}
