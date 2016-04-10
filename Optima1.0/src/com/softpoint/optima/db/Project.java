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
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "project_id")
	private int projectId;

	@Column(name = "advanced_payment_percentage")
	private BigDecimal advancedPaymentPercentage;

	@Column(name = "advanced_payment_amount")
	private BigDecimal advancedPaymentAmount;

	@Column(name = "delay_penalty_amount")
	private BigDecimal delayPenaltyAmount;

	@Column(name = "collect_payment_period")
	private int collectPaymentPeriod;

	@Column(name = "payment_request_period")
	private int paymentRequestPeriod;

	@Column(name = "interest_rate")
	private BigDecimal interestRate;

	@Column(name = "overhead_per_day")
	private BigDecimal overheadPerDay;

	@Column(name = "project_code")
	private String projectCode;

	@Column(name = "project_description")
	private String projectDescription;

	@Column(name = "project_name")
	private String projectName;

	@Temporal(TemporalType.DATE)
	@Column(name = "proposed_finish_date")
	private Date proposedFinishDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "propused_start_date")
	private Date propusedStartDate;

	@Column(name = "retained_percentage")
	private BigDecimal retainedPercentage;

	// bi-directional many-to-one association to DaysOff
	@OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<DaysOff> daysOffs;

	// bi-directional many-to-one association to Portfolio
	@ManyToOne
	@JoinColumn(name = "portfolio_id")
	private Portfolio portfolio;

	// bi-directional many-to-one association to WeekendDay
	@ManyToOne
	@JoinColumn(name = "Weekend_days_id")
	private WeekendDay weekendDays;

	// bi-directional many-to-one association to ProjectPayment
	@OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<ProjectPayment> projectPayments;

	// bi-directional many-to-one association to ProjectTask
	@OneToMany(mappedBy = "project", fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
	@OrderBy("calendarStartDate ASC,tentativeStartDate ASC")
	private List<ProjectTask> projectTasks;

	public Project() {
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public BigDecimal getAdvancedPaymentPercentage() {
		return this.advancedPaymentPercentage;
	}

	public void setAdvancedPaymentPercentage(BigDecimal advancedPaymentPercentage) {
		this.advancedPaymentPercentage = advancedPaymentPercentage;
	}

	public BigDecimal getAdvancedPaymentAmount() {
		return advancedPaymentAmount;
	}

	public void setAdvancedPaymentAmount(BigDecimal advancedPaymentAmount) {
		this.advancedPaymentAmount = advancedPaymentAmount;
	}

	public BigDecimal getDelayPenaltyAmount() {
		return delayPenaltyAmount;
	}

	public void setDelayPenaltyAmount(BigDecimal delayPenaltyAmount) {
		this.delayPenaltyAmount = delayPenaltyAmount;
	}

	public int getCollectPaymentPeriod() {
		return collectPaymentPeriod;
	}

	public void setCollectPaymentPeriod(int collectPaymentPeriod) {
		this.collectPaymentPeriod = collectPaymentPeriod;
	}

	public int getPaymentRequestPeriod() {
		return paymentRequestPeriod;
	}

	public void setPaymentRequestPeriod(int paymentRequestPeriod) {
		this.paymentRequestPeriod = paymentRequestPeriod;
	}

	public BigDecimal getInterestRate() {
		return this.interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public BigDecimal getOverheadPerDay() {
		return this.overheadPerDay;
	}

	public void setOverheadPerDay(BigDecimal overheadPerDay) {
		this.overheadPerDay = overheadPerDay;
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

	public Date getProposedFinishDate() {
		return this.proposedFinishDate;
	}

	public void setProposedFinishDate(Date proposedFinishDate) {
		this.proposedFinishDate = proposedFinishDate;
	}

	public Date getPropusedStartDate() {
		return this.propusedStartDate;
	}

	public void setPropusedStartDate(Date propusedStartDate) {
		this.propusedStartDate = propusedStartDate;
	}

	public BigDecimal getRetainedPercentage() {
		return this.retainedPercentage;
	}

	public void setRetainedPercentage(BigDecimal retainedPercentage) {
		this.retainedPercentage = retainedPercentage;
	}

	public List<DaysOff> getDaysOffs() {
		return this.daysOffs;
	}

	public void setDaysOffs(List<DaysOff> daysOffs) {
		this.daysOffs = daysOffs;
	}

	public DaysOff addDaysOff(DaysOff daysOff) {
		getDaysOffs().add(daysOff);
		daysOff.setProject(this);

		return daysOff;
	}

	public DaysOff removeDaysOff(DaysOff daysOff) {
		getDaysOffs().remove(daysOff);
		daysOff.setProject(null);

		return daysOff;
	}

	public Portfolio getPortfolio() {
		return this.portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}

	public WeekendDay getWeekendDays() {
		return this.weekendDays;
	}

	public void setWeekendDays(WeekendDay weekendDays) {
		this.weekendDays = weekendDays;
	}

	public List<ProjectPayment> getProjectPayments() {
		return this.projectPayments;
	}

	public void setProjectPayments(List<ProjectPayment> projectPayments) {
		this.projectPayments = projectPayments;
	}

	public ProjectPayment addProjectPayment(ProjectPayment projectPayment) {
		getProjectPayments().add(projectPayment);
		projectPayment.setProject(this);

		return projectPayment;
	}

	public ProjectPayment removeProjectPayment(ProjectPayment projectPayment) {
		getProjectPayments().remove(projectPayment);
		projectPayment.setProject(null);

		return projectPayment;
	}

	public List<ProjectTask> getProjectTasks() {
		return this.projectTasks;
	}

	public void setProjectTasks(List<ProjectTask> projectTasks) {
		this.projectTasks = projectTasks;
	}

}