package com.softpoint.optima.db;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * The persistent class for the location_info database table.
 * 
 */
@Entity
@Table(name="location_info")
public class LocationInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="location_id")
	private int locationId;

	@Column(name="location_name")
	private String locationName;

	@Column(name="location_type")
	private String locationType;

	//bi-directional many-to-one association to Client
	@OneToMany(mappedBy="country")
	private List<Client> countryClients;

	//bi-directional many-to-one association to Client
	@OneToMany(mappedBy="city")
	private List<Client> cityClients;

	//bi-directional many-to-one association to Client
	@OneToMany(mappedBy="province")
	private List<Client> provinceClients;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="parent_id")
	private LocationInfo parentLocation;

	//bi-directional many-to-one association to LocationInfo
	@OneToMany(mappedBy="parentLocation")
	private List<LocationInfo> childrenLocations;

/*	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="city")
	private List<Project> cityProjects;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="province")
	private List<Project> provinceProjects;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="country")
	private List<Project> countryProjects;
*/
    public LocationInfo() {
    }

	public int getLocationId() {
		return this.locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationType() {
		return this.locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public List<Client> getCountryClients() {
		return this.countryClients;
	}

	public void setCountryClients(List<Client> countryClients) {
		this.countryClients = countryClients;
	}
	
	public List<Client> getCityClients() {
		return this.cityClients;
	}

	public void setCityClients(List<Client> cityClients) {
		this.cityClients = cityClients;
	}
	
	public List<Client> getProvinceClients() {
		return this.provinceClients;
	}

	public void setProvinceClients(List<Client> provinceClients) {
		this.provinceClients = provinceClients;
	}
	
	public LocationInfo getParentLocation() {
		return this.parentLocation;
	}

	public void setParentLocation(LocationInfo parentLocation) {
		this.parentLocation = parentLocation;
	}
	
	public List<LocationInfo> getChildrenLocations() {
		return this.childrenLocations;
	}

	public void setChildrenLocations(List<LocationInfo> childrenLocations) {
		this.childrenLocations = childrenLocations;
	}
	
/*	public List<Project> getCityProjects() {
		return this.cityProjects;
	}

	public void setCityProjects(List<Project> cityProjects) {
		this.cityProjects = cityProjects;
	}
	
	public List<Project> getProvinceProjects() {
		return this.provinceProjects;
	}

	public void setProvinceProjects(List<Project> provinceProjects) {
		this.provinceProjects = provinceProjects;
	}
	
	public List<Project> getCountryProjects() {
		return this.countryProjects;
	}

	public void setCountryProjects(List<Project> countryProjects) {
		this.countryProjects = countryProjects;
	}
	*/
}