package com.softpoint.optima.db;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;


/**
 * The persistent class for the client database table.
 * 
 */
@Entity
public class Client implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="client_id")
	private int clientId;

	@Column(name="client_address_postal_code")
	private String clientAddressPostalCode;

	@Column(name="client_address_street")
	private String clientAddressStreet;

	@Column(name="client_name")
	private String clientName;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="client_address_country")
	private LocationInfo country;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="client_address_city")
	private LocationInfo city;

	//bi-directional many-to-one association to LocationInfo
    @ManyToOne
	@JoinColumn(name="client_address_province")
	private LocationInfo province;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="client")
	private List<Project> projects;

    public Client() {
    }

	public int getClientId() {
		return this.clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getClientAddressPostalCode() {
		return this.clientAddressPostalCode;
	}

	public void setClientAddressPostalCode(String clientAddressPostalCode) {
		this.clientAddressPostalCode = clientAddressPostalCode;
	}

	public String getClientAddressStreet() {
		return this.clientAddressStreet;
	}

	public void setClientAddressStreet(String clientAddressStreet) {
		this.clientAddressStreet = clientAddressStreet;
	}

	public String getClientName() {
		return this.clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public LocationInfo getCountry() {
		return this.country;
	}

	public void setCountry(LocationInfo country) {
		this.country = country;
	}
	
	public LocationInfo getCity() {
		return this.city;
	}

	public void setCity(LocationInfo city) {
		this.city = city;
	}
	
	public LocationInfo getProvince() {
		return this.province;
	}

	public void setProvince(LocationInfo province) {
		this.province = province;
	}
	
	public List<Project> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}
	
}