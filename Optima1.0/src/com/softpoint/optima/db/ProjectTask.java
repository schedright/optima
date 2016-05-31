package com.softpoint.optima.db;

import java.io.Serializable;

import javax.persistence.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the project_task database table.
 * 
 */
@Entity
@Table(name="project_task")
public class ProjectTask implements Serializable  {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="task_id")
	private int taskId;

    @Temporal( TemporalType.DATE)
	@Column(name="actual_start_date")
	private Date actualStartDate;
    
    @Column(name="duration")
	private int duration;
    
    @Column(name="calender_duration")
    private int calenderDuration;

    @Temporal( TemporalType.DATE)
	@Column(name="scheduled_start_date")
	private Date scheduledStartDate;

	@Column(name="task_name")
	private String taskName;

	@Column(name="guid")
	private String taskGuid;
	
	@Column(name="task_description")
	private String taskDescription;

    @Temporal( TemporalType.DATE)
	@Column(name="tentative_start_date")
	private Date tentativeStartDate;

    @Temporal( TemporalType.DATE)
	@Column(name="calendar_start_date")
	private Date calendarStartDate;
    
	/**
	 * @return the calendarStartDate
	 */
	public Date getCalendarStartDate() {
		return calendarStartDate;
	}

	/**
	 * @param calendarStartDate the calendarStartDate to set
	 */
	public void setCalendarStartDate(Date calendarStartDate) {
		this.calendarStartDate = calendarStartDate;
	}

	@Column(name="uniform_daily_cost")
	private BigDecimal uniformDailyCost;

	@Column(name="uniform_daily_income")
	private BigDecimal uniformDailyIncome;

	//bi-directional many-to-one association to Project
    @ManyToOne
	@JoinColumn(name="project_id")
	private Project project;

	//bi-directional many-to-one association to TaskDependency
	@OneToMany(mappedBy="dependency", fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<TaskDependency> asDependency;

	//bi-directional many-to-one association to TaskDependency
	@OneToMany(mappedBy="dependent" , fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<TaskDependency> asDependent;

    public ProjectTask() {
    }

	public int getTaskId() {
		return this.taskId;
	}

	/**
	 * @return the calenderDuration
	 */
	public int getCalenderDuration() {
		return calenderDuration;
	}

	/**
	 * @param calenderDuration the calenderDuration to set
	 */
	public void setCalenderDuration(int calenderDuration) {
		this.calenderDuration = calenderDuration;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public Date getActualStartDate() {
		return this.actualStartDate;
	}

	public void setActualStartDate(Date actualStartDate) {
		this.actualStartDate = actualStartDate;
	}

	public int getDuration() {
		return this.duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Date getScheduledStartDate() {
		return this.scheduledStartDate;
	}

	public void setScheduledStartDate(Date scheduledStartDate) {
		this.scheduledStartDate = scheduledStartDate;
	}

	public String getTaskDescription() {
		return this.taskDescription;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	
	public String getTaskName() {
		return this.taskName;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public Date getTentativeStartDate() {
		return this.tentativeStartDate;
	}
	
	public Date getEffectiveTentativeStartDate() {
		if (this.tentativeStartDate!=null) {
			return this.tentativeStartDate;
		} else {
			return getProject().getPropusedStartDate();
		}
	}
	

	public void setTentativeStartDate(Date tentativeStartDate) {
		this.tentativeStartDate = tentativeStartDate;
	}

	public BigDecimal getUniformDailyCost() {
		return this.uniformDailyCost;
	}

	public void setUniformDailyCost(BigDecimal uniformDailyCost) {
		this.uniformDailyCost = uniformDailyCost;
	}

	public BigDecimal getUniformDailyIncome() {
		return this.uniformDailyIncome;
	}

	public void setUniformDailyIncome(BigDecimal uniformDailyIncome) {
		this.uniformDailyIncome = uniformDailyIncome;
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public List<TaskDependency> getAsDependency() {
		return this.asDependency;
	}

	public void setAsDependency(List<TaskDependency> asDependency) {
		this.asDependency = asDependency;
	}
	
	public List<TaskDependency> getAsDependent() {
		return this.asDependent;
	}

	public void setAsDependent(List<TaskDependency> asDependent) {
		this.asDependent = asDependent;
	}
	
	@Override
	public boolean equals(Object obj) {
		ProjectTask task = (ProjectTask)obj;
		return task.getTaskId() == getTaskId();
	}

	public String getTaskGuid() {
		return taskGuid;
	}

	public void setTaskGuid(String taskGuid) {
		this.taskGuid = taskGuid;
	}
	
	
}