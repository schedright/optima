package com.softpoint.optima.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.ProjectTask;
import com.softpoint.optima.db.WeekendDay;

public class TaskUtil {

	public static int calculateTaskDuration(ProjectTask task) {
		int totalDays = 0;
		int countDown = task.getDuration();
		Calendar start = Calendar.getInstance();
		start.setTime(task.getEffectiveTentativeStartDate());
		while (countDown>0) {
			Date date = start.getTime() ;
			start.add(Calendar.DATE, 1);
			if ( TaskUtil.isDayOff(date, task.getProject().getDaysOffs()) || TaskUtil.isWeekendDay(date, task.getProject().getWeekendDays())) {
				totalDays ++;
				continue;
			} else {
				countDown--;
				totalDays++;
			}
		}
		return totalDays;
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

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days); // minus number would decrement the days
		return cal.getTime();
	}

}
