package com.softpoint.optima.util.solution;

public class DayDetails {
	Double finance;
	Double payments;
	Double periodCost;
	Double periodIncome;
	Double financeInterest;
	Double overhead;
	Double leftOver;
	Double retained;
	Double penalty;
	Double balance;

	public DayDetails(Double finance, Double payments, Double periodCost, Double periodIncome, Double financeInterest, Double overhead,
			Double leftOver, Double retained, Double penalty, Double balance) {
		super();
		this.finance = finance;
		this.payments = payments;
		this.periodCost = periodCost;
		this.financeInterest = financeInterest;
		this.overhead = overhead;
		this.leftOver = leftOver;
		this.retained = retained;
		this.penalty = penalty;
		this.balance = balance;
		this.periodIncome = periodIncome;
	}

	public DayDetails(DayDetails dayDetails) {
		super();
		this.finance = dayDetails.finance;
		this.payments = dayDetails.payments;
		this.periodCost = dayDetails.periodCost;
		this.financeInterest = dayDetails.financeInterest;
		this.overhead = dayDetails.overhead;
		this.leftOver = dayDetails.leftOver;
		this.retained = dayDetails.retained;
		this.penalty = dayDetails.penalty;
		this.balance = dayDetails.balance;
		this.periodIncome = dayDetails.periodIncome;
	}
	
	public DayDetails() {
		finance			= (double) 0 ;
		payments		= (double) 0 ;
		periodCost		= (double) 0 ;
		periodIncome	= (double) 0 ;
		financeInterest	= (double) 0 ;
		overhead		= (double) 0 ;
		leftOver		= (double) 0 ;
		retained		= (double) 0 ;
		penalty			= (double) 0 ;
		balance			= (double) 0 ;
	}

	public Double getFinance() {
		return finance;
	}

	public void setFinance(Double finance) {
		this.finance = finance;
	}

	public void addFinance(Double finance) {
		this.finance += finance;
	}
	
	
	public Double getPayments() {
		return payments;
	}

	public void setPayments(Double payments) {
		this.payments = payments;
	}

	public void addPayments(Double payments) {
		this.payments += payments;
	}

	public Double getPeriodCost() {
		return periodCost;
	}

	public void setPeriodCost(Double periodCost) {
		this.periodCost = periodCost;
	}

	public void addPeriodCost(Double periodCost) {
		this.periodCost += periodCost;
	}
	
	public Double getPeriodIncome() {
		return periodIncome;
	}

	public void setPeriodIncome(Double periodIncome) {
		this.periodIncome = periodIncome;
	}

	public void addPeriodIncome(Double periodIncome) {
		this.periodIncome += periodIncome;
	}
	
	public Double getFinanceInterest() {
		return financeInterest;
	}

	public void setFinanceInterest(Double financeInterest) {
		this.financeInterest = financeInterest;
	}

	public void addFinanceInterest(Double financeInterest) {
		this.financeInterest += financeInterest;
	}

	public Double getOverhead() {
		return overhead;
	}

	public void setOverhead(Double overhead) {
		this.overhead = overhead;
	}

	public void addOverhead(Double overhead) {
		this.overhead += overhead;
	}
	
	public Double getLeftOver() {
		return leftOver;
	}

	public void setLeftOver(Double leftOver) {
		this.leftOver = leftOver;
	}

	public void addLeftOver(Double leftOver) {
		this.leftOver += leftOver;
	}

	public Double getRetained() {
		return retained;
	}

	public void setRetained(Double retained) {
		this.retained = retained;
	}

	public void addRetained(Double retained) {
		this.retained += retained;
	}

	public Double getPenalty() {
		return penalty;
	}

	public void setPenalty(Double penalty) {
		this.penalty = penalty;
	}

	public void addPenalty(Double penalty) {
		this.penalty += penalty;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public void addBalance(Double balance) {
		this.balance += balance;
	}

	public void addMoreDetails(DayDetails dayDetails) {
		finance += dayDetails.finance;
		payments += dayDetails.payments;
		periodCost += dayDetails.periodCost;
		financeInterest += dayDetails.financeInterest;
		overhead += dayDetails.overhead;
		leftOver += dayDetails.leftOver;
		retained += dayDetails.retained;
		penalty += dayDetails.penalty;
		balance += dayDetails.balance;
		periodIncome += dayDetails.periodIncome;
	}

}
