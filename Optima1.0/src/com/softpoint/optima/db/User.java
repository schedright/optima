package com.softpoint.optima.db;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


/**
 * The persistent class for the user database table.
 * 
 * create table user (
 user_id INT NOT null AUTO_INCREMENT primary key, 
  user_name         varchar(15) not null,
  user_pass         varchar(15) not null
);

 */
@Entity
@Table(name="user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="user_id")
	private String userId;

	@Column(name="user_name")
	private String userName;

	
	@Column(name="user_pass")
	private String userPass;

	//bi-directional many-to-one association to Project
	@OneToMany(mappedBy="user", fetch=FetchType.EAGER, cascade = CascadeType.REFRESH)
	private List<UserRole> userRoles;


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getUserPass() {
		return userPass;
	}


	public void setUserPass(String userPass) {
		this.userPass = userPass;
	}


	public List<UserRole> getUserRoles() {
		return userRoles;
	}


	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}


/*	@Override
	public String toString() {
		return "Users [userName=" + userName + ", userPass=" + userPass + ", userRoles=" + userRoles + "]";
	}
*/

	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}



	
}