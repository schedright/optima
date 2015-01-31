package com.softpoint.optima.db;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-24T16:20:29.968-0400")
@StaticMetamodel(ProjectTask.class)
public class ProjectTask_ {
	public static volatile SingularAttribute<ProjectTask, Integer> taskId;
	public static volatile SingularAttribute<ProjectTask, Date> actualStartDate;
	public static volatile SingularAttribute<ProjectTask, Integer> duration;
	public static volatile SingularAttribute<ProjectTask, Date> scheduledStartDate;
	public static volatile SingularAttribute<ProjectTask, String> taskDescription;
	public static volatile SingularAttribute<ProjectTask, Date> tentativeStartDate;
	public static volatile SingularAttribute<ProjectTask, Date> calendarStartDate;
	public static volatile SingularAttribute<ProjectTask, BigDecimal> uniformDailyCost;
	public static volatile SingularAttribute<ProjectTask, BigDecimal> uniformDailyIncome;
	public static volatile SingularAttribute<ProjectTask, Project> project;
	public static volatile ListAttribute<ProjectTask, TaskDependency> asDependency;
	public static volatile ListAttribute<ProjectTask, TaskDependency> asDependent;
	public static volatile SingularAttribute<ProjectTask, String> taskName;
}
