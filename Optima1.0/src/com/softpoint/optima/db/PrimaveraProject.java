package com.softpoint.optima.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 * The persistent class for the portfolio database table.
 * 
 */
@Entity
@Table(name="primavera_project")
public class PrimaveraProject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="primavera_project_id")
	private int premaveraProjectId;

	@Column(name="project_guid")
	private String projectGuid;

	@OneToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name="file_contnet")
	private String fileContent;


    public PrimaveraProject() {
    }


	public int getPremaveraProjectId() {
		return premaveraProjectId;
	}


	public void setPremaveraProjectId(int premaveraProjectId) {
		this.premaveraProjectId = premaveraProjectId;
	}


	public String getProjectGuid() {
		return projectGuid;
	}


	public void setProjectGuid(String projectGuid) {
		this.projectGuid = projectGuid;
	}


	public Project getProject() {
		return project;
	}


	public void setProject(Project project) {
		this.project = project;
	}


	public String getFileContent() {
		return fileContent;
	}


	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}