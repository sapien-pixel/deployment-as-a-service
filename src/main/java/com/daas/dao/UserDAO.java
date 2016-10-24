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
	 * get all projects for the user
	 * @param user_id
	 * 					User id
	 * @return List of all {@link Project} of the user
	 */
	public List<Project> getAllProjects(long user_id);

}
