package com.softpoint.optima.db;

import java.math.BigDecimal;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.135-0400")
@StaticMetamodel(PortfolioFinance.class)
public class PortfolioFinance_ {
	public static volatile SingularAttribute<PortfolioFinance, Integer> financeId;
	public static volatile SingularAttribute<PortfolioFinance, BigDecimal> financeAmount;
	public static volatile SingularAttribute<PortfolioFinance, Date> financeUntillDate;
	public static volatile SingularAttribute<PortfolioFinance, Portfolio> portfolio;
}
