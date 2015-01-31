package com.softpoint.optima.struct;

import java.util.Date;


public class Period {


	
	/**
	 * 
	 */
	public Period() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param dateFrom
	 * @param dateTo
	 */
	public Period(Date dateFrom, Date dateTo) {
		super();
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}
	private Date dateFrom = null;
	private Date dateTo = null;
	
	public Date getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}
	public Date getDateTo() {
		return dateTo;
	}
	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

}