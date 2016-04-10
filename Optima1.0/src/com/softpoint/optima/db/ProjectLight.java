package com.softpoint.optima.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the project database table.
 * 
 */
@Entity
@Table(name = "project")
@NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p")
public class ProjectLight implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "project_id")
	private int projectId;

	@Column(name = "project_code")
	private String projectCode;

	@Column(name = "project_description")
	private String projectDescription;

	@Column(name = "project_name")
	private String projectName;

	// bi-directional many-to-one association to Portfolio
	@ManyToOne
	@JoinColumn(name = "portfolio_id")
	private PortfolioLight portfolio;

	@Temporal(TemporalType.DATE)
	@Column(name = "propused_start_date")
	private Date propusedStartDate;

	public ProjectLight() {
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getProjectCode() {
		return this.projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProjectDescription() {
		return this.projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public PortfolioLight getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(PortfolioLight portfolio) {
		this.portfolio = portfolio;
	}

	public Date getPropusedStartDate() {
		return propusedStartDate;
	}

	public void setPropusedStartDate(Date propusedStartDate) {
		this.propusedStartDate = propusedStartDate;
	}

}