package com.daas.dao;

import java.util.List;

import com.daas.model.Project;
import com.daas.model.User;

/**
 * {@link User} Data Access Object
 * @author Vivek
 */
public interface UserDAO {

	/**
	 * create a new User
	 * @param User
	 * 				User object
	 * @return {@link User}
	 */
	public User create(User user);
	
	/**
	 * get a User
	 * @param id
	 * 				User id
	 * @return {@link User}
	 */
	public User read(long id);
	
	/**
	 * update a User
	 * @param User
	 * 				User object
	 * @return {@link User}
	 */
	public User update(User user);
	
	/**
	 * delete a User
	 * @param User
	 * 				User object
	 * @return {@link User}
	 */
	public User delete(User user);
	
	/**
	 * get a User by email
	 * @param email	
	 * 					Email Id
	 * @return {@link User} if exists, null if not
	 */
	public User getUserByEmail(String email);
	
	/**
	 * get a User by organization name
	 * @param orgName	
	 * 					Name of organization
	 * @return {@link User} if exists, null if not
	 */
	public User getUserByOrganization(String orgName);
	
	/**
	 * get all projects for the user
	 * @param user
	 * 					User
	 * @return List of all {@link Project} of the user
	 */
	public List<Project> getAllProjects(User user);

}
