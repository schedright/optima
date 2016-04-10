package com.softpoint.optima.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


/**
 * The persistent class for the user_role database table.
 * create table user_role (
  role_id INT NOT null AUTO_INCREMENT primary key, 
  user_id         int not null,
  role_name         varchar(15) not null,
  FOREIGN KEY (user_id) REFERENCES user(user_id)
);

 */
@Entity
@Table(name="user_role")
public class UserRole implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="role_id")
	private String roleId;

	@Column(name="user_name")
	private String userName;

	
	@Column(name="role_name")
	private String roleName;


	@ManyToOne
	@PrimaryKeyJoinColumn (name = "user_name",referencedColumnName = "user_name")
	private User user;


	public String getRoleId() {
		return roleId;
	}


	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}





	public String getRoleName() {
		return roleName;
	}


	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}

/*	@Override
	public String toString() {
		return "UserRole [roleId=" + roleId + ", userName=" + userName + ", roleName=" + roleName + ", user=" + user + "]";
	}
*/

	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}