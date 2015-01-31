package com.softpoint.optima.db;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2013-10-10T09:58:21.775-0400")
@StaticMetamodel(LocationInfo.class)
public class LocationInfo_ {
	public static volatile SingularAttribute<LocationInfo, Integer> locationId;
	public static volatile SingularAttribute<LocationInfo, String> locationName;
	public static volatile SingularAttribute<LocationInfo, String> locationType;
	public static volatile ListAttribute<LocationInfo, Client> countryClients;
	public static volatile ListAttribute<LocationInfo, Client> cityClients;
	public static volatile ListAttribute<LocationInfo, Client> provinceClients;
	public static volatile SingularAttribute<LocationInfo, LocationInfo> parentLocation;
	public static volatile ListAttribute<LocationInfo, LocationInfo> childrenLocations;
	public static volatile ListAttribute<LocationInfo, Project> cityProjects;
	public static volatile ListAttribute<LocationInfo, Project> provinceProjects;
	public static volatile ListAttribute<LocationInfo, Project> countryProjects;
}
