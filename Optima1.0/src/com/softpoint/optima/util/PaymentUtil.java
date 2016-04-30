package com.softpoint.optima.util;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.softpoint.optima.OptimaException;
import com.softpoint.optima.control.EntityController;
import com.softpoint.optima.control.EntityControllerException;
import com.softpoint.optima.control.PortfolioController;
import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.PaymentType;
import com.softpoint.optima.db.Portfolio;
import com.softpoint.optima.db.PortfolioExtrapayment;
import com.softpoint.optima.db.PortfolioFinance;
import com.softpoint.optima.db.Project;
import com.softpoint.optima.db.ProjectPayment;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.TaskDependency;
import com.softpoint.optima.db.WeekendDay;
import com.softpoint.optima.struct.DailyCashFlowMapEntity;
import com.softpoint.optima.struct.Period;
import com.softpoint.optima.struct.ProjectPaymentDetail;
import com.softpoint.optima.struct.TaskSolution;

public class PaymentUtil {
	public static SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	public synchronized static String getDatesOnly(Date date){
		return dateOnlyFormat.format(date);
	}
	
	public static boolean isDateInBetweenDates(Date start, Date end, Date value) {
		return start.getTime() <= value.getTime()
				&& value.getTime() <= end.getTime();
	}

	/**
	 * @param date
	 * @param daysOff
	 * @return
	 */
	public static boolean isDayOff(Date date, List<DaysOff> daysOff) {
		if (daysOff != null) {
			for (DaysOff dayoff : daysOff) {
				if (dayoff.getDayOff().equals(date)) {
					return true;
				}
			}
		} 
		return false;
	}
	
	/**
	 * @param date
	 * @param weekendDays
	 * @return
	 */
	public static boolean isWeekendDay(Date date, WeekendDay weekend) {
		if (weekend == null) return false;
		int weekendDays = weekend.getWeekendDaysId(); 
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (weekendDays == 1 && (dayOfTheWeek == Calendar.SATURDAY || dayOfTheWeek == Calendar.SUNDAY) || weekendDays == 2
				&& (dayOfTheWeek == Calendar.FRIDAY || dayOfTheWeek == Calendar.SATURDAY) || weekendDays == 3
				&& (dayOfTheWeek == Calendar.THURSDAY || dayOfTheWeek == Calendar.FRIDAY)) {
			return true;
		} else {
			return false;
		}
	}

	
	public static double getPortfolioLeftOverCost(EntityController<Portfolio> controller , Portfolio portfolio , Date from , Date to, HashMap<Integer, Integer> completedProjects) {
		List<Project> projects = portfolio.getProjects();
		double portfolioLeftOvers = 0;
		for (Project project : projects) {
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(PaymentUtil.getProjectExpectedEndDate(project));
				cal.add(Calendar.DATE, 1);
				Date projectEndDate = cal.getTime();
				
				Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
				Date projectStartDate = projectDates[0];
				
				if (projectStartDate != null && projectStartDate.getTime() < to.getTime() && projectEndDate.getTime() >= from.getTime()) {
					if(completedProjects!=null && completedProjects.get(project.getProjectId())!=null)
						portfolioLeftOvers += getLeftOverCost( project, from, to,projectStartDate, projectEndDate,  true);
					else
						portfolioLeftOvers += getLeftOverCost( project, from, to,projectStartDate, projectEndDate,  false);
				}
			} catch (EntityControllerException e) {
				e.printStackTrace();
			}
		}
		return portfolioLeftOvers;
	}
	
	public static double getLeftOverCost(Project project , Date from , Date to , Date projectStartDate , Date projectEndDate, boolean projectCompleted) {
		List<ProjectTask> tasks = project.getProjectTasks();
		int numberOfDays = 0;
		
		numberOfDays = PaymentUtil.daysBetween(PaymentUtil.maxDate(from, projectStartDate) , PaymentUtil.minDate(to, projectEndDate));
		double taskCostCounter = 0;
		
		int diffInDays = 0;
		long maxEndTime = 0;
		for (ProjectTask currentTask : tasks) {
			
			Date taskDate = PaymentUtil.getTaskDate(currentTask);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(taskDate);
			calendar.add(Calendar.DATE, currentTask.getCalenderDuration());
			Date taskEndDate = calendar.getTime();
			
			if (taskDate.before(from) && taskEndDate.after(from)){
				
				int taskDaysAfterPeriodStart = PaymentUtil.daysBetween(PaymentUtil.maxDate(from, projectStartDate), taskEndDate);
				int effictiveNumberOfDays = taskDaysAfterPeriodStart;
				if(numberOfDays < taskDaysAfterPeriodStart) {
					effictiveNumberOfDays = numberOfDays;
				}
				
				Calendar endEffectiveDate = Calendar.getInstance();
				endEffectiveDate.setTime(from);
				endEffectiveDate.add(Calendar.DATE, effictiveNumberOfDays);
				// task cost
				
				if(endEffectiveDate.getTime().getTime() > maxEndTime)
					maxEndTime = endEffectiveDate.getTime().getTime();
					
				int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, from, endEffectiveDate.getTime()); 
				
				effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
				double taskCost = currentTask.getUniformDailyCost().doubleValue() * effictiveNumberOfDays;
				taskCostCounter += taskCost;
				
				
			}
			
		}
		if(projectCompleted){
			if(from.getTime() < maxEndTime){
				diffInDays = (int) (maxEndTime - from.getTime()) / (1000 * 60 * 60 * 24);
				numberOfDays = diffInDays;
			}
		}
			
		double overhead =  project.getOverheadPerDay().doubleValue() * numberOfDays;
		//System.out.println(project.getProjectId() + "-" + numberOfDays);
		double cashout = taskCostCounter +  overhead;
		return cashout;
		
	}
	
	
	public static Date[] getPortofolioDateRangesNew( EntityController<?> controller, int portfolioId) throws EntityControllerException {
		String query = "select min(calendar_start_date) , max(ADDDATE(calendar_start_date, calender_duration - 1))   from project_task where project_id in (select project_id from project where portfolio_id = ?1)";
		List<?> results = controller.nativeQuery(query, portfolioId);
		Date[] dates = new Date[2];
		
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				dates[0] = (Date) values[0];
			if (values[1] != null) {
				String endDate = (String) values[1];
				try {
					dates[1] = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
				
			}
			
			query = "select min(propused_start_date) from project where project_id in (select project_id from project where portfolio_id = ?1)";
			results = controller.nativeQuery(query, portfolioId);
			dates[0] = (Date) results.get(0);

		}
		return dates;
	}
		
		
	public static Date[] getPortofolioDateRanges( EntityController<?> controller, int portfolioId) throws EntityControllerException {
		String query = "select min(calendar_start_date) , max(ADDDATE(calendar_start_date, calender_duration - 1))   from project_task where project_id in (select project_id from project where portfolio_id = ?1)";
		List<?> results = controller.nativeQuery(query, portfolioId);
		Date[] dates = new Date[2];
		
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				dates[0] = (Date) values[0];
			if (values[1] != null) {
				String endDate = (String) values[1];
				try {
					dates[1] = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
				
			}

		}
		
		
		query = "select min(payment_date) , max(payment_date)   from project_payment where project_id in (select project_id from project where portfolio_id = ?1)";
		results = controller.nativeQuery(query, portfolioId);
		Date[] paymentDates = new Date[2];
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				paymentDates[0] = (Date) values[0];
			if (values[1] != null) {
				paymentDates[1] = (Date) values[1];
			}
		}
		
		if (null != paymentDates[0] && dates[0].after(paymentDates[0])) {
			dates[0] = paymentDates[0];
		}
		if (null != paymentDates[1] && dates[1].before(paymentDates[1])) {
			dates[1] = paymentDates[1];
		}
		query = "select min(propused_start_date) , max(proposed_finish_date) from project where portfolio_id = ?";
		results = controller.nativeQuery(query, portfolioId);
		Date[] projectDates = new Date[2];
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				projectDates[0] = (Date) values[0];
			if (values[1] != null) {
				projectDates[1] = (Date) values[1];
			}
		}
		
		
		if (null != projectDates[0] && dates[0].after(projectDates[0])) {
			dates[0] = projectDates[0];
		}
		if (null != projectDates[1] && dates[1].before(projectDates[1])) {
			dates[1] = projectDates[1];
		}
		return dates;
	}
	

	
	public static Date[] getProjectExtendedDateRanges( EntityController<?> controller, Project project) throws EntityControllerException {
		int projectId = project.getProjectId();
		String query = "select min(calendar_start_date) , max(ADDDATE(calendar_start_date, calender_duration - 1))   from project_task where project_id  = ?1";
		List<?> results = controller.nativeQuery(query, projectId);
		Date[] dates = new Date[2];
		
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				dates[0] = (Date) values[0];
			if (values[1] != null) {
				String endDate = (String) values[1];
				try {
					dates[1] = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
				
			}
		}
		
		if (dates[1]!=null && project.getCollectPaymentPeriod()>0 && project.getPaymentRequestPeriod()>0) {
			int nod = daysBetween(dates[0],dates[1]) + 1;
			double periods = Math.ceil(((double)nod)/project.getPaymentRequestPeriod());
			int days = (int) (periods * project.getPaymentRequestPeriod() + project.getCollectPaymentPeriod());
			Date d2 = PortfolioController.addDayes(dates[0], days);
			dates[1] = d2;
		}
		
/*		
		query = "select min(payment_date) , max(payment_date)   from project_payment where project_id = ?1";
		results = controller.nativeQuery(query, projectId);
		Date[] paymentDates = new Date[2];
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				paymentDates[0] = (Date) values[0];
			if (values[1] != null) {
				paymentDates[1] = (Date) values[1];
			}
		}
			
		if (null != paymentDates[0] && dates[0].after(paymentDates[0])) {
			dates[0] = paymentDates[0];
		}
		if (null != paymentDates[1] && dates[1].before(paymentDates[1])) {
			dates[1] = paymentDates[1];
		}
		
		query = "select min(propused_start_date) , max(proposed_finish_date) from project where project_id = ?";
		results = controller.nativeQuery(query, projectId);
		Date[] projectDates = new Date[2];
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				projectDates[0] = (Date) values[0];
			if (values[1] != null) {
				projectDates[1] = (Date) values[1];
			}
		}
		
		
		if (null != projectDates[0] && dates[0].after(projectDates[0])) {
			dates[0] = projectDates[0];
		}
		if (null != projectDates[1] && dates[1].before(projectDates[1])) {
			dates[1] = projectDates[1];
		}*/
		return dates;
	}
	
	
	public static Date[] getProjectDateRanges( EntityController<?> controller, int projectId) throws EntityControllerException {
		String query = "select min(calendar_start_date) , max(ADDDATE(calendar_start_date, calender_duration - 1))   from project_task where project_id  = ?1";
		
		List<?> results = controller.nativeQuery(query, projectId);
		Date[] dates = new Date[2];
		if (results != null && results.size() > 0) {
			Object[] values = (Object[]) results.get(0);
			if (values[0] != null)
				dates[0] = (Date) values[0];
			if (values[1] != null) {
				String endDate = (String) values[1];
				try {
					dates[1] = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(endDate);
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
				
			}
			
			query = "select propused_start_date from project where project_id  = ?1";
			results = controller.nativeQuery(query, projectId);
			if (results.get(0)!=null) {
				dates[0] = (Date) results.get(0);
			}

		}
//		Calendar calendar = Calendar.getInstance();
//		calendar.setTime(dates[1]);
//		calendar.add(Calendar.DATE, 1);
//		dates[1] = calendar.getTime();
		return dates;
	}
	
	public static double getDateTasksCashout(Project project, Date currentDate , boolean includeOverHeads ) {
		double cashoutTotal = 0d;
		List<ProjectTask> tasks = project.getProjectTasks();
		Iterator<ProjectTask> iter = tasks.iterator();
		ProjectTask currentTask;
		Date taskDate = null;
		while (iter.hasNext()) {
			currentTask = iter.next();
			if(currentTask.getActualStartDate() != null){
				taskDate = currentTask.getActualStartDate();
			} else if (currentTask.getScheduledStartDate() != null) {
				taskDate = currentTask.getScheduledStartDate();
			} else if (currentTask.getCalendarStartDate() != null) {
				taskDate = currentTask.getCalendarStartDate();
			}
			
			if (taskDate != null && isDateInBetweenDates(taskDate, DateUtils.add(taskDate, Calendar.DATE, currentTask.getCalenderDuration() - 1), currentDate)
					&&  !isWeekendDay(currentDate, project.getWeekendDays())
					&& !isDayOff(currentDate, project.getDaysOffs()) ) {
				cashoutTotal = cashoutTotal + currentTask.getUniformDailyCost().doubleValue();

			}
			taskDate = null;
		}
		if (includeOverHeads) {
			cashoutTotal += project.getOverheadPerDay().doubleValue();
		}
		return cashoutTotal;
	}
	
	
	/**
	 * @param currentDate
	 * @param results
	 * @param projectId
	 * @return
	 */
	public static DailyCashFlowMapEntity getPreviousDayInfo(Date currentDate , Map<String, DailyCashFlowMapEntity> results, int projectId){
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
	    c.add(Calendar.DATE, -1);
	    Date previousDate = c.getTime();
	    String previousDateStr = getDatesOnly(previousDate);
	    
		DailyCashFlowMapEntity cashFlowMapEntity = results.get(previousDateStr + ","+ projectId);
	
		return cashFlowMapEntity;
	
	}
	
	/**
	 * @param project
	 * @param currentDate
	 * @param results
	 * @return
	 */
	public static double getDateFinanceCost(Project project, Date currentDate , Map<String, DailyCashFlowMapEntity> results ){
		DailyCashFlowMapEntity previousDay = getPreviousDayInfo(currentDate, results, project.getProjectId());
		if(previousDay == null || previousDay.getBalance() >= 0 ){
			return 0d;
		}else {
			double financeCost = previousDay.getBalance() * getInterestInDay(project,currentDate);
			return financeCost;
		}
	}
	
	public static Double getInterestInDay(Project project, Date date) {
		List<PortfolioFinance> finances = null;
		if (project.getPortfolio()==null) {
			finances = project.getPortfolioFinances();
		}else {
			finances = project.getPortfolio().getPortfolioFinances();
		}
		
		Double interest = (double) 0;
		
		if (finances!=null) {
			for (PortfolioFinance fin : finances) {
				if (!fin.getFinanceUntillDate().before(date)) {
					interest = fin.getInterestRate().doubleValue();
					break;
				}
			}
		}
		return interest;
	}
	//3.	Balance = yesterday netbalance – today cashout – finance cost of today. 
	public static double getBalance(Date currentDate, DailyCashFlowMapEntity currentDayInfo, Map<String, DailyCashFlowMapEntity> results ){
		DailyCashFlowMapEntity previousDay = getPreviousDayInfo(currentDate, results, currentDayInfo.getProjectId());
		double balance = 0d;
		if(previousDay == null) {
			balance = currentDayInfo.getPayments() - currentDayInfo.getCashout();
		} else {
			balance = previousDay.getBalance() + currentDayInfo.getPayments() -   currentDayInfo.getCashout() ;
		}

		
		return balance;
	}
	
	//4.	Payments : sum (project payments) for this day. 
	
	public static double getPortfolioPayments(Portfolio portfolio , Date currentDate) {
		double payments = 0;
		List<Project> projects = portfolio.getProjects();
		for (Project project : projects) {
			payments += getProjectPaymentst(project, currentDate);
		}
		return payments;
	}
	
	
	
	public static double getProjectPaymentstNew(HttpSession session, Project project, Date currentDate) throws EntityControllerException {
		EntityController<ProjectTask> controller = new EntityController<>(session.getServletContext());
		double projectPayments = 0d;
		Date[] projectBoundaries = PaymentUtil.getPortofolioDateRangesNew(controller, project.getPortfolio().getPortfolioId());
		
		List<ProjectPayment> payments = getProjectPayments(session, project,  projectBoundaries[1]);
		
		Iterator<ProjectPayment> iter = payments.iterator();
		ProjectPayment currentPayment;
		while (iter.hasNext()) {
			currentPayment = iter.next();
			if(DateUtils.isSameDay(currentDate, currentPayment.getPaymentDate())){
				projectPayments = projectPayments + currentPayment.getPaymentAmount().doubleValue();
				
			}
		}
		return projectPayments;
	}
	
	public static double getProjectPaymentst(Project project, Date currentDate) {
		double projectPayments = 0d;
		List<ProjectPayment> payments = project.getProjectPayments();
		Iterator<ProjectPayment> iter = payments.iterator();
		ProjectPayment currentPayment;
		while (iter.hasNext()) {
			currentPayment = iter.next();
			if(DateUtils.isSameDay(currentDate, currentPayment.getPaymentDate())){
				projectPayments = projectPayments + currentPayment.getPaymentAmount().doubleValue();
			}
		}
		return projectPayments;
	}







	public static Period getPortfolioBoundaryDate(HttpSession session,
			int portfolioId) {
		
		
		EntityController<ProjectTask> taskController = new EntityController<>(session.getServletContext());
		try {
			List<ProjectTask> tasks = taskController.nativeQuery(ProjectTask.class , "Select * from project_task where project_id in (select project_id from project where portfolio_id = ?)",portfolioId);
			Date portfolioStartDate = null;
			Date portfolioEndDate = null;
					
			for (ProjectTask task : tasks) {
				Date taskDate = getTaskDate(task);
				if (portfolioEndDate == null) {
					portfolioStartDate = taskDate;
					portfolioEndDate = taskDate;
				} else {
					
					if (portfolioStartDate.after(taskDate)) {
						portfolioStartDate = taskDate; 
					} 
					
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(taskDate);
					calendar.add(Calendar.DATE, task.getCalenderDuration());
					if (portfolioEndDate.before(calendar.getTime())) {
						portfolioEndDate = calendar.getTime();
					}
				
				}
				
			}
			return new Period(portfolioStartDate , portfolioEndDate);
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return null;
		}
		
	}







	public static Date getTaskDate(ProjectTask task) {
		if (task.getActualStartDate() != null) {
			return task.getActualStartDate();
		} else if (task.getScheduledStartDate() != null) {
			return task.getScheduledStartDate();
		} else if (task.getCalendarStartDate() != null) {
			return task.getCalendarStartDate();
		} else return task.getTentativeStartDate();
		
	}



	public static Period findPaymentScheduleNew (HttpSession session , Date date, int portfolioId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRangesNew(controller, portfolioId);
			Period boundaries = new Period(projectBoundaries[0] , projectBoundaries[1]); 
			EntityController<Portfolio> portController = new EntityController<Portfolio>(session.getServletContext());
			Portfolio portfolio = portController.find(Portfolio.class, portfolioId);
			List<Project> projects = portfolio.getProjects();
			
			Comparator<Date> dateComparator = new DateComparator();
	        PriorityQueue<Date> paymentsQueue = new PriorityQueue<Date>(dateComparator);

			for (Project currentProject : projects) {
				List<ProjectPayment> projectPayments = getProjectPayments(session, currentProject, projectBoundaries[1]);
				
				for (ProjectPayment projectPayment : projectPayments) {
					paymentsQueue.add(projectPayment.getPaymentDate());
				}
			}
			
			List<PortfolioExtrapayment> extraPaymentsList = portfolio.getPortfolioExtrapayments();
			for (PortfolioExtrapayment extraPayment : extraPaymentsList) {
				paymentsQueue.add(extraPayment.getExtraPayment_date());
			}
			
	        if(paymentsQueue != null)
			{
	        	while (paymentsQueue.size() != 0)
		        {
	        		Date paymentDate = paymentsQueue.remove();
	        		if ( paymentDate.after(date) ) {
						boundaries.setDateTo(paymentDate);	
						return boundaries;
					} else {
						boundaries.setDateFrom(paymentDate);
					}
		        }
				
			}
			return boundaries;
		} catch (EntityControllerException e) {
			return null;
			
		}
	}
	
	public static Period findPaymentSchedule(HttpSession session , Date date, int portfolioId) throws OptimaException {
		EntityController<ProjectPayment> controller = new EntityController<ProjectPayment>(session.getServletContext());
		try {
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRanges(controller, portfolioId);
			Period boundaries = new Period(projectBoundaries[0] , projectBoundaries[1]); 
			List <ProjectPayment> projectPayments = controller.nativeQuery(ProjectPayment.class , "Select * from project_payment d where project_id in  (Select project_id from Project where portfolio_id = ? )  order by payment_date asc" , portfolioId);
			if(projectPayments != null)
			{
				for (ProjectPayment payment : projectPayments) {
					
					if ( payment.getPaymentDate().after(date) ) {
						boundaries.setDateTo(payment.getPaymentDate());	
						return boundaries;
					} else {
						boundaries.setDateFrom(payment.getPaymentDate());
					}
				}
				
			}
			return boundaries;
		} catch (EntityControllerException e) {
			return null;
			
		}
	}



public static Period findFinanceSchedule(HttpSession session , Date date, int portfolioId) throws OptimaException {
		
		
		EntityController<PortfolioFinance> controller = new EntityController<PortfolioFinance>(session.getServletContext());
		try {
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRanges(controller, portfolioId);
			Period boundaries = new Period(projectBoundaries[0] , projectBoundaries[1]); 
		
			//Get the date of finance > given date
			List <PortfolioFinance> portfolioFinances = controller.nativeQuery(PortfolioFinance.class , "Select * from portfolio_finance d where portfolio_id =  ?  order by  finance_untill_date asc" , portfolioId);
			
			if(portfolioFinances != null)
			{
				for (PortfolioFinance finance : portfolioFinances) {
					if (finance.getFinanceUntillDate().after(date)) {	
						 boundaries.setDateTo(finance.getFinanceUntillDate());
						 return boundaries;
					} else {
						boundaries.setDateFrom(finance.getFinanceUntillDate());
					}
				}
				
			} 
			return boundaries;
			
			
		} catch (EntityControllerException e) {
			return null;
			
		}
	}

	
	public static int daysBetween(Date start, Date end) {
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(start);
		
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(end);
		
		
		Calendar date = (Calendar) startDate.clone();
		int daysBetween = 0;
		while (date.before(endDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		return daysBetween;
	}

	public static 	Map<String, DailyCashFlowMapEntity> getProjectCashFlowDataNew(HttpSession session, int projectId, Date from, Date to) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			
			Project project = controller.find(Project.class , projectId);

			
			Calendar start = Calendar.getInstance();
			start.setTime(from);
			Calendar end = Calendar.getInstance();
			end.setTime(to);
			Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
			Date projectStartDate = projectDates[0];
			Date projectEndDate = projectDates[1];
			
			for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
				DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
				entity.setPortfolioId(project.getPortfolio().getPortfolioId());
				entity.setProjectId(project.getProjectId());
				entity.setDay(date);
				boolean includeOverhead = false;
				if (   (date.equals(projectStartDate) || date.after(projectStartDate)) 
					&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date , includeOverhead));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				entity.setPayments(PaymentUtil.getProjectPaymentstNew(session,project, date));
				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());
				
				
				results.put( PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			
			
			return results;
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	
	
	public static 	Map<String, DailyCashFlowMapEntity> getProjectCashFlowData(HttpSession session, int projectId, Date from, Date to) throws OptimaException {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		try {
			
			Map<String, DailyCashFlowMapEntity> results = new HashMap<String, DailyCashFlowMapEntity>();
			
			Project project = controller.find(Project.class , projectId);

			
			Calendar start = Calendar.getInstance();
			start.setTime(from);
			Calendar end = Calendar.getInstance();
			end.setTime(to);
			Date[] projectDates = PaymentUtil.getProjectDateRanges(controller, project.getProjectId());
			Date projectStartDate = projectDates[0];
			Date projectEndDate = projectDates[1];
			
			for (Date date = start.getTime(); !start.after(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
				DailyCashFlowMapEntity entity = new DailyCashFlowMapEntity();
				entity.setPortfolioId(project.getPortfolio().getPortfolioId());
				entity.setProjectId(project.getProjectId());
				entity.setDay(date);
				boolean includeOverhead = false;
				if (   (date.equals(projectStartDate) || date.after(projectStartDate)) 
					&& (date.before(projectEndDate) || date.equals(projectEndDate))) {
					includeOverhead = true;
				}
				entity.setCashout(PaymentUtil.getDateTasksCashout(project, date , includeOverhead));
				entity.setFinanceCost(PaymentUtil.getDateFinanceCost(project, date, results));
				entity.setPayments(PaymentUtil.getProjectPaymentst(project, date));
				entity.setBalance(PaymentUtil.getBalance(date, entity, results));
				entity.setNetBalance(entity.getBalance() + entity.getFinanceCost());
				
				
				results.put( PaymentUtil.dateOnlyFormat.format(date) + "," + project.getProjectId(), entity);
			}

			
			
			return results;
		} catch (EntityControllerException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * @param session
	 * @param portfolioId
	 * @param toDate
	 * @return
	 * @throws EntityControllerException
	 */
	public static double getFinanceLimit(HttpSession session, int portfolioId,
			Date toDate) throws EntityControllerException {
		EntityController<PortfolioFinance> financeController = new EntityController<PortfolioFinance>(session.getServletContext());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String theDate = format.format(toDate);
		
		@SuppressWarnings("unchecked")
		List<BigDecimal> financeLimit = (List<BigDecimal>) financeController.nativeQuery("Select  finance_amount from portfolio_finance where portfolio_id = ? and finance_untill_date > ? order by finance_untill_date asc" , portfolioId , theDate);
		if (financeLimit == null || financeLimit.isEmpty()) return 0d;
		return financeLimit.get(0).doubleValue();
	}
	
	public static double getExtraPayment(HttpSession session, int portfolioId, Date fromDate) throws EntityControllerException {
		EntityController<PortfolioFinance> financeController = new EntityController<PortfolioFinance>(session.getServletContext());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String theDate = format.format(fromDate);
		
		@SuppressWarnings("unchecked")
		List<BigDecimal> extraPayment = (List<BigDecimal>) financeController.nativeQuery("Select  extraPayment_amount from portfolio_extrapayment where portfolio_id = ? and extraPayment_date = ? order by extraPayment_date asc" , portfolioId , theDate);
		if (extraPayment == null || extraPayment.isEmpty()) return 0d;
		return extraPayment.get(0).doubleValue();
	}

	public static List<ProjectTask> getEligibleTasks(Project project, Date from, Date to , boolean useCalDate) {
		List<ProjectTask> projectTasks = project.getProjectTasks();
		List<ProjectTask> eligibleTasks = new ArrayList<ProjectTask>();
		Calendar calendar = Calendar.getInstance();
		for (ProjectTask currentTask : projectTasks) {
			Date taskDate = PaymentUtil.getTaskDate(currentTask);
			if (useCalDate && currentTask.getCalendarStartDate() != null) {
				taskDate = currentTask.getCalendarStartDate();
			}
			calendar.setTime(taskDate);
			
			if ((taskDate.after(from) || taskDate.equals(from) ) && taskDate.before(to)) {
				eligibleTasks.add(currentTask);
			}	
			
		}
		return eligibleTasks;
	}
	
	
	public static List<ProjectTask> getCurrentTasks(Project project, Date from, Date to , boolean useCalDate) {
		List<ProjectTask> projectTasks = project.getProjectTasks();
		List<ProjectTask> currentTasks = new ArrayList<ProjectTask>();
		Calendar calendar = Calendar.getInstance();
		for (ProjectTask currentTask : projectTasks) {
			Date taskDate = PaymentUtil.getTaskDate(currentTask);
			if (useCalDate && currentTask.getCalendarStartDate() != null) {
				taskDate = currentTask.getCalendarStartDate();
			}
			calendar.setTime(taskDate);
			calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
			Date taskEndDate = calendar.getTime();
			// From or To?
			if(taskDate.before(to) && ( taskEndDate.after(from) || taskEndDate.equals(from) )){
				currentTasks.add(currentTask);
			}	
			
		}
		return currentTasks;
	}

	public static double getPortfolioOpenBalanceNew(HttpSession session, Portfolio portfolio, Date from) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		List<Project> projects = portfolio.getProjects();
		double balanceAccumulator = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(from);
		calendar.add(Calendar.DATE, -1);
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;
		for (Project project : projects) {
			try {
				Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, project.getProjectId())[0];
				if (projectStartDate != null) { // has some tasks or payments.
					cashFlowInfo = PaymentUtil.getProjectCashFlowDataNew(session, project.getProjectId(), projectStartDate , calendar.getTime());
					DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from , cashFlowInfo, project.getProjectId());
					balanceAccumulator += previousDay == null? 0 : previousDay.getBalance() ;
				}
			} catch (EntityControllerException | OptimaException e) {
				
				e.printStackTrace();
			}
		}
		return balanceAccumulator;
	}
	
	public static double getPortfolioOpenBalance(HttpSession session, Portfolio portfolio, Date from) {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		List<Project> projects = portfolio.getProjects();
		double balanceAccumulator = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(from);
		calendar.add(Calendar.DATE, -1);
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;
		for (Project project : projects) {
			try {
				Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, project.getProjectId())[0];
				if (projectStartDate != null) { // has some tasks or payments.
					cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, project.getProjectId(), projectStartDate , calendar.getTime());
					DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from , cashFlowInfo, project.getProjectId());
					balanceAccumulator += previousDay == null? 0 : previousDay.getBalance() ;
				}
			} catch (EntityControllerException | OptimaException e) {
				
				e.printStackTrace();
			}
		}
		return balanceAccumulator;
	}


	//Finance cost for left overs 
	public static double getPortfolioFinanceCostNew(HttpSession session, Portfolio portfolio, Date from, Date to) throws EntityControllerException, OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;
		Date taskEndDate ; 
		Date taskDate ;
		Calendar calendar = Calendar.getInstance();
		List<ProjectTask> tasks;
		double balanceCounter = 0;
		double financeCostCounter = 0;
		
		List<Project> projects = portfolio.getProjects();
		for (Project currentProject : projects) {
			Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId())[0];
			if (projectStartDate != null) {
				cashFlowInfo = PaymentUtil.getProjectCashFlowDataNew(session, currentProject.getProjectId(), projectStartDate , calendar.getTime());
	
				Calendar prevDay = Calendar.getInstance();
				prevDay.setTime(from);
				prevDay.add(Calendar.DATE, -1);
				
				DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from , cashFlowInfo, currentProject.getProjectId());
				
				double firstDateFinanceCost = PaymentUtil.getDateFinanceCost(currentProject, from, cashFlowInfo); 
				financeCostCounter += firstDateFinanceCost;
				double firstDayBalance = previousDay == null? 0 : previousDay.getBalance() ;
				double projectPayment = PaymentUtil.getProjectPaymentstNew(session, currentProject, from);
				firstDayBalance += projectPayment;
				balanceCounter = firstDayBalance;
	
				Calendar start = Calendar.getInstance();
				start.setTime(from);
				Calendar end = Calendar.getInstance();
				end.setTime(to);
				
				for (Date date = start.getTime() ; start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
					
					if (!PaymentUtil.isDayOff(date,currentProject.getDaysOffs()) && ! PaymentUtil.isWeekendDay(date,currentProject.getWeekendDays())) {
						tasks = currentProject.getProjectTasks();
						for (ProjectTask currentTask : tasks) {
					
							taskDate = PaymentUtil.getTaskDate(currentTask);
							calendar.setTime(taskDate);
							calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
							taskEndDate = calendar.getTime();
							if (taskDate.before(from) && ( taskEndDate.after(from) || taskEndDate.equals(from)) && ( date.after(taskDate)  || date.equals(taskDate) ) && 
									(date.before(taskEndDate) || date.equals(taskEndDate) )){
								balanceCounter -= currentTask.getUniformDailyCost().doubleValue();		
	
							}
						}
					}
					balanceCounter -= currentProject.getOverheadPerDay().doubleValue();
					if (balanceCounter < 0) {
						financeCostCounter += balanceCounter * getInterestInDay(currentProject,date);
						// balanceCounter -= balanceCounter * currentProject.getDailyInterestRate().doubleValue();	
					}
					
				}
			}
			
		}
		
		
		return financeCostCounter;
	}
	
	//Finance cost for left overs 
	public static double getPortfolioFinanceCost(HttpSession session, Portfolio portfolio, Date from, Date to) throws EntityControllerException, OptimaException {
		EntityController<Portfolio> controller = new EntityController<Portfolio>(session.getServletContext());
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = null;
		Date taskEndDate ; 
		Date taskDate ;
		Calendar calendar = Calendar.getInstance();
		List<ProjectTask> tasks;
		double balanceCounter = 0;
		double financeCostCounter = 0;
		
		List<Project> projects = portfolio.getProjects();
		for (Project currentProject : projects) {
			Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, currentProject.getProjectId())[0];
			if (projectStartDate != null) {
				cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, currentProject.getProjectId(), projectStartDate , calendar.getTime());
	
				Calendar prevDay = Calendar.getInstance();
				prevDay.setTime(from);
				prevDay.add(Calendar.DATE, -1);
				
				DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from , cashFlowInfo, currentProject.getProjectId());
				
				double firstDateFinanceCost = PaymentUtil.getDateFinanceCost(currentProject, from, cashFlowInfo); 
				financeCostCounter += firstDateFinanceCost;
				double firstDayBalance = previousDay == null? 0 : previousDay.getBalance() ;
				double projectPayment = PaymentUtil.getProjectPaymentst(currentProject, from);
				firstDayBalance += projectPayment;
				balanceCounter = firstDayBalance;
	
				Calendar start = Calendar.getInstance();
				start.setTime(from);
				Calendar end = Calendar.getInstance();
				end.setTime(to);
				
				for (Date date = start.getTime() ; start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
					
					if (!PaymentUtil.isDayOff(date,currentProject.getDaysOffs()) && ! PaymentUtil.isWeekendDay(date,currentProject.getWeekendDays())) {
						tasks = currentProject.getProjectTasks();
						for (ProjectTask currentTask : tasks) {
					
							taskDate = PaymentUtil.getTaskDate(currentTask);
							calendar.setTime(taskDate);
							calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
							taskEndDate = calendar.getTime();
							if (taskDate.before(from) && ( taskEndDate.after(from) || taskEndDate.equals(from)) && ( date.after(taskDate)  || date.equals(taskDate) ) && 
									(date.before(taskEndDate) || date.equals(taskEndDate) )){
								balanceCounter -= currentTask.getUniformDailyCost().doubleValue();		
	
							}
						}
					}
					balanceCounter -= currentProject.getOverheadPerDay().doubleValue();
					if (balanceCounter < 0) {
						financeCostCounter += balanceCounter * getInterestInDay(currentProject,date);
						// balanceCounter -= balanceCounter * currentProject.getDailyInterestRate().doubleValue();	
					}
					
				}
			}
			
		}
		
		
		return financeCostCounter;
	}

	public static double getProjectOverhead(Project project, Date from, Date to) {
		int daysInPeriod = PaymentUtil.daysBetween(from, to);	
		return daysInPeriod * project.getOverheadPerDay().doubleValue();
	}

	public static double getEligiableTaskCost( List<ProjectTask> eligibleTasks , Date to ) {
		double costAccumulator = 0;
		for (ProjectTask task : eligibleTasks) {
			
			Date taskDate = getTaskDate(task);
			
			Calendar endDate = Calendar.getInstance();
			endDate.setTime(taskDate);
			endDate.add(Calendar.DATE, task.getCalenderDuration());
			
			int taskLength = PaymentUtil.daysBetween(taskDate , endDate.getTime());
			int daysInPeriod = PaymentUtil.daysBetween(taskDate , to);
			int effictiveNumberOfDays = Math.min(taskLength, daysInPeriod);
			Date dateToCompare = null;
			if (effictiveNumberOfDays == taskLength) {
				dateToCompare =  endDate.getTime();
			} else {
				dateToCompare = to;
			}
			
			int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, taskDate, dateToCompare); // What if taskEndDate > to?
			
			effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
			
	
			costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
		}
		return costAccumulator;
	}
	
	public static double getEligiableTaskCurrentPeriodCost( List<ProjectTask> eligibleTasks , Date start, Date end) {
		double costAccumulator = 0;
		Calendar calendar = Calendar.getInstance();
		for (ProjectTask task : eligibleTasks) {
			Date taskDate = task.getCalendarStartDate();
			int daysInPeriod = PaymentUtil.daysBetween(taskDate, end);			
			int effictiveNumberOfDays = Math.min(task.getCalenderDuration() , daysInPeriod);
			if (effictiveNumberOfDays == task.getCalenderDuration()) {
				calendar.setTime(taskDate);
				calendar.add(Calendar.DATE, task.getCalenderDuration() - 1);
			} else {
				calendar.setTime(end);
			}
			int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, taskDate, calendar.getTime()); // What if taskEndDate > to?
			
			effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
			
	
			costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
		}
		return costAccumulator;
	}
	
	
	public static double getOtherProjectsCurrentPeriodCost( Project project, Date start, Date end) {
		double costAccumulator = 0;
		Portfolio portfolio = project.getPortfolio();
		List<Project> projects = portfolio.getProjects();
		for (Project currentProject : projects) {
			if (currentProject.getProjectId() != project.getProjectId()) {
				List<ProjectTask> eligibleTasks = PaymentUtil.getEligibleTasks(currentProject, start, end , false);
				costAccumulator += PaymentUtil.getEligiableTaskCurrentPeriodCost(eligibleTasks , start, end);
			}
		}
		return costAccumulator;
	}
	

	public static double getOtherProjectsLeftOverCostNew(HttpSession session, Project project, Date start, Date end, Date projectExpectedEndDate, HashMap<Integer, Integer> completedProjects) throws EntityControllerException {
		double costAccumulator = 0;
		int maxEffictiveNumberOfDays = 0;
		Portfolio portfolio = project.getPortfolio();
		List<Project> projects = portfolio.getProjects();
		for (Project currentProject : projects) {
			if (currentProject.getProjectId() != project.getProjectId()) {
				Integer projId = completedProjects.get(currentProject.getProjectId());
				for (ProjectPayment payment: currentProject.getProjectPayments()) {
					if (payment.getPaymentDate().equals(end)) {
						List<ProjectTask> eligibleTasks = PaymentUtil.getEligibleTasks(currentProject, start, end , false);
						Calendar calendar = Calendar.getInstance();
						for (ProjectTask task : eligibleTasks) {
							Date taskDate = task.getCalendarStartDate();
							int taskLength = task.getCalenderDuration();
							calendar.setTime(taskDate);
							calendar.add(Calendar.DATE, taskLength);
							Date taskEndDate = calendar.getTime();
							if (taskEndDate.getTime() >= end.getTime()) {
								int effictiveNumberOfDays = PaymentUtil.daysBetween(end, taskEndDate);
								if(maxEffictiveNumberOfDays < effictiveNumberOfDays)
									maxEffictiveNumberOfDays = effictiveNumberOfDays;
								int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, end, taskEndDate); // What if taskEndDate > to?
								effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
								costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
							}
						}
						int overheadDays = 0;
						
						if(projId !=null)
						{
							overheadDays = maxEffictiveNumberOfDays; 
						}else
							overheadDays = PaymentUtil.daysBetween(end, PaymentUtil.getNextEventNew(session, project , end, projectExpectedEndDate));
						costAccumulator += overheadDays * project.getOverheadPerDay().doubleValue();
						break;
					}		
				}
			}
		}
		return costAccumulator;
	}
	
	public static double getOtherProjectsLeftOverCost( Project project, Date start, Date end) {
		double costAccumulator = 0;
		Portfolio portfolio = project.getPortfolio();
		List<Project> projects = portfolio.getProjects();
		for (Project currentProject : projects) {
			if (currentProject.getProjectId() != project.getProjectId()) {
				for (ProjectPayment payment: currentProject.getProjectPayments()) {
					if (payment.getPaymentDate().equals(end)) {
						List<ProjectTask> eligibleTasks = PaymentUtil.getEligibleTasks(currentProject, start, end , false);
						Calendar calendar = Calendar.getInstance();
						for (ProjectTask task : eligibleTasks) {
							Date taskDate = task.getCalendarStartDate();
							int taskLength = task.getCalenderDuration();
							calendar.setTime(taskDate);
							calendar.add(Calendar.DATE, taskLength);
							Date taskEndDate = calendar.getTime();
							if (taskEndDate.getTime() >= end.getTime()) {
								int effictiveNumberOfDays = PaymentUtil.daysBetween(end, taskEndDate);
								int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, end, taskEndDate); // What if taskEndDate > to?
								effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
								costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
							}
						}
						
						
						int overheadDays = PaymentUtil.daysBetween(end, PaymentUtil.getNextEvent(project , end));
						costAccumulator += overheadDays * project.getOverheadPerDay().doubleValue();
						break;
					}		
				}
			}
		}
		return costAccumulator;
	}
	
	
	
	
	public static double getEligiableTaskLeftOverCostNew(HttpSession session, List<ProjectTask> eligibleTasks ,Project project, Date start, Date end,  Date projectExpectedEndDate) throws EntityControllerException {
		double costAccumulator = 0;
		Calendar calendar = Calendar.getInstance();
		Date nextEventDate = PaymentUtil.getNextEventNew(session, project , end, projectExpectedEndDate);
		for (ProjectTask task : eligibleTasks) {
			Date taskDate = task.getCalendarStartDate();
			int taskLength = task.getCalenderDuration();
			calendar.setTime(taskDate);
			calendar.add(Calendar.DATE, taskLength);
			Date taskEndDate = calendar.getTime();
			if (taskEndDate.getTime() >= end.getTime()) {
				Date endOfEffectiveDays = taskEndDate;
				if(nextEventDate.before(taskEndDate))
					 endOfEffectiveDays = nextEventDate;
				int effictiveNumberOfDays = PaymentUtil.daysBetween(end, endOfEffectiveDays);
				int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, end, endOfEffectiveDays); // What if taskEndDate > to?
				effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
				costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
			}
		}
		int overheadDays = 0;
		Date date = PaymentUtil.getProjectExpectedEndDate(project);
		if (date.before(nextEventDate)) {
			overheadDays = PaymentUtil.daysBetween(end, date); //end is included
			if (!end.after(date)) {
				overheadDays++;
			}
		} else {
			overheadDays = PaymentUtil.daysBetween(end, nextEventDate);
		}
		costAccumulator += overheadDays * project.getOverheadPerDay().doubleValue();
		return costAccumulator;
	}
	
	public static double getEligiableTaskLeftOverCost( List<ProjectTask> eligibleTasks ,Project project, Date start, Date end) {
		double costAccumulator = 0;
		Calendar calendar = Calendar.getInstance();
		Date nextEventDate = PaymentUtil.getNextEvent(project , end);
		for (ProjectTask task : eligibleTasks) {
			Date taskDate = task.getCalendarStartDate();
			int taskLength = task.getCalenderDuration();
			calendar.setTime(taskDate);
			calendar.add(Calendar.DATE, taskLength);
			Date taskEndDate = calendar.getTime();
			if (taskEndDate.getTime() >= end.getTime()) {
				Date endOfEffectiveDays = taskEndDate;
				if(nextEventDate.before(taskEndDate))
					 endOfEffectiveDays = nextEventDate;
				int effictiveNumberOfDays = PaymentUtil.daysBetween(end, endOfEffectiveDays);
				int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(task, end, endOfEffectiveDays); // What if taskEndDate > to?
				effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
				costAccumulator += effictiveNumberOfDays * task.getUniformDailyCost().doubleValue();
			}
		}
		int overheadDays = PaymentUtil.daysBetween(end, nextEventDate);
		costAccumulator += overheadDays * project.getOverheadPerDay().doubleValue();
		return costAccumulator;
	}

	private static Date getNextEventNew(HttpSession session, Project project, Date end, Date projectExpectedEndDate) throws EntityControllerException {
		int startsAfter = Integer.MAX_VALUE;
		ProjectPayment nextPayment = null;
		List<ProjectPayment> projectPayments = getProjectPayments(session, project, projectExpectedEndDate);
		for (ProjectPayment payment : projectPayments) {
			Date paymentDate = payment.getPaymentDate();
			if (paymentDate.getTime() > end.getTime()) {
				int startsAfterTmp = PaymentUtil.daysBetween(end, paymentDate);
				if (startsAfterTmp < startsAfter) {
					startsAfter = startsAfterTmp;
					nextPayment = payment;
				}
			}
		}
		PortfolioFinance nextFinanceChange = null;
		int financeChangeAfter = Integer.MAX_VALUE;
		for (PortfolioFinance finance : project.getPortfolio().getPortfolioFinances()) {
			Date financeDate = finance.getFinanceUntillDate();
			if (financeDate.getTime() > end.getTime()) {
				int startsAfterTmp = PaymentUtil.daysBetween(end, financeDate);
				if (startsAfterTmp < financeChangeAfter) {
					financeChangeAfter = startsAfterTmp;
					nextFinanceChange = finance;
				}
			}
		}
		
		if (startsAfter <= financeChangeAfter && nextPayment != null) { 
			return nextPayment.getPaymentDate();
		} else if (nextFinanceChange != null) {
			return nextFinanceChange.getFinanceUntillDate();
		}
		if (nextPayment == null && nextFinanceChange == null && project.getProposedFinishDate() != null ){
			return project.getProposedFinishDate();
		}
		return end;
		
	}
	
	private static Date getNextEvent(Project project, Date end) {
		int startsAfter = Integer.MAX_VALUE;
		ProjectPayment nextPayment = null;
		for (ProjectPayment payment : project.getProjectPayments()) {
			Date paymentDate = payment.getPaymentDate();
			if (paymentDate.getTime() > end.getTime()) {
				int startsAfterTmp = PaymentUtil.daysBetween(end, paymentDate);
				if (startsAfterTmp < startsAfter) {
					startsAfter = startsAfterTmp;
					nextPayment = payment;
				}
			}
		}
		PortfolioFinance nextFinanceChange = null;
		int financeChangeAfter = Integer.MAX_VALUE;
		for (PortfolioFinance finance : project.getPortfolio().getPortfolioFinances()) {
			Date financeDate = finance.getFinanceUntillDate();
			if (financeDate.getTime() > end.getTime()) {
				int startsAfterTmp = PaymentUtil.daysBetween(end, financeDate);
				if (startsAfterTmp < financeChangeAfter) {
					financeChangeAfter = startsAfterTmp;
					nextFinanceChange = finance;
				}
			}
		}
		
		if (startsAfter <= financeChangeAfter && nextPayment != null) { 
			return nextPayment.getPaymentDate();
		} else if (nextFinanceChange != null) {
			return nextFinanceChange.getFinanceUntillDate();
		}
		if (nextPayment == null && nextFinanceChange == null && project.getProposedFinishDate() != null ){
			return project.getProposedFinishDate();
		}
		return end;
		
	}

	
	public static double getEligiableTaskFinanceCostNew(HttpSession session , Project project,
			List<ProjectTask> eligibleTasks , Date from , Date to) throws EntityControllerException , OptimaException {
		EntityController<Project> controller = new EntityController<>(session.getServletContext());
		Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, project.getProjectId())[0];
		if (projectStartDate == null) {
			return 0;
		}
		Calendar firstDateCal = Calendar.getInstance();
		firstDateCal.setTime(from);
		firstDateCal.add(Calendar.DATE, -1);
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, project.getProjectId(), projectStartDate , firstDateCal.getTime());
		
		DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from, cashFlowInfo, project.getProjectId());
		
		Calendar calendar = Calendar.getInstance();

		double firstDateFinanceCost = PaymentUtil.getDateFinanceCost(project, from, cashFlowInfo); 
		double financeCostCounterEligibleTasks = 0;
		financeCostCounterEligibleTasks += firstDateFinanceCost;
		double financeCostCounterLeftovers = 0 ;
		financeCostCounterLeftovers += firstDateFinanceCost;
		
		double firstDayBalance = previousDay == null? 0 : previousDay.getBalance() ;
		firstDayBalance += PaymentUtil.getProjectPaymentstNew(session, project, from);
		double balanceCounterEligibleTasks = firstDayBalance;
		double balanceCounterLeftovers = firstDayBalance;
		
		Calendar start = Calendar.getInstance();
		start.setTime(from);
		Calendar end = Calendar.getInstance();
		end.setTime(to);
		
		for (Date date = start.getTime() ; start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			if (!PaymentUtil.isDayOff(date,project.getDaysOffs()) && ! PaymentUtil.isWeekendDay(date,project.getWeekendDays())) {
				
				for (ProjectTask currentTask : project.getProjectTasks()) {
					Date taskDate = PaymentUtil.getTaskDate(currentTask);
					calendar.setTime(taskDate);
					calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
					Date taskEndDate = calendar.getTime();
					if (( taskDate.after(from) || taskDate.equals(from) )&& taskDate.before(to) && ( date.after(taskDate) || date.equals(taskDate) ) 
							&& ( date.before(taskEndDate) || date.equals(taskEndDate))){
						balanceCounterEligibleTasks -= currentTask.getUniformDailyCost().doubleValue();		
					} else if (taskDate.before(from) && ( taskEndDate.after(from) || taskEndDate.equals(from)) && ( date.after(taskDate) || date.equals(taskDate) ) 
							&& ( date.before(taskEndDate) || date.equals(taskEndDate))) {
						balanceCounterEligibleTasks -= currentTask.getUniformDailyCost().doubleValue();
						balanceCounterLeftovers -= currentTask.getUniformDailyCost().doubleValue();
						
					}
				}
			}
			balanceCounterEligibleTasks -= project.getOverheadPerDay().doubleValue();
			balanceCounterLeftovers -= project.getOverheadPerDay().doubleValue();
			
			if (balanceCounterLeftovers < 0) {
				financeCostCounterLeftovers += balanceCounterLeftovers * getInterestInDay(project,date);
			}
			if (balanceCounterEligibleTasks < 0) {
				financeCostCounterEligibleTasks += balanceCounterEligibleTasks * getInterestInDay(project,date);
			}
		}

		return financeCostCounterEligibleTasks - financeCostCounterLeftovers;
	}
	
	
	public static double getEligiableTaskFinanceCost(HttpSession session , Project project,
			List<ProjectTask> eligibleTasks , Date from , Date to) throws EntityControllerException , OptimaException {
		EntityController<Project> controller = new EntityController<>(session.getServletContext());
		Date projectStartDate = PaymentUtil.getProjectDateRanges(controller, project.getProjectId())[0];
		if (projectStartDate == null) {
			return 0;
		}
		Calendar firstDateCal = Calendar.getInstance();
		firstDateCal.setTime(from);
		firstDateCal.add(Calendar.DATE, -1);
		Map<String, DailyCashFlowMapEntity> cashFlowInfo = PaymentUtil.getProjectCashFlowData(session, project.getProjectId(), projectStartDate , firstDateCal.getTime());
		
		DailyCashFlowMapEntity previousDay = PaymentUtil.getPreviousDayInfo(from, cashFlowInfo, project.getProjectId());
		
		Calendar calendar = Calendar.getInstance();

		double firstDateFinanceCost = PaymentUtil.getDateFinanceCost(project, from, cashFlowInfo); 
		double financeCostCounterEligibleTasks = 0;
		financeCostCounterEligibleTasks += firstDateFinanceCost;
		double financeCostCounterLeftovers = 0 ;
		financeCostCounterLeftovers += firstDateFinanceCost;
		
		double firstDayBalance = previousDay == null? 0 : previousDay.getBalance() ;
		firstDayBalance += PaymentUtil.getProjectPaymentst(project, from);
		double balanceCounterEligibleTasks = firstDayBalance;
		double balanceCounterLeftovers = firstDayBalance;
		
		Calendar start = Calendar.getInstance();
		start.setTime(from);
		Calendar end = Calendar.getInstance();
		end.setTime(to);
		
		for (Date date = start.getTime() ; start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
			if (!PaymentUtil.isDayOff(date,project.getDaysOffs()) && ! PaymentUtil.isWeekendDay(date,project.getWeekendDays())) {
				
				for (ProjectTask currentTask : project.getProjectTasks()) {
					Date taskDate = PaymentUtil.getTaskDate(currentTask);
					calendar.setTime(taskDate);
					calendar.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
					Date taskEndDate = calendar.getTime();
					if (( taskDate.after(from) || taskDate.equals(from) )&& taskDate.before(to) && ( date.after(taskDate) || date.equals(taskDate) ) 
							&& ( date.before(taskEndDate) || date.equals(taskEndDate))){
						balanceCounterEligibleTasks -= currentTask.getUniformDailyCost().doubleValue();		
					} else if (taskDate.before(from) && ( taskEndDate.after(from) || taskEndDate.equals(from)) && ( date.after(taskDate) || date.equals(taskDate) ) 
							&& ( date.before(taskEndDate) || date.equals(taskEndDate))) {
						balanceCounterEligibleTasks -= currentTask.getUniformDailyCost().doubleValue();
						balanceCounterLeftovers -= currentTask.getUniformDailyCost().doubleValue();
						
					}
				}
			}
			balanceCounterEligibleTasks -= project.getOverheadPerDay().doubleValue();
			balanceCounterLeftovers -= project.getOverheadPerDay().doubleValue();
			
			if (balanceCounterLeftovers < 0) {
				financeCostCounterLeftovers += balanceCounterLeftovers * getInterestInDay(project,date);
			}
			if (balanceCounterEligibleTasks < 0) {
				financeCostCounterEligibleTasks += balanceCounterEligibleTasks * getInterestInDay(project,date);
			}
		}

		return financeCostCounterEligibleTasks - financeCostCounterLeftovers;
	}

	

	public static int getNoOfWeekEndDaysAndDaysOff(ProjectTask currentTask,
			Date from, Date to) {
		int noOfWeekendDaysAndDaysOff = 0;
		Calendar start = Calendar.getInstance();
		start.setTime(from);
		for (Date date = start.getTime() ; date.before(to); start.add(Calendar.DATE, 1), date = start.getTime()) {
			if ( isDayOff(date, currentTask.getProject().getDaysOffs()) 
				|| isWeekendDay(date, currentTask.getProject().getWeekendDays()) ) {
				noOfWeekendDaysAndDaysOff++;
			}
		}
		
		return noOfWeekendDaysAndDaysOff;
	}
	
	
	public static boolean isDependent(ProjectTask taskA, ProjectTask taskB) {
		List<TaskDependency> dependecies = taskA.getAsDependent();
		if ( dependecies == null || dependecies.isEmpty()) {
			return false;
		}
		for (TaskDependency dependecy : dependecies) {
			if (dependecy.getDependency().getTaskId() == taskB.getTaskId()) {
				return true;
			} else {
				return isDependent(dependecy.getDependency(), taskB);
			}
		}
		return false;
		
	}
	
	
	
	/**
	 * @param session
	 * @param projectId
	 */
	public static void adjustStartDateBasedOnTaskDependency(Project project) {
		// taskController.dml(ProjectTask.class, "Update ProjectTask t set t.calendarStartDate = null where t.project = ?1", project);
		
		List<ProjectTask> rootTasks = getRootTasks(project);
		for (ProjectTask task : rootTasks) {
			processTask(task, project);
		}

	}

	public static void processTask(ProjectTask task, Project project)  {
		calculateCalederDuration(project, task);
		for (TaskDependency dependency : task.getAsDependency()) {
			ProjectTask nextTask = dependency.getDependent();
			if (nextTask != null) {
				Date nextTaskStartDate = nextTask.getCalendarStartDate();
				Calendar cal = Calendar.getInstance();
				cal.setTime(task.getCalendarStartDate());
				cal.add(Calendar.DATE, task.getCalenderDuration());
				if (nextTaskStartDate == null || nextTaskStartDate.before(cal.getTime())) {
					//BUG Previously does not consider the weekends and the daysOff
					//nextTask.setCalendarStartDate(cal.getTime());
					//Fix BUG - BassemVic

					while (PaymentUtil.isDayOff(cal.getTime(), project.getDaysOffs())  
							|| PaymentUtil.isWeekendDay(cal.getTime() ,  project.getWeekendDays()) )
					{
						cal.add(Calendar.DATE, 1);
					}
					nextTask.setCalendarStartDate(cal.getTime());
				}
				processTask(dependency.getDependent(), project);
			}
		}
	
	}

	
	/**
	 * @param project
	 * @return
	 */
	public static List<ProjectTask> getRootTasks(Project project) {
		ArrayList<ProjectTask> rootTasks = new ArrayList<ProjectTask>();
		for (ProjectTask task : project.getProjectTasks()) {
			if (task.getAsDependent() == null || task.getAsDependent().isEmpty()) {
				rootTasks.add(task);
			}
		}
		return rootTasks;
	}
	
	/**
	 * @param project
	 * @param task
	 */
	public static void calculateCalederDuration(Project project, ProjectTask task) {
		Date startDate = task.getCalendarStartDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int duration = task.getDuration();
		int calendarDuration = 0;
		while (duration > 0) {
			if ( PaymentUtil.isDayOff(startDate, project.getDaysOffs())
					||  PaymentUtil.isWeekendDay(startDate, project.getWeekendDays())) {
				calendarDuration++;
			} else {
				duration--;
				calendarDuration++;
			}
			calendar.add(Calendar.DATE, 1);
			startDate = calendar.getTime();
			

		}
		task.setCalenderDuration(calendarDuration);
	}

	public static int getProjectLength(Project project) {
		List<ProjectTask> tasks = project.getProjectTasks();
		if (tasks == null || tasks.size() <= 0)
			return 0;
		
		Date minStartDate = tasks.get(0).getCalendarStartDate(); // getTaskDate(tasks.get(0)); //
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(minStartDate);
		calendar.add(Calendar.DATE, tasks.get(0).getCalenderDuration() );
		Date maxEndDate = calendar.getTime();
		
		for (ProjectTask currentTask : tasks) {
			
			Date taskStartDate = currentTask.getCalendarStartDate();  // 	getTaskDate(currentTask);
			if (taskStartDate.before(minStartDate)) {
				minStartDate = taskStartDate;
			}
			
			calendar.setTime(taskStartDate);
			calendar.add(Calendar.DATE, currentTask.getCalenderDuration() );
			Date taskEndDate = calendar.getTime();
			if (taskEndDate.after(maxEndDate)) {
				maxEndDate = taskEndDate;
			}
		}
		int numberOfDays = PaymentUtil.daysBetween(minStartDate, maxEndDate);
		return numberOfDays;
	}

	public static Date getProjectExpectedEndDate(Project project) {
		List<ProjectTask> tasks = project.getProjectTasks();
		if (tasks == null || tasks.size() <= 0)
			return project.getPropusedStartDate();
		
		Date minStartDate = tasks.get(0).getCalendarStartDate(); // getTaskDate(tasks.get(0)); //
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(minStartDate);
		calendar.add(Calendar.DATE, tasks.get(0).getCalenderDuration() );
		Date maxEndDate = calendar.getTime();
		
		for (ProjectTask currentTask : tasks) {
			
			Date taskStartDate = currentTask.getCalendarStartDate();  // 	getTaskDate(currentTask);
			if (taskStartDate.before(minStartDate)) {
				minStartDate = taskStartDate;
			}
			
			calendar.setTime(taskStartDate);
			calendar.add(Calendar.DATE, currentTask.getCalenderDuration()-1 );
			Date taskEndDate = calendar.getTime();
			if (taskEndDate.after(maxEndDate)) {
				maxEndDate = taskEndDate;
			}
		}
		return maxEndDate;
	}

	/*
	 * First select the solution with minimum project length If multiple, select
	 * the feasible one If multiple select the maximum cost If none select the
	 * minimum cost.
	 */
	public static TaskSolution findSolutionIteration(List<TaskSolution> solutions, double cashAvailable, double cashAvailableNextPeriod) {
		if (null == solutions || solutions.isEmpty()) {
			return null;
		}
		
		List<TaskSolution> minimumProjectLengthSolutions = new ArrayList<TaskSolution>();

		int projectLength = Integer.MAX_VALUE;
		for (TaskSolution taskSolution : solutions) {	
			if (taskSolution.getProjectLength() < projectLength) {
					minimumProjectLengthSolutions.clear();
					minimumProjectLengthSolutions.add(taskSolution);
					projectLength = taskSolution.getProjectLength();
				} else if (taskSolution.getProjectLength() == projectLength ) {
					minimumProjectLengthSolutions.add(taskSolution);
				}
		}
		if (minimumProjectLengthSolutions.size() == 1) {
			return minimumProjectLengthSolutions.get(0);
		}
		
		List<TaskSolution> feasibleProjectsSolutions = new ArrayList<TaskSolution>();
		for (TaskSolution taskSolution : minimumProjectLengthSolutions) {
			if (isFeasibleSolution(taskSolution, cashAvailable, cashAvailableNextPeriod)) {
				feasibleProjectsSolutions.add(taskSolution);
			}
		}
		
		if (feasibleProjectsSolutions.size() == 1) {
			return feasibleProjectsSolutions.get(0);
		}
		
		double maxCost = Integer.MIN_VALUE;
		List<TaskSolution> maxCostSolutions = new ArrayList<TaskSolution>();
		for (TaskSolution taskSolution : feasibleProjectsSolutions) {
			if (taskSolution.getCurrentPeriodCost() > maxCost) {
				maxCostSolutions.clear();
				maxCostSolutions.add(taskSolution);
				maxCost = taskSolution.getCurrentPeriodCost();
			} else if (taskSolution.getCurrentPeriodCost() == maxCost) {
				maxCostSolutions.add(taskSolution);
			}
		}
		
		if (maxCostSolutions.size() == 1) {
			return maxCostSolutions.get(0);
		}
		
		double minCost = Integer.MAX_VALUE;
		TaskSolution minimumCostSolution = null;
		for (TaskSolution taskSolution : minimumProjectLengthSolutions) {
			if (taskSolution.getCurrentPeriodCost() < minCost) {
				minCost = taskSolution.getCurrentPeriodCost();
				minimumCostSolution = taskSolution;
			}
		}
		return minimumCostSolution;
	}
	
	public static TaskSolution findBestFeasbileIteration(List<TaskSolution> solutions, double cashAvailable, double cashAvailableNextPeriod) {
		if (null == solutions || solutions.isEmpty()) {
			return null;
		}
		List<TaskSolution> minimumProjectLengthSolutions = new ArrayList<TaskSolution>();

		int projectLength = Integer.MAX_VALUE;
		for (TaskSolution taskSolution : solutions) {
			
			if (isFeasibleSolution(taskSolution, cashAvailable, cashAvailableNextPeriod)) {
				if (taskSolution.getProjectLength() < projectLength) {
					minimumProjectLengthSolutions.clear();
					minimumProjectLengthSolutions.add(taskSolution);
					projectLength = taskSolution.getProjectLength();
				} else if (taskSolution.getProjectLength() == projectLength ) {
					minimumProjectLengthSolutions.add(taskSolution);
				}
			}
		}
		if (minimumProjectLengthSolutions.size() == 1) {
			return minimumProjectLengthSolutions.get(0);
		} else {
			double maxCost = Double.MIN_VALUE;
			TaskSolution taskSolution = null;
			for (TaskSolution minPrjLengthTask : minimumProjectLengthSolutions) {
				if (minPrjLengthTask.getCurrentPeriodCost() > maxCost) {
					maxCost = minPrjLengthTask.getCurrentPeriodCost();
					taskSolution = minPrjLengthTask;
				}
			}
			
			return taskSolution;
		}
	}
	
	public static TaskSolution findBestIteration(List<TaskSolution> solutions) {
		if (null == solutions || solutions.isEmpty()) {
			return null;
		}
		List<TaskSolution> minimumProjectLengthSolutions = new ArrayList<TaskSolution>();

		int projectLength = Integer.MAX_VALUE;
		for (TaskSolution taskSolution : solutions) {
			if (taskSolution.getProjectLength() < projectLength) {
				minimumProjectLengthSolutions.clear();
				minimumProjectLengthSolutions.add(taskSolution);
				projectLength = taskSolution.getProjectLength();
			} else if (taskSolution.getProjectLength() == projectLength) {
				minimumProjectLengthSolutions.add(taskSolution);
			}
		}
		if (minimumProjectLengthSolutions.size() == 1) {
			return minimumProjectLengthSolutions.get(0);
		} else {
			double maxCost = Integer.MAX_VALUE;
			TaskSolution taskSolution = null;
			for (TaskSolution minPrjLengthTask : minimumProjectLengthSolutions) {
				if (minPrjLengthTask.getCurrentPeriodCost() < maxCost) {
					maxCost = minPrjLengthTask.getCurrentPeriodCost();
					taskSolution = minPrjLengthTask;
				}
			}
			
			return taskSolution;
		}
	}
	
	public static TaskSolution findBestIterationNew(List<TaskSolution> solutions, double cashAvailable, double cashAvailableNextPeriod) {
		TaskSolution solution = null;
		solution = PaymentUtil.findBestFeasbileIteration(solutions, cashAvailable, cashAvailableNextPeriod);
		if (solution == null) {
			solution = PaymentUtil.findBestIteration(solutions);
		}
		return solution;
	}

	public static boolean isFeasibleSolution (TaskSolution solution, double cashAvailable,  double cashAvailableNextPeriod) {
		// 				if (totalCostCurrent < cashAvailable && cashAvailable - totalCostCurrent + expectedCashIn > leftOverNextCost ) {
		if (solution.getCurrentPeriodCost() < cashAvailable && cashAvailableNextPeriod - solution.getCurrentPeriodCost() + solution.getIncome() > solution.getLeftOversCost()) 
			return true;
		return false;
	}
	
	public static double getCashOutOtherProjects(HttpSession session,
			Project project, Date from, Date to , String solvedProjects) throws EntityControllerException, OptimaException {
		double cost = 0;
		String[] solvedProjectsArray = null;
		
		Portfolio portfolio = project.getPortfolio();
		if (solvedProjects != null && solvedProjects.length() > 0) 
		{
			solvedProjectsArray = solvedProjects.split(",");
			for (int i = 0 ; i < solvedProjectsArray.length; i++) {
				List<Project> projects = portfolio.getProjects();
				int projectId = Integer.parseInt(solvedProjectsArray[i]);
				for (Project currentProject : projects) {
					if (currentProject.getProjectId() == projectId) {
						List<ProjectTask> eligibleTasks = PaymentUtil.getEligibleTasks(currentProject, from, to , false);
						cost += PaymentUtil.getEligiableTaskCost(eligibleTasks , to);
					}
				}
			}
		}
		return cost;
	}

	
	public static Date maxDate(Date date1 , Date date2) {
		if (date1.before(date2)) {
			return date2;
		} else {
			return date1;
		}
	}
	
	public static Date minDate(Date date1 , Date date2) {
		if (date1.before(date2)) {
			return date1;
		} else {
			return date2;
		}
	}
	
	public static double getExpectedPayment ( Project project, Date paymentStart, Date paymentEnd, Date to,
			double extraPayments, double retainagePercentage, double advancedPaymentDeduction){
		
		Date periodStart = getProjectPaymentCycleStart(project , to);
		
		if (periodStart == null) {
			return 0.0; //-- no period for that project
		}

		double totalTasksPayment = getExpectedPaymentNoAdjustments(project,
				paymentStart, paymentEnd);
		totalTasksPayment -= (totalTasksPayment * retainagePercentage /100);
		totalTasksPayment += extraPayments;
		totalTasksPayment -= advancedPaymentDeduction;
		
		return totalTasksPayment;

		}
	
	
	public static double getExpectedPaymentNew (HttpSession session, Project project, Date paymentStart, Date paymentEnd, Date to,
			double extraPayments, double retainagePercentage, double advancedPaymentDeduction, Date projectExpectedEndDate, boolean applyRetainedAmount) throws EntityControllerException{
		
		Date periodStart = getProjectPaymentCycleStartNew(session, project , to, projectExpectedEndDate);
		
		if (periodStart == null) {
			return 0.0; //-- no period for that project
		}
		double totalTasksPayment = 0;
		if(paymentStart != null && paymentEnd != null)
			totalTasksPayment = getExpectedPaymentNoAdjustments(project,paymentStart, paymentEnd);
		double totalTasksPaymentAfterReduction = totalTasksPayment;
		if(applyRetainedAmount)
		{
			totalTasksPaymentAfterReduction  = totalTasksPayment - (totalTasksPayment * retainagePercentage);
			totalTasksPaymentAfterReduction += extraPayments;
			totalTasksPaymentAfterReduction -= totalTasksPayment * advancedPaymentDeduction;
			
		}
		
		return totalTasksPaymentAfterReduction;

		}

	private static double getExpectedPaymentNoAdjustments(Project project,
			Date paymentStart, Date paymentEnd) {
		Date taskEndDate ; 
		Date taskDate ;
		int effictiveNumberOfDays;
		double totalTasksPayment = 0;
		
		
		Calendar cal = Calendar.getInstance();
		
		List<ProjectTask> projectTasks = project.getProjectTasks();

		for (ProjectTask currentTask : projectTasks) {
			taskDate = PaymentUtil.getTaskDate(currentTask);
			cal.setTime(taskDate);
			cal.add(Calendar.DATE, currentTask.getCalenderDuration() - 1);
			taskEndDate = cal.getTime();
			
			if ((taskDate.getTime() < paymentEnd.getTime()  &&  taskEndDate.getTime() >= paymentStart.getTime())) {
				Date startDate = taskDate.getTime() > paymentStart.getTime() ? taskDate : paymentStart;
				Date endDate = taskEndDate.getTime() > paymentEnd.getTime() ? paymentEnd : taskEndDate;
				
				effictiveNumberOfDays = PaymentUtil.daysBetween(startDate , endDate) + (endDate.equals(paymentEnd)?0:1);
				 
				
				
				int noOfWeekendDaysAndDaysOf = PaymentUtil.getNoOfWeekEndDaysAndDaysOff(currentTask, startDate, endDate); // What if taskEndDate > to?
				
				effictiveNumberOfDays -= noOfWeekendDaysAndDaysOf;
				double taskIncome = currentTask.getUniformDailyIncome().doubleValue() * effictiveNumberOfDays;
				totalTasksPayment += taskIncome;

				
			}
		}
		return totalTasksPayment;
	}
	
	
	private static Date getProjectPaymentCycleStartNew(HttpSession session, Project project, Date periodEnd, Date projectExpectedEndDate) throws EntityControllerException {
		ProjectPayment startPayment = null;
		int minNumberOfDays = Integer.MAX_VALUE;
		List<ProjectPayment> projectPayments = getProjectPayments(session, project, projectExpectedEndDate);
		for (ProjectPayment payment : projectPayments) {
			if (payment.getPaymentDate().before(periodEnd)) {
				int numberOfDays = daysBetween(payment.getPaymentDate() , periodEnd);
				if (numberOfDays < minNumberOfDays) {
					startPayment = payment;
					minNumberOfDays = numberOfDays;
				}
			}
		}
		if (startPayment == null) {
			
			boolean firstDate = true; 
			Date startDate = null;
			for (ProjectTask task : project.getProjectTasks()) {
				if (firstDate) {
					startDate = getTaskDate(task);
					firstDate = false;
				} else {
					Date taskStartDate = getTaskDate(task); 
					if (taskStartDate.before(startDate)) {					
						startDate =  taskStartDate;
					}
				}
			}
			return startDate;
		} else {
			return startPayment.getPaymentDate();
		}
	}

	private static Date getProjectPaymentCycleStart(Project project,
			Date periodEnd) {
		ProjectPayment startPayment = null;
		int minNumberOfDays = Integer.MAX_VALUE;
		for (ProjectPayment payment : project.getProjectPayments()) {
			if (payment.getPaymentDate().before(periodEnd)) {
				int numberOfDays = daysBetween(payment.getPaymentDate() , periodEnd);
				if (numberOfDays < minNumberOfDays) {
					startPayment = payment;
					minNumberOfDays = numberOfDays;
				}
			}
		}
		if (startPayment == null) {
			
			boolean firstDate = true; 
			Date startDate = null;
			for (ProjectTask task : project.getProjectTasks()) {
				if (firstDate) {
					startDate = getTaskDate(task);
					firstDate = false;
				} else {
					Date taskStartDate = getTaskDate(task); 
					if (taskStartDate.before(startDate)) {					
						startDate =  taskStartDate;
					}
				}
			}
			return startDate;
		} else {
			return startPayment.getPaymentDate();
		}
	}
	
	public static List<ProjectPayment> getProjectPayments(HttpSession session, Project project, Date projectExpectedEndDate) throws EntityControllerException {

		//if multiple projects, the aligned period will be shorted that each project expected period, which might cause problems,
		if (project.getPortfolio().getProjects().size()>1)
		{
			Calendar cal = Calendar.getInstance();
			cal = Calendar.getInstance();
	        cal.setTime(projectExpectedEndDate);
	        cal.add(Calendar.DATE, project.getPaymentRequestPeriod());
	        projectExpectedEndDate = cal.getTime();
		}
		
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		List<ProjectPayment> projectPayments = new ArrayList<ProjectPayment>();
		Date projectStartDate = project.getPropusedStartDate(); 
		if(projectStartDate == null)
		{
			String query = "select min(calendar_start_date) from project_task where project_id = ?";
			List<?> results = controller.nativeQuery(query, project.getProjectId());
			if (results != null && results.size() > 0) {
				projectStartDate = (Date) results.get(0);
			}
		}
			
		int paymentRequestPeriod = project.getPaymentRequestPeriod();
		int collectionPeriod = project.getCollectPaymentPeriod();
		
		boolean reachProjectEnd = false;
		int i=1;
		//Add projectStartDate
		ProjectPayment projectPayment = new ProjectPayment();
		
		projectPayment.setPaymentDate(projectStartDate);
		
		PaymentType paymentType = new PaymentType();
		paymentType.setPaymentTypeId(1);  //Advance
		paymentType.setPaymentType("Advance");  //Advance
		projectPayment.setPaymentType(paymentType);

		projectPayments.add(projectPayment);
		while(!reachProjectEnd){
			Calendar cal = Calendar.getInstance();
	        cal.setTime(projectStartDate);
	        int l = paymentRequestPeriod + ((i-1)*collectionPeriod);
	        cal.add(Calendar.DATE, l); // play here
	        Date paymentDate = cal.getTime();
	        projectPayment = new ProjectPayment();
	        
	        cal = Calendar.getInstance();
	        cal.setTime(paymentDate);
	        cal.add(Calendar.DATE, -1 * collectionPeriod);
	        Date paymentEnd = cal.getTime();
	        
	        cal = Calendar.getInstance();
	        cal.setTime(paymentEnd);
	        cal.add(Calendar.DATE, -1 * paymentRequestPeriod);
	        Date paymentStart = cal.getTime();
	        
	        double paymentAmount = getExpectedPaymentNoAdjustments(project,paymentStart, paymentEnd);
				
	        double retainagePercentage = project.getRetainedPercentage().doubleValue();
	        double advancedPaymentDeduction = paymentAmount  * project.getAdvancedPaymentPercentage().doubleValue();
	        		
	        		
	        		
	        paymentAmount -= (paymentAmount * retainagePercentage);
	        paymentAmount -= advancedPaymentDeduction;
			
		
			projectPayment.setPaymentAmount(new BigDecimal(paymentAmount));
			
			
			projectPayment.setPaymentDate(paymentDate);
			paymentType = new PaymentType();
			paymentType.setPaymentTypeId(2); //Interim
			paymentType.setPaymentType("Intrim");  //Interim
			projectPayment.setPaymentType(paymentType);
			projectPayments.add(projectPayment);
	        
			i++;
	        if(paymentDate.after(projectExpectedEndDate))
	        	reachProjectEnd = true;
		}
		return projectPayments;
	}
	
	
	public static double getPortfolioPayment(Date from , Date to, Map<Integer , ProjectPaymentDetail> paymentDetails , Portfolio portfolio, boolean doNotCalculate) {
		double expectedPayment = 0;
		
		for (Project project : portfolio.getProjects()) {
			ProjectPaymentDetail detail = paymentDetails.get(project.getProjectId());
			
			for (ProjectPayment payment: project.getProjectPayments()) {
				
				if (!doNotCalculate && payment.getPaymentType().getPaymentType().equalsIgnoreCase("Intrim") && payment.getPaymentDate().equals(from) && payment.getPaymentAmount().doubleValue() == 0) {
					if (detail != null) {
						expectedPayment += PaymentUtil.getExpectedPayment(project,  detail.getPaymentStart(),  detail.getPaymentEnd(), to,  detail.getExtra(), 
								detail.getRetained(), detail.getRepayment());
					} else {
						//expectedPayment += PaymentUtil.getExpectedPayment(project,  from ,  to , to,  0, 0, 0);
						expectedPayment += payment.getPaymentAmount().doubleValue();
					}
				} else if (payment.getPaymentDate().equals(from)) {
					expectedPayment += payment.getPaymentAmount().doubleValue();
				}
			}
		
		}
		return expectedPayment;
	}
	

	public static double getPortfolioPaymentNew(HttpSession session, Date from , Date to, Map<Integer , ProjectPaymentDetail> paymentDetails , Portfolio portfolio, boolean doNotCalculate, Date[] projectBoundaries) throws EntityControllerException {
		double expectedPayment = 0;
		
		for (Project project : portfolio.getProjects()) {
			List<ProjectPayment> projectPayments = getProjectPayments(session, project, projectBoundaries[1]);
			ProjectPaymentDetail detail = paymentDetails.get(project.getProjectId());
			
			for (ProjectPayment payment: projectPayments) {
				
				if (!doNotCalculate && payment.getPaymentType().getPaymentType().equalsIgnoreCase("Intrim") && payment.getPaymentDate().equals(from) && payment.getPaymentAmount().doubleValue() == 0) {
					if (detail != null) {
						expectedPayment += PaymentUtil.getExpectedPaymentNew(session, project,  detail.getPaymentStart(),  detail.getPaymentEnd(), to,  detail.getExtra(), 
								detail.getRetained(), detail.getRepayment(), projectBoundaries[1], true);
					} else {
						//expectedPayment += PaymentUtil.getExpectedPayment(project,  from ,  to , to,  0, 0, 0);
						expectedPayment += payment.getPaymentAmount().doubleValue();
					}
				} else if (payment.getPaymentDate().equals(from)) {
					expectedPayment += payment.getPaymentAmount().doubleValue();
				}
			}
		
		}
		return expectedPayment;
	}
	
	
	public static double getProjectPayment(Date from, Date to, double advanceRepayment,
			double retainPercent, double extraPayment, Date paymentStart, Date paymentEnd ,
			Project project) {
		double expectedPayment = 0;
	
		for (ProjectPayment payment: project.getProjectPayments()) {
			
			if (payment.getPaymentType().getPaymentType().equalsIgnoreCase("Intrim") && payment.getPaymentDate().equals(from) && payment.getPaymentAmount().doubleValue() == 0) {
				expectedPayment += PaymentUtil.getExpectedPayment(project, paymentStart, paymentEnd , to,  extraPayment, retainPercent, advanceRepayment);
			} else if (payment.getPaymentDate().equals(from)) {
				expectedPayment += payment.getPaymentAmount().doubleValue();
			}
		}
	return expectedPayment;
	}
	
	public static double getProjectPaymentNoAdjustments(Date from, Date to, Date paymentStart, Date paymentEnd ,
			Project project) {
		double expectedPayment = 0;
	
		for (ProjectPayment payment: project.getProjectPayments()) {
			
			if (payment.getPaymentType().getPaymentType().equalsIgnoreCase("Intrim") && payment.getPaymentDate().equals(from) && payment.getPaymentAmount().doubleValue() == 0) {
				expectedPayment += PaymentUtil.getExpectedPaymentNoAdjustments(project, paymentStart, paymentEnd);
			} else if (payment.getPaymentDate().equals(from)) {
				expectedPayment += payment.getPaymentAmount().doubleValue();
			}
		}
	return expectedPayment;
	}

	public static double getNextPeriodLeftOverFinanceCost(double openBalance, Date to,
			Project project, List<ProjectTask> eligibleTasks) {
		Calendar calendar = Calendar.getInstance();
		double balanceCounter = openBalance;
		double financeCostCounter = 0;
		Date tasksEndDate = to;
		for ( ProjectTask task : eligibleTasks) {
			Date taskDate = PaymentUtil.getTaskDate(task);
			calendar.setTime(taskDate);
			calendar.add(Calendar.DATE, task.getCalenderDuration() - 1);
			Date taskEndDate = calendar.getTime();
			if (taskEndDate.after(tasksEndDate)) {
				tasksEndDate = taskEndDate;
			}
		}
		
		// loop from "to" to tasksEndDate
		calendar.setTime(to);
		for (Date date = calendar.getTime() ; date.before(tasksEndDate); calendar.add(Calendar.DATE, 1), date = calendar.getTime()) {
			if (!PaymentUtil.isDayOff(date,project.getDaysOffs()) && ! PaymentUtil.isWeekendDay(date,project.getWeekendDays())) {
				
				for ( ProjectTask task : eligibleTasks) {
					Date taskDate = PaymentUtil.getTaskDate(task);
					Calendar cal = Calendar.getInstance();
					cal.setTime(taskDate);
					cal.add(Calendar.DATE, task.getCalenderDuration() - 1);
					Date taskEndDate = calendar.getTime();
					if ( date.getTime() <= taskEndDate.getTime()) {
						balanceCounter -= task.getUniformDailyCost().doubleValue();	
					}
				}
			}
			balanceCounter -= project.getOverheadPerDay().doubleValue();
			
			if (balanceCounter < 0) {
				financeCostCounter  += balanceCounter * getInterestInDay(project, date);
			}
			
		}

		
		return financeCostCounter;
	}

	public static Object getEligibaleTaskNameList(
			List<ProjectTask> tasks) {
		StringBuilder builder = new StringBuilder();
		for (ProjectTask task : tasks) {
			builder.append(task.getTaskName());
			builder.append(",");
		}
		if (builder.length() > 0 ) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	public static Object getTaskListStart(List<ProjectTask> tasks) {
		
		StringBuilder builder = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		for (ProjectTask task : tasks) {
			calendar.setTime(task.getCalendarStartDate());
			builder.append(String.format("%d/%d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
			builder.append(",");
		}
		if (builder.length() > 0 ) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	public static Map<Integer, ProjectPaymentDetail> getPaymentDetailsMap(
			String paymentDetailsJson) {
		Map<Integer , ProjectPaymentDetail> paymentDetails = new HashMap<Integer, ProjectPaymentDetail>();
		try {
			JSONArray jsonObj = new JSONArray(paymentDetailsJson);
			for ( int i = 0; i < jsonObj.length(); i++) {
				JSONObject obj =  (JSONObject)jsonObj.get(i);
				ProjectPaymentDetail paymentDetail = new ProjectPaymentDetail();
				paymentDetail.setExtra(obj.getDouble("extra"));
				paymentDetail.setPayment(obj.getDouble("payment"));
				paymentDetail.setPaymentEnd(new Date(obj.getLong("paymentEnd")));
				paymentDetail.setPaymentStart(new Date(obj.getLong("paymentStart")));
				paymentDetail.setProject(obj.getString("project"));
				paymentDetail.setProjectId(obj.getInt("projectId"));
				paymentDetail.setRepayment(obj.getDouble("repayment"));
				paymentDetail.setRetained(obj.getDouble("retained"));
				
				paymentDetails.put(paymentDetail.getProjectId(), paymentDetail);
			}
			
		} catch (JSONException e1) {
			
			e1.printStackTrace();
			
		}
		return paymentDetails;
	}
	
	public static Map<Integer, ProjectPaymentDetail> getPaymentDetailsNew(HttpSession session , int projectId, Date from, Date to) {
		EntityController<Project> controller = new EntityController<Project>(session.getServletContext());
		ProjectPaymentDetail paymentDetail = new ProjectPaymentDetail();
		Map<Integer , ProjectPaymentDetail> paymentDetails = new HashMap<Integer, ProjectPaymentDetail>();
		try {
			
			Project project = controller.find(Project.class , projectId);
			Date[] projectBoundaries = PaymentUtil.getPortofolioDateRanges(controller, project.getPortfolio().getPortfolioId());
			
			Date projectStartDate = project.getPropusedStartDate(); 
			if(projectStartDate == null)
			{
				String query = "select min(calendar_start_date) from project_task where project_id = ?";
				List<?> results = controller.nativeQuery(query, project.getProjectId());
				if (results != null && results.size() > 0) {
					projectStartDate = (Date) results.get(0);
				}
			}
			
			int paymentRequestPeriod = project.getPaymentRequestPeriod();
			int collectionPeriod = project.getCollectPaymentPeriod();
			
			boolean reachProjectEnd = false;
			int i=1;
			while(!reachProjectEnd){
				Calendar cal = Calendar.getInstance();
		        cal.setTime(projectStartDate);
		        cal.add(Calendar.DATE, i*paymentRequestPeriod+collectionPeriod);
		        Date collectionDate = cal.getTime();
		        if(collectionDate.getTime() >= from.getTime() && collectionDate.getTime() < to.getTime())
		        {
			        cal.setTime(projectStartDate);
			        cal.add(Calendar.DATE, (i-1)*paymentRequestPeriod);
			        paymentDetail.setPaymentStart(cal.getTime());
			        
			        cal.setTime(projectStartDate);
			        cal.add(Calendar.DATE, (i)*paymentRequestPeriod);
			        paymentDetail.setPaymentEnd(cal.getTime()); 
	
		        }
		        i++;
		        if(collectionDate.after(projectBoundaries[1]))
		        	reachProjectEnd = true;
			}
			
			List<PortfolioExtrapayment> extraPaymentsList = project.getPortfolio().getPortfolioExtrapayments();
			for (PortfolioExtrapayment extraPayment : extraPaymentsList) {
				if(extraPayment.getExtraPayment_date().getTime() >= from.getTime() && extraPayment.getExtraPayment_date().getTime() < to.getTime())
		        {
					paymentDetail.setExtra(extraPayment.getExtraPayment_amount().doubleValue());
					paymentDetail.setPayment(extraPayment.getExtraPayment_amount().doubleValue());
	
		        }
			}
			
			paymentDetail.setProjectId(projectId);
			paymentDetail.setRetained(project.getRetainedPercentage().doubleValue());
			paymentDetail.setProject(project.getProjectCode());
			//paymentDetail.setRepayment(project.getAdvancedPaymentPercentage().doubleValue() * project.getAdvancedPaymentAmount().doubleValue());
			paymentDetail.setRepayment(project.getAdvancedPaymentPercentage().doubleValue());
			
			
			paymentDetails.put(paymentDetail.getProjectId(), paymentDetail);
			
		} catch (EntityControllerException e) {
			e.printStackTrace();
		}
		return paymentDetails;
	}
	
	
}


