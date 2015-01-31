/**
 * 
 */
package com.softpoint.optima.struct;

import java.util.Date;

/**
 * @author WDARWISH
 *
 */
public class SolvedTask {
	private int taskId;
	private String taskName;
	private String taskDescription;
	private int calenderDuration;
	private Date scheduledStartDate;
	
	
	
	/**
	 * @param taskId
	 * @param taskName
	 * @param taskDescription
	 * @param calenderDuration
	 * @param scheduledStartDate
	 */
	public SolvedTask(int taskId, String taskName, String taskDescription,
			int calenderDuration, Date scheduledStartDate) {
		super();
		this.taskId = taskId;
		this.taskName = taskName;
		this.taskDescription = taskDescription;
		this.calenderDuration = calenderDuration;
		this.scheduledStartDate = scheduledStartDate;
	}

	/**
	 * @return the taskId
	 */
	public int getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	/**
	 * @return the taskName
	 */
	public String getTaskName() {
		return taskName;
	}

	/**
	 * @param taskName the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return the taskDescription
	 */
	public String getTaskDescription() {
		return taskDescription;
	}

	/**
	 * @param taskDescription the taskDescription to set
	 */
	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	/**
	 * @return the calenderDuration
	 */
	public int getCalenderDuration() {
		return calenderDuration;
	}

	/**
	 * @param calenderDuration the calenderDuration to set
	 */
	public void setCalenderDuration(int calenderDuration) {
		this.calenderDuration = calenderDuration;
	}

	/**
	 * @return the scheduledStartDate
	 */
	public Date getScheduledStartDate() {
		return scheduledStartDate;
	}

	/**
	 * @param scheduledStartDate the scheduledStartDate to set
	 */
	public void setScheduledStartDate(Date scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	
	
	/**
	 * 
	 */
	public SolvedTask() {
		// TODO Auto-generated constructor stub
	}

}
