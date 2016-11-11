package com.daas.dao;

import com.daas.model.Project;

/**
 * {@link Project} Data Access Object
 * @author Vivek
 */
public interface ProjectDAO {

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
	
}
