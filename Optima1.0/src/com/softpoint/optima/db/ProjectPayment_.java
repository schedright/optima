package com.softpoint.optima.db;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.172-0400")
@StaticMetamodel(ProjectPayment.class)
public class ProjectPayment_ {
	public static volatile SingularAttribute<ProjectPayment, Integer> paymentId;
	public static volatile SingularAttribute<ProjectPayment, BigDecimal> paymentAmount;
	public static volatile SingularAttribute<ProjectPayment, Date> paymentDate;
	public static volatile SingularAttribute<ProjectPayment, String> paymentInterimNumber;
	public static volatile SingularAttribute<ProjectPayment, PaymentType> paymentType;
	public static volatile SingularAttribute<ProjectPayment, Project> project;
}
