package com.daas.service;

import java.util.List;

import com.daas.model.Project;
import com.daas.model.User;
/**
 * {@link User} Service
 * @author Vivek
 */
public interface UserService {

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
	 * check if a User with given email exists
	 * @param email	
	 * 					Email Id
	 * @return true if not exists, false otherwise.
	 */
	public boolean checkEmailExists(String email);
	
	/**
	 * check if a User with given organization name exists
	 * @param orgName	
	 * 					Name of organization
	 * @return true if not exists, false otherwise.
	 */
	public boolean checkOrganizationExists(String orgName);
	
	/**
	 * validate a User
	 * @param User
	 * 				User object
	 * @return {@link User}
	 */
	public User validateUser(User user);
	
	/**
	 * check if user exists
	 * @param id
	 * 				User id
	 * @return true, if exists
	 * false, if not
	 */
	public boolean userExists(long id);
	
	/**
	 * get all projects for the user
	 * @param user_id
	 * 					User id
	 * @return List of all {@link Project} of the user
	 */
	public List<Project> getAllProjects(long user_id);
	
}
