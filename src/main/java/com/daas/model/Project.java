package com.daas.model;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;

import java.util.List;

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
	@Column(name="project_id",unique=true,columnDefinition="VARCHAR(64)")
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

	@Column(name="project_username")
	private String project_username;

	@Column(name="project_password")
	private String project_password;

	@Column(name="app_url")
	private String app_url;

	@Transient
	private String old_clusterURL;

	@Transient
	private String clusterMasterUsername;

	@Transient
	private String clusterMasterPassword;

	@Transient
	private String cloud_access_key;

	@Transient
	private String cloud_secret_key;

	@Transient
	private String Aws_key;
	
	@Column(name="masterSize")
	private String master_size;
	
	@Column(name="volumeSize")
	private String volume_size;
	
	@Transient
	private String volume_id;

	@Column(name="nodeSize")
	private String node_size;
	
	@Column(name="nodeNumbers")
	private String node_numbers;

	@Transient
	private List<Service> services;

	@Transient
	private List<Deployment> deployments;	


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
	 * @return the project_username
	 */
	public String getProject_username() {
		return project_username;
	}

	/**
	 * @param project_username the project_username to set
	 */
	public void setProject_username(String project_username) {
		this.project_username = project_username;
	}

	/**
	 * @return the project_password
	 */
	public String getProject_password() {
		return project_password;
	}

	/**
	 * @param project_password the project_password to set
	 */
	public void setProject_password(String project_password) {
		this.project_password = project_password;
	}

	/**
	 * @return the app_url
	 */
	public String getApp_url() {
		return app_url;
	}

	/**
	 * @param app_url the app_url to set
	 */
	public void setApp_url(String app_url) {
		this.app_url = app_url;
	}

	/**
	 * @return the old_clusterURL
	 */
	public String getOld_clusterURL() {
		return old_clusterURL;
	}

	/**
	 * @param old_clusterURL the old_clusterURL to set
	 */
	public void setOld_clusterURL(String old_clusterURL) {
		this.old_clusterURL = old_clusterURL;
	}

	/**
	 * @return the clusterMasterUsername
	 */
	public String getClusterMasterUsername() {
		return clusterMasterUsername;
	}

	/**
	 * @param clusterMasterUsername the clusterMasterUsername to set
	 */
	public void setClusterMasterUsername(String clusterMasterUsername) {
		this.clusterMasterUsername = clusterMasterUsername;
	}

	/**
	 * @return the clusterMasterPassword
	 */
	public String getClusterMasterPassword() {
		return clusterMasterPassword;
	}

	/**
	 * @param clusterMasterPassword the clusterMasterPassword to set
	 */
	public void setClusterMasterPassword(String clusterMasterPassword) {
		this.clusterMasterPassword = clusterMasterPassword;
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
	 * @return the aws_key
	 */
	public String getAws_key() {
		return Aws_key;
	}

	/**
	 * @param aws_key the aws_key to set
	 */
	public void setAws_key(String aws_key) {
		Aws_key = aws_key;
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

	public String getVolume_size() {
		return volume_size;
	}

	public void setVolume_size(String volume_size) {
		this.volume_size = volume_size;
	}

	public String getVolume_id() {
		return volume_id;
	}

	public void setVolume_id(String volume_id) {
		this.volume_id = volume_id;
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

	/**
	 * @return the services
	 */
	public List<Service> getServices() {
		return services;
	}

	/**
	 * @param services the services to set
	 */
	public void setServices(List<Service> services) {
		this.services = services;
	}

	/**
	 * @return the deployments
	 */
	public List<Deployment> getDeployments() {
		return deployments;
	}

	/**
	 * @param deployments the deployments to set
	 */
	public void setDeployments(List<Deployment> deployments) {
		this.deployments = deployments;
	}

}
