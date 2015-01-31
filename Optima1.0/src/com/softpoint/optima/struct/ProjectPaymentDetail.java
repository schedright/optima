package com.softpoint.optima.struct;

import java.util.Date;

public class ProjectPaymentDetail {
	private String project;
	private int projectId;
	private double payment;
	private double repayment;
	private Date paymentStart;
	private Date paymentEnd;
	private double retained;
	private double extra;
	// {"project": "proj-1", "payment": 1980, "repayment": "100", "retained": "5", "extra": "50", "netPayment": 1831, "projectId": "6"}
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public double getPayment() {
		return payment;
	}
	public void setPayment(double payment) {
		this.payment = payment;
	}
	public double getRepayment() {
		return repayment;
	}
	public void setRepayment(double repayment) {
		this.repayment = repayment;
	}
	public Date getPaymentStart() {
		return paymentStart;
	}
	public void setPaymentStart(Date paymentStart) {
		this.paymentStart = paymentStart;
	}
	public Date getPaymentEnd() {
		return paymentEnd;
	}
	public void setPaymentEnd(Date paymentEnd) {
		this.paymentEnd = paymentEnd;
	}
	public double getRetained() {
		return retained;
	}
	public void setRetained(double retained) {
		this.retained = retained;
	}
	public double getExtra() {
		return extra;
	}
	public void setExtra(double extra) {
		this.extra = extra;
	}
	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}
	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}		
	
}