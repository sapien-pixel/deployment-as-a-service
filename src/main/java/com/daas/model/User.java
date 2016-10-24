package com.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * User Object
 * @author Vivek
 */
@Entity
@Table(name="userprofiles", uniqueConstraints = { @UniqueConstraint(columnNames = {
"email"}) })
public class User {

	@Id
	@Column(name="user_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long user_id;

	@Column(name="firstname")
	private String firstName;
	
	@Column(name="lastname")
	private String lastName;

	@Column(name="email",nullable=false)
	private String email;

	@Column(name="password")
	private String password;
	
	@Column(name="organization")
	private String organization;
	
	@Column(name="dateRegistered")
	private long dateRegistered;

	/**
	 * @return the user_id
	 */
	public long getUser_id() {
		return user_id;
	}

	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(long user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the dateRegistered
	 */
	public long getDateRegistered() {
		return dateRegistered;
	}

	/**
	 * @param dateRegistered the dateRegistered to set
	 */
	public void setDateRegistered(long dateRegistered) {
		this.dateRegistered = dateRegistered;
	}

}
