package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.212-0400")
@StaticMetamodel(TaskDependency.class)
public class TaskDependency_ {
	public static volatile SingularAttribute<TaskDependency, Integer> dependencyId;
	public static volatile SingularAttribute<TaskDependency, ProjectTask> dependency;
	public static volatile SingularAttribute<TaskDependency, ProjectTask> dependent;
}
