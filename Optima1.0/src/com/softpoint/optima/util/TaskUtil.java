package com.softpoint.optima.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.softpoint.optima.db.DaysOff;
import com.softpoint.optima.db.ProjectTask;

public class TaskUtil {

	public static int calculateTaskDuration(ProjectTask task) {
		int totalDays = 0;
		int countDown = task.getDuration();
		Calendar start = Calendar.getInstance();
		start.setTime(task.getEffectiveTentativeStartDate());
		while (countDown>0) {
			Date date = start.getTime() ;
			start.add(Calendar.DATE, 1);
			if ( TaskUtil.isDayOff(date, task.getProject().getDaysOffs()) || TaskUtil.isWeekendDay(date, task.getProject().getWeekend())) {
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
	public static boolean isWeekendDay(Date date, String weekend) {
		if (weekend == null) return false;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK); //Sun 1, Mon 2 ... 
		if (weekend.length()>=dayOfTheWeek) {
			if (weekend.charAt(dayOfTheWeek-1)=='1') {
				return true;
			}
		}
		return false;
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
