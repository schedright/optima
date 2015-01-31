package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the days_off database table.
 * 
 */
@Entity
@Table(name="days_off")
public class DaysOff implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="dayoff_id")
	private int dayoffId;

    @Temporal( TemporalType.DATE)
	@Column(name="day_off")
	private Date dayOff;

	@Column(name="dayoff_type")
	private String dayoffType;

	//bi-directional many-to-one association to Project
    @ManyToOne
	@JoinColumn(name="project_id")
	private Project project;

    public DaysOff() {
    }

	public int getDayoffId() {
		return this.dayoffId;
	}

	public void setDayoffId(int dayoffId) {
		this.dayoffId = dayoffId;
	}

	public Date getDayOff() {
		return this.dayOff;
	}

	public void setDayOff(Date dayOff) {
		this.dayOff = dayOff;
	}

	public String getDayoffType() {
		return this.dayoffType;
	}

	public void setDayoffType(String dayoffType) {
		this.dayoffType = dayoffType;
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
}