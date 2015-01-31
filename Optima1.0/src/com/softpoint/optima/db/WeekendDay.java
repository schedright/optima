package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the weekend_days database table.
 * 
 */
@Entity
@Table(name="weekend_days")
public class WeekendDay implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="weekend_days_id")
	private int weekendDaysId;

	@Column(name="weekend_days")
	private String weekendDays;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="weekendDays")
	private List<Project> projects;

    public WeekendDay() {
    }

	public int getWeekendDaysId() {
		return this.weekendDaysId;
	}

	public void setWeekendDaysId(int weekendDaysId) {
		this.weekendDaysId = weekendDaysId;
	}

	public String getWeekendDays() {
		return this.weekendDays;
	}

	public void setWeekendDays(String weekendDays) {
		this.weekendDays = weekendDays;
	}

	public List<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
	
}