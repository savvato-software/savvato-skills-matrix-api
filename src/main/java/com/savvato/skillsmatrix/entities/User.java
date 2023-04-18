package com.savvato.skillsmatrix.entities;

import java.util.Calendar;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class User {

	private static final long serialVersionUID = 13532121L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	///
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	///
	private String password;
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	private String phone;
	private String email;
	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	//////
	private Integer enabled;

	public Integer getEnabled() {
		return enabled;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}
	
	/////
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name="user_user_role_map"
		, joinColumns={
			@JoinColumn(name="userId")
		}
		, inverseJoinColumns={
			@JoinColumn(name="userRoleId")
	})
	private Set<UserRole> roles;

	public Set<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRole> set) {
		this.roles = set;
	}

	///
	private java.sql.Timestamp created;

	public java.sql.Timestamp getCreated() {
		return created;
	}

	public void setCreated() {
		this.created = java.sql.Timestamp.from(Calendar.getInstance().toInstant());
	}

	public void setCreated(java.sql.Timestamp ts) {
		this.created = ts;
	}

	///
	private java.sql.Timestamp lastUpdated;

	public java.sql.Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated() {
		this.lastUpdated = java.sql.Timestamp.from(Calendar.getInstance().toInstant());
	}

	public void setLastUpdated(java.sql.Timestamp ts) {
		this.lastUpdated = ts;
	}

	/////
	public User(String name, String password, String phone, String email, Integer enabled) {
		this.name = name;
		this.password = password;
		
		this.phone = phone;
		this.email = email;
		this.enabled = enabled;

		this.setCreated();
		this.setLastUpdated();
	}
	
	public User(String name, String password, String phone, String email) {
		this.name = name;
		this.password = password;
		
		this.phone = phone;
		this.email = email;
		this.enabled = 1;

		this.setCreated();
		this.setLastUpdated();
	}
	
	public User() {
		
	}
}
