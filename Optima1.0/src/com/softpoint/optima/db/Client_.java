package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-10T20:50:42.190-0400")
@StaticMetamodel(Client.class)
public class Client_ {
	public static volatile SingularAttribute<Client, Integer> clientId;
	public static volatile SingularAttribute<Client, String> clientAddressPostalCode;
	public static volatile SingularAttribute<Client, String> clientAddressStreet;
	public static volatile SingularAttribute<Client, String> clientName;
	public static volatile SingularAttribute<Client, LocationInfo> country;
	public static volatile SingularAttribute<Client, LocationInfo> city;
	public static volatile SingularAttribute<Client, LocationInfo> province;
	public static volatile ListAttribute<Client, Project> projects;
}
