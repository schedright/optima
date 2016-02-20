package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

}