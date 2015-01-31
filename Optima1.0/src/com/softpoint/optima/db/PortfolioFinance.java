package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;


/**
 * The persistent class for the portfolio_finance database table.
 * 
 */
@Entity
@Table(name="portfolio_finance")
public class PortfolioFinance implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="finance_id")
	private int financeId;

	@Column(name="finance_amount")
	private BigDecimal financeAmount;

    @Temporal( TemporalType.DATE)
	@Column(name="finance_untill_date")
	private Date financeUntillDate;

	//bi-directional many-to-one association to Portfolio
    @ManyToOne
	@JoinColumn(name="portfolio_id")
	private Portfolio portfolio;

    public PortfolioFinance() {
    }

	public int getFinanceId() {
		return this.financeId;
	}

	public void setFinanceId(int financeId) {
		this.financeId = financeId;
	}

	public BigDecimal getFinanceAmount() {
		return this.financeAmount;
	}

	public void setFinanceAmount(BigDecimal financeAmount) {
		this.financeAmount = financeAmount;
	}

	public Date getFinanceUntillDate() {
		return this.financeUntillDate;
	}

	public void setFinanceUntillDate(Date financeUntillDate) {
		this.financeUntillDate = financeUntillDate;
	}

	public Portfolio getPortfolio() {
		return this.portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
	
}