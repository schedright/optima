package com.softpoint.optima.db;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-10T09:58:21.740-0400")
@StaticMetamodel(DaysOff.class)
public class DaysOff_ {
	public static volatile SingularAttribute<DaysOff, Integer> dayoffId;
	public static volatile SingularAttribute<DaysOff, Date> dayOff;
	public static volatile SingularAttribute<DaysOff, String> dayoffType;
	public static volatile SingularAttribute<DaysOff, Project> project;
}
