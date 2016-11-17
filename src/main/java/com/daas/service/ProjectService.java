package com.daas.service;

import com.daas.model.Project;

/**
 * {@link Project} Service
 * @author Vivek
 */
public interface ProjectService {

	/**
	 * create a new Project
	 * @param Project
	 * 				Project object
	 * @return {@link Project}
	 */
	public Project create(Project project);
	
	/**
	 * get a Project
	 * @param id
	 * 				Project id
	 * @return {@link Project}
	 */
	public Project read(String id);
	
	/**
	 * update a Project
	 * @param Project
	 * 				Project object
	 * @return {@link Project}
	 */
	public Project update(Project project);
	
	/**
	 * delete a Project
	 * @param id
	 * 				Project id
	 * @return {@link Project}
	 */
	public Project delete(Project project);	
	
	/**
	 * check if Project exists
	 * @param id
	 * 				Project id
	 * @return true, if exists
	 * false, if not
	 */
	public boolean projectExists(String id);
	
	/**
	 * check if project_url for this project exists
	 * @param id
	 * 				Project id
	 * @return true, if exists
	 * false, if not
	 */
	public boolean projectURLExists(String id);
	
}
