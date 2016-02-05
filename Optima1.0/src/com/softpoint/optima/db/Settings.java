package com.softpoint.optima.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the Settings database table.
 * 
 */
@Entity
@Table(name="settings")
@NamedQuery(name="Settings.findAll", query="SELECT p FROM Settings p")
public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="settings_id")
	private int settingsId;

	@Column(name="name")
	private String name;

	@Column(name="value")
	private String value;

    public Settings() {
    }

	public int getSettingsId() {
		return settingsId;
	}

	public void setSettingsId(int settingsId) {
		this.settingsId = settingsId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}