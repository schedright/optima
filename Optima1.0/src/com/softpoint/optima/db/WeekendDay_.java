package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.232-0400")
@StaticMetamodel(WeekendDay.class)
public class WeekendDay_ {
	public static volatile SingularAttribute<WeekendDay, Integer> weekendDaysId;
	public static volatile SingularAttribute<WeekendDay, String> weekendDays;
	public static volatile ListAttribute<WeekendDay, Project> projects;
}
