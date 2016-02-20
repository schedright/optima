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
public class PortfolioLight implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="portfolio_id")
	private int portfolioId;

	@Column(name="portfolio_descreption")
	private String portfolioDescreption;

	@Column(name="portfolio_name")
	private String portfolioName;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="portfolio", fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<ProjectLight> projects;

    public PortfolioLight() {
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

	public List<ProjectLight> getProjects() {
		return this.projects;
	}

	public void setProjects(List<ProjectLight> projects) {
		this.projects = projects;
	}

}