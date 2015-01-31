package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.105-0400")
@StaticMetamodel(PaymentType.class)
public class PaymentType_ {
	public static volatile SingularAttribute<PaymentType, Integer> paymentTypeId;
	public static volatile SingularAttribute<PaymentType, String> paymentType;
	public static volatile ListAttribute<PaymentType, ProjectPayment> projectPayments;
}
