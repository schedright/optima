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
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="project_id")
	private int projectId;

	@Column(name="interest_rate")
	private BigDecimal interestRate;

	@Column(name="overhead_per_day")
	private BigDecimal overheadPerDay;

	@Column(name="project_address_postal_code")
	private String projectAddressPostalCode;

	@Column(name="project_address_street")
	private String projectAddressStreet;

	@Column(name="project_code")
	private String projectCode;

	@Column(name="project_description")
	private String projectDescription;

	@Column(name="project_name")
	private String projectName;

    @Temporal( TemporalType.DATE)
	@Column(name="proposed_finish_date")
	private Date proposedFinishDate;

    @Temporal( TemporalType.DATE)
	@Column(name="propused_start_date")
	private Date propusedStartDate;

	//bi-directional many-to-one association to DaysOff
	@OneToMany(mappedBy="project" , fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<DaysOff> daysOffs;

	//bi-directional many-to-one association to Client
    @ManyToOne
	@JoinColumn(name="client_id")
	private Client client;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="project_address_city")
	private LocationInfo city;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="project_address_province")
	private LocationInfo province;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="project_address_country")
	private LocationInfo country;

	//bi-directional many-to-one association to Portfolio
    @ManyToOne
	@JoinColumn(name="portfolio_id")
	private Portfolio portfolio;

	//bi-directional many-to-one association to WeekendDay
    @ManyToOne
	@JoinColumn(name="Weekend_days_id")
	private WeekendDay weekendDays;

	//bi-directional many-to-one association to ProjectPayment
	@OneToMany(mappedBy="project" , fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<ProjectPayment> projectPayments;

	//bi-directional many-to-one association to ProjectTask
	@OneToMany(mappedBy="project" , fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<ProjectTask> projectTasks;

    public Project() {
    }

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
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

	public String getProjectAddressPostalCode() {
		return this.projectAddressPostalCode;
	}

	public void setProjectAddressPostalCode(String projectAddressPostalCode) {
		this.projectAddressPostalCode = projectAddressPostalCode;
	}

	public String getProjectAddressStreet() {
		return this.projectAddressStreet;
	}

	public void setProjectAddressStreet(String projectAddressStreet) {
		this.projectAddressStreet = projectAddressStreet;
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

	public List<DaysOff> getDaysOffs() {
		return this.daysOffs;
	}

	public void setDaysOffs(List<DaysOff> daysOffs) {
		this.daysOffs = daysOffs;
	}
	
	public Client getClient() {
		return this.client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
	
	public LocationInfo getCity() {
		return this.city;
	}

	public void setCity(LocationInfo city) {
		this.city = city;
	}
	
	public LocationInfo getProvince() {
		return this.province;
	}

	public void setProvince(LocationInfo province) {
		this.province = province;
	}
	
	public LocationInfo getCountry() {
		return this.country;
	}

	public void setCountry(LocationInfo country) {
		this.country = country;
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
	
	public List<ProjectTask> getProjectTasks() {
		return this.projectTasks;
	}

	public void setProjectTasks(List<ProjectTask> projectTasks) {
		this.projectTasks = projectTasks;
	}
	
}