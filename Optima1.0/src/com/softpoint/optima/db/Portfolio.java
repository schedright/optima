package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the portfolio database table.
 * 
 */
@Entity
@Table(name="portfolio")
@NamedQuery(name="Portfolio.findAll", query="SELECT p FROM Portfolio p")
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

	//bi-directional many-to-one association to PortfolioExtrapayment
	@OneToMany(mappedBy="portfolio")
	private List<PortfolioExtrapayment> portfolioExtrapayments;

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

	public List<PortfolioExtrapayment> getPortfolioExtrapayments() {
		return this.portfolioExtrapayments;
	}

	public void setPortfolioExtrapayments(List<PortfolioExtrapayment> portfolioExtrapayments) {
		this.portfolioExtrapayments = portfolioExtrapayments;
	}

	public PortfolioExtrapayment addPortfolioExtrapayment(PortfolioExtrapayment portfolioExtrapayment) {
		getPortfolioExtrapayments().add(portfolioExtrapayment);
		portfolioExtrapayment.setPortfolio(this);

		return portfolioExtrapayment;
	}

	public PortfolioExtrapayment removePortfolioExtrapayment(PortfolioExtrapayment portfolioExtrapayment) {
		getPortfolioExtrapayments().remove(portfolioExtrapayment);
		portfolioExtrapayment.setPortfolio(null);

		return portfolioExtrapayment;
	}

	public List<PortfolioFinance> getPortfolioFinances() {
		return this.portfolioFinances;
	}

	public void setPortfolioFinances(List<PortfolioFinance> portfolioFinances) {
		this.portfolioFinances = portfolioFinances;
	}

	public PortfolioFinance addPortfolioFinance(PortfolioFinance portfolioFinance) {
		getPortfolioFinances().add(portfolioFinance);
		portfolioFinance.setPortfolio(this);

		return portfolioFinance;
	}

	public PortfolioFinance removePortfolioFinance(PortfolioFinance portfolioFinance) {
		getPortfolioFinances().remove(portfolioFinance);
		portfolioFinance.setPortfolio(null);

		return portfolioFinance;
	}

	public List<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public Project addProject(Project project) {
		getProjects().add(project);
		project.setPortfolio(this);

		return project;
	}

	public Project removeProject(Project project) {
		getProjects().remove(project);
		project.setPortfolio(null);

		return project;
	}

}