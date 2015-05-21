package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the portfolio_extrapayment database table.
 * 
 */
@Entity
@Table(name="portfolio_extrapayment")
@NamedQuery(name="PortfolioExtrapayment.findAll", query="SELECT p FROM PortfolioExtrapayment p")
public class PortfolioExtrapayment implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int extraPayment_id;

	private BigDecimal extraPayment_amount;

	@Temporal(TemporalType.DATE)
	private Date extraPayment_date;

	//bi-directional many-to-one association to Portfolio
	@ManyToOne
	@JoinColumn(name="portfolio_id")
	private Portfolio portfolio;

	public PortfolioExtrapayment() {
	}

	public int getExtraPayment_id() {
		return this.extraPayment_id;
	}

	public void setExtraPayment_id(int extraPayment_id) {
		this.extraPayment_id = extraPayment_id;
	}

	public BigDecimal getExtraPayment_amount() {
		return this.extraPayment_amount;
	}

	public void setExtraPayment_amount(BigDecimal extraPayment_amount) {
		this.extraPayment_amount = extraPayment_amount;
	}

	public Date getExtraPayment_date() {
		return this.extraPayment_date;
	}

	public void setExtraPayment_date(Date extraPayment_date) {
		this.extraPayment_date = extraPayment_date;
	}

	public Portfolio getPortfolio() {
		return this.portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

}