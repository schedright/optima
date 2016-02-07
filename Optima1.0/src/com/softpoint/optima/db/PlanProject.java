package com.softpoint.optima.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the Settings database table.
 * 
 */
@Entity
@Table(name="capicatl_plan_projects")
@NamedQuery(name="PlanProject.findAll", query="SELECT p FROM PlanProject p")
public class PlanProject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="plan_id")
	private int planId;

	@Column(name="project_id")
	private int projectId;

    public PlanProject() {
    }

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}


}