package com.daas.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Project Object
 * @author Vivek
 */
@Entity
@Table(name="project")
public class Project {

	@Id
	@Column(name="project_id")
	private String project_id;

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

	@Transient
	private String cloud_access_key;

	@Transient
	private String cloud_secret_key;

	@Transient
	private String iam_admin_role;

	@Transient
	private String cloud_account_id;

	@Transient
	private String master_size;

	@Transient
	private String node_size;

	@Transient
	private String node_numbers;


	/**
	 * @return the project_id
	 */
	public String getProject_id() {
		return project_id;
	}

	/**
	 * @param project_id the project_id to set
	 */
	public void setProject_id(String project_id) {
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

	/**
	 * @return the cloud_access_key
	 */
	public String getCloud_access_key() {
		return cloud_access_key;
	}

	/**
	 * @param cloud_access_key the cloud_access_key to set
	 */
	public void setCloud_access_key(String cloud_access_key) {
		this.cloud_access_key = cloud_access_key;
	}

	/**
	 * @return the cloud_secret_key
	 */
	public String getCloud_secret_key() {
		return cloud_secret_key;
	}

	/**
	 * @param cloud_secret_key the cloud_secret_key to set
	 */
	public void setCloud_secret_key(String cloud_secret_key) {
		this.cloud_secret_key = cloud_secret_key;
	}

	/**
	 * @return the iam_admin_role
	 */
	public String getIam_admin_role() {
		return iam_admin_role;
	}

	/**
	 * @param iam_admin_role the iam_admin_role to set
	 */
	public void setIam_admin_role(String iam_admin_role) {
		this.iam_admin_role = iam_admin_role;
	}

	/**
	 * @return the cloud_account_id
	 */
	public String getCloud_account_id() {
		return cloud_account_id;
	}

	/**
	 * @param cloud_account_id the cloud_account_id to set
	 */
	public void setCloud_account_id(String cloud_account_id) {
		this.cloud_account_id = cloud_account_id;
	}

	/**
	 * @return the master_size
	 */
	public String getMaster_size() {
		return master_size;
	}

	/**
	 * @param master_size the master_size to set
	 */
	public void setMaster_size(String master_size) {
		this.master_size = master_size;
	}

	/**
	 * @return the node_size
	 */
	public String getNode_size() {
		return node_size;
	}

	/**
	 * @param node_size the node_size to set
	 */
	public void setNode_size(String node_size) {
		this.node_size = node_size;
	}

	/**
	 * @return the node_numbers
	 */
	public String getNode_numbers() {
		return node_numbers;
	}

	/**
	 * @param node_numbers the node_numbers to set
	 */
	public void setNode_numbers(String node_numbers) {
		this.node_numbers = node_numbers;
	}


}
