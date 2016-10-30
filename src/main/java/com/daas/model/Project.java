package com.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Project Object
 * @author Vivek
 */
@Entity
@Table(name="project")
public class Project {

	@Id
	@Column(name="project_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long project_id;

	@Column(name="projectName")
	private String projectName;

	@Column(name="description")
	private String description;

	@ManyToOne
	@JoinColumn(name="user_id")
	private User user_id;

	@Column(name="cloudProvider")
	private String cloudProvider;

	@Column(name="dateCreated")
	private long dateCreated;

	@Column(name="project_url")
	private String project_url;

	/**
	 * @return the project_id
	 */
	public long getProject_id() {
		return project_id;
	}

	/**
	 * @param project_id the project_id to set
	 */
	public void setProject_id(long project_id) {
		this.project_id = project_id;
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the user_id
	 */
	public User getUser_id() {
		return user_id;
	}

	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(User user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the cloudProvider
	 */
	public String getCloudProvider() {
		return cloudProvider;
	}

	/**
	 * @param cloudProvider the cloudProvider to set
	 */
	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}

	/**
	 * @return the dateCreated
	 */
	public long getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the project_url
	 */
	public String getProject_url() {
		return project_url;
	}

	/**
	 * @param project_url the project_url to set
	 */
	public void setProject_url(String project_url) {
		this.project_url = project_url;
	}

}
