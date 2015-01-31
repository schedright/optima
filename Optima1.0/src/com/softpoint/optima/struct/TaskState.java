/**
 * 
 */
package com.softpoint.optima.struct;

import java.util.Date;

/**
 * @author WDARWISH
 *
 */
public class TaskState {
	
	private Date originalStartDate;
	private Date shiftedStartDate;
	private int  originalCalendarDuration;
	private int  shiftedCalendarDuration;
	private Date originalScheduledStartDate;
	private Date shiftedScheduledStartData;
	
	/**
	 * 
	 */
	public TaskState(Date originalStartDate, Date shiftSatrtDate , int originalCalendarDuration , int shiftedCalendarDuration , Date originalScheduledStartDate , Date shiftedScheduledStartDate) {
		this.originalStartDate = originalStartDate;
		this.shiftedStartDate = shiftSatrtDate;
		this.setOriginalCalendarDuration(originalCalendarDuration);
		this.setShiftedCalendarDuration(shiftedCalendarDuration);
		this.setOriginalScheduledStartDate(originalScheduledStartDate);
		this.setShiftedScheduledStartData(shiftedScheduledStartDate);
	}

	/**
	 * @return the originalStartDate
	 */
	public Date getOriginalStartDate() {
		return originalStartDate;
	}

	/**
	 * @param originalStartDate the originalStartDate to set
	 */
	public void setOriginalStartDate(Date originalStartDate) {
		this.originalStartDate = originalStartDate;
	}

	/**
	 * @return the shiftedStartDate
	 */
	public Date getShiftedStartDate() {
		return shiftedStartDate;
	}

	/**
	 * @param shiftedStartDate the shiftedStartDate to set
	 */
	public void setShiftedStartDate(Date shiftedStartDate) {
		this.shiftedStartDate = shiftedStartDate;
	}

	/**
	 * @return the originalCalendarDuration
	 */
	public int getOriginalCalendarDuration() {
		return originalCalendarDuration;
	}

	/**
	 * @param originalCalendarDuration the originalCalendarDuration to set
	 */
	public void setOriginalCalendarDuration(int originalCalendarDuration) {
		this.originalCalendarDuration = originalCalendarDuration;
	}

	/**
	 * @return the shiftedCalendarDuration
	 */
	public int getShiftedCalendarDuration() {
		return shiftedCalendarDuration;
	}

	/**
	 * @param shiftedCalendarDuration the shiftedCalendarDuration to set
	 */
	public void setShiftedCalendarDuration(int shiftedCalendarDuration) {
		this.shiftedCalendarDuration = shiftedCalendarDuration;
	}

	/**
	 * @return the originalScheduledStartDate
	 */
	public Date getOriginalScheduledStartDate() {
		return originalScheduledStartDate;
	}

	/**
	 * @param originalScheduledStartDate the originalScheduledStartDate to set
	 */
	public void setOriginalScheduledStartDate(Date originalScheduledStartDate) {
		this.originalScheduledStartDate = originalScheduledStartDate;
	}

	/**
	 * @return the shiftedScheduledStartData
	 */
	public Date getShiftedScheduledStartData() {
		return shiftedScheduledStartData;
	}

	/**
	 * @param shiftedScheduledStartData the shiftedScheduledStartData to set
	 */
	public void setShiftedScheduledStartData(Date shiftedScheduledStartData) {
		this.shiftedScheduledStartData = shiftedScheduledStartData;
	}

}
