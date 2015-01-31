package com.softpoint.optima.db;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;


/**
 * The persistent class for the portfolio database table.
 * 
 */
@Entity
public class Portfolio implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="portfolio_id")
	private int portfolioId;

	@Column(name="portfolio_descreption")
	private String portfolioDescreption;

	@Column(name="portfolio_name")
	private String portfolioName;

	//bi-directional many-to-one association to PortfolioFinance
	@OneToMany(mappedBy="portfolio" , fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<PortfolioFinance> portfolioFinances;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="portfolio", fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<Project> projects;

    public Portfolio() {
    }

	public int getPortfolioId() {
		return this.portfolioId;
	}

	public void setPortfolioId(int portfolioId) {
		this.portfolioId = portfolioId;
	}

	public String getPortfolioDescreption() {
		return this.portfolioDescreption;
	}

	public void setPortfolioDescreption(String portfolioDescreption) {
		this.portfolioDescreption = portfolioDescreption;
	}

	public String getPortfolioName() {
		return this.portfolioName;
	}

	public void setPortfolioName(String portfolioName) {
		this.portfolioName = portfolioName;
	}

	public List<PortfolioFinance> getPortfolioFinances() {
		return this.portfolioFinances;
	}

	public void setPortfolioFinances(List<PortfolioFinance> portfolioFinances) {
		this.portfolioFinances = portfolioFinances;
	}
	
	public List<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
	
}