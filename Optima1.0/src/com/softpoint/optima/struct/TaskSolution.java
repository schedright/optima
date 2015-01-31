/**
 * 
 */
package com.softpoint.optima.struct;

import java.util.Date;

import com.softpoint.optima.db.ProjectTask;

/**
 * @author WDARWISH
 *
 */
public class TaskSolution {
	
	
	private ProjectTask task;
	private Date startDate;
	private double income;
	private int projectLength;
	private double currentPeriodCost;
	private double leftOversCost;
	

	/**
	 * 
	 */
	public TaskSolution() {
	
	}


	

	

	
	/**
	 * @param taskId
	 * @param scheduledStartDate
	 */
	public TaskSolution(ProjectTask task) {
		super();
		this.setTask(task);
	}


	/**
	 * @return the projectLength
	 */
	public int getProjectLength() {
		return projectLength;
	}


	/**
	 * @param projectLength the projectLength to set
	 */
	public void setProjectLength(int projectLength) {
		this.projectLength = projectLength;
	}


	/**
	 * @return the task
	 */
	public ProjectTask getTask() {
		return task;
	}







	/**
	 * @param task the task to set
	 */
	public void setTask(ProjectTask task) {
		this.task = task;
	}







	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}







	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}







	/**
	 * @return the income
	 */
	public double getIncome() {
		return income;
	}







	/**
	 * @param income the income to set
	 */
	public void setIncome(double income) {
		this.income = income;
	}







	public double getCurrentPeriodCost() {
		return currentPeriodCost;
	}







	public void setCurrentPeriodCost(double currentPeriodCost) {
		this.currentPeriodCost = currentPeriodCost;
	}







	public double getLeftOversCost() {
		return leftOversCost;
	}







	public void setLeftOversCost(double leftOversCost) {
		this.leftOversCost = leftOversCost;
	}



	




		

}
