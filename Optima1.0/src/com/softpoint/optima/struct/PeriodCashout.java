package com.softpoint.optima.struct;

public class PeriodCashout {

	public PeriodCashout(int projectId, String projectCode, double taskCost,
			double overhead, double cashout, double openingBalance , double projectPayment) {
		super();
		this.projectId = projectId;
		this.projectCode = projectCode;
		this.taskCost = taskCost;
		this.overhead = overhead;
		this.cashout = cashout;
		
		this.openingBalance = openingBalance;
		this.setProjectPayment(projectPayment);
	}
	
	
	private int projectId;
	private double openingBalance;
	private double projectPayment;
	
	
	/**
	 * @return the openingBalance
	 */
	public double getOpeningBalance() {
		return openingBalance;
	}
	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(double openingBalance) {
		this.openingBalance = openingBalance;
	}
	/**
	 * @return the projectId
	 */
	public int getProjectId() {
		return projectId;
	}
	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	/**
	 * @return the projectCode
	 */
	public String getProjectCode() {
		return projectCode;
	}
	/**
	 * @param projectCode the projectCode to set
	 */
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	/**
	 * @return the taskCost
	 */
	public double getTaskCost() {
		return taskCost;
	}
	/**
	 * @param taskCost the taskCost to set
	 */
	public void setTaskCost(double taskCost) {
		this.taskCost = taskCost;
	}
	/**
	 * @return the overhead
	 */
	public double getOverhead() {
		return overhead;
	}
	/**
	 * @param overhead the overhead to set
	 */
	public void setOverhead(double overhead) {
		this.overhead = overhead;
	}
	/**
	 * @return the cashout
	 */
	public double getCashout() {
		return cashout;
	}
	/**
	 * @param cashout the cashout to set
	 */
	public void setCashout(double cashout) {
		this.cashout = cashout;
	}



	/**
	 * @return the projectPayment
	 */
	public double getProjectPayment() {
		return projectPayment;
	}
	/**
	 * @param projectPayment the projectPayment to set
	 */
	public void setProjectPayment(double projectPayment) {
		this.projectPayment = projectPayment;
	}


	private String projectCode;
	private double taskCost;
	private double overhead;
	private double cashout;
	
	
}
