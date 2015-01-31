package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-07T21:55:34.120-0400")
@StaticMetamodel(Portfolio.class)
public class Portfolio_ {
	public static volatile SingularAttribute<Portfolio, Integer> portfolioId;
	public static volatile SingularAttribute<Portfolio, String> portfolioDescreption;
	public static volatile SingularAttribute<Portfolio, String> portfolioName;
	public static volatile ListAttribute<Portfolio, PortfolioFinance> portfolioFinances;
	public static volatile ListAttribute<Portfolio, Project> projects;
}
