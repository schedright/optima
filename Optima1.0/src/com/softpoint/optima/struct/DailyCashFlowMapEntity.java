package com.softpoint.optima.struct;

import java.util.Date;

public class DailyCashFlowMapEntity {
	int portfolioId;
	int projectId;
	Date day;
	double cashout;
	double financeCost;
	double balance;
	double payments;
	double netBalance;
	
	public int getPortfolioId() {
		return portfolioId;
	}
	public int getProjectId() {
		return projectId;
	}
	public Date getDay() {
		return day;
	}
	public double getCashout() {
		return cashout;
	}
	public double getFinanceCost() {
		return financeCost;
	}
	public double getBalance() {
		return balance;
	}
	public double getPayments() {
		return payments;
	}
	public double getNetBalance() {
		return netBalance;
	}
	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public void setDay(Date day) {
		this.day = day;
	}
	public void setCashout(double cashout) {
		this.cashout = cashout;
	}
	public void setFinanceCost(double financeCost) {
		this.financeCost = financeCost;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public void setPayments(double payments) {
		this.payments = payments;
	}
	public void setNetBalance(double netBalance) {
		this.netBalance = netBalance;
	}
}
