package com.softpoint.optima.db;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-12T11:40:08.359-0400")
@StaticMetamodel(Project.class)
public class Project_ {
	public static volatile SingularAttribute<Project, Integer> projectId;
	public static volatile SingularAttribute<Project, BigDecimal> dailyInterestRate;
	public static volatile SingularAttribute<Project, BigDecimal> overheadPerDay;
	public static volatile SingularAttribute<Project, String> projectAddressPostalCode;
	public static volatile SingularAttribute<Project, String> projectAddressStreet;
	public static volatile SingularAttribute<Project, String> projectCode;
	public static volatile SingularAttribute<Project, String> projectDescription;
	public static volatile SingularAttribute<Project, String> projectName;
	public static volatile SingularAttribute<Project, Date> proposedFinishDate;
	public static volatile SingularAttribute<Project, Date> propusedStartDate;
	public static volatile ListAttribute<Project, DaysOff> daysOffs;
	public static volatile SingularAttribute<Project, Client> client;
	public static volatile SingularAttribute<Project, LocationInfo> city;
	public static volatile SingularAttribute<Project, LocationInfo> province;
	public static volatile SingularAttribute<Project, LocationInfo> country;
	public static volatile SingularAttribute<Project, Portfolio> portfolio;
	public static volatile SingularAttribute<Project, WeekendDay> weekendDays;
	public static volatile ListAttribute<Project, ProjectPayment> projectPayments;
	public static volatile ListAttribute<Project, ProjectTask> projectTasks;
}
