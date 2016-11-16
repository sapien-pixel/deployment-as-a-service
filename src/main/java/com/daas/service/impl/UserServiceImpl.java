package com.daas.service.impl;

import java.util.List;

import com.daas.dao.UserDAO;
import com.daas.dao.impl.UserDAOImpl;
import com.daas.model.Project;
import com.daas.model.User;
import com.daas.service.UserService;
import com.daas.util.PasswordHash;

/**
 * {@link User} Service Implementation
 * @author Vivek
 */
public class UserServiceImpl implements UserService {

	private static UserDAO userDAO = new UserDAOImpl();


	@Override
	public User create(User user) {

		String hashed_password = PasswordHash.hashPassword(user.getPassword());
		user.setPassword(hashed_password);
		return userDAO.create(user);
	}

	@Override
	public User read(long id) {

		return userDAO.read(id);
	}

	@Override
	public User update(User user) {

		if(!userExists(user.getUser_id())){
			return null;
		}

		return userDAO.update(user);
	}

	@Override
	public User delete(User user) {

		if(!userExists(user.getUser_id())){
			return null;
		}

		return userDAO.delete(user);
	}

	@Override
	public boolean checkEmailExists(String email) {

		User valid_user = userDAO.getUserByEmail(email);

		if(valid_user==null){
			return true;
		}
		return false;
	}

	@Override
	public boolean checkOrganizationExists(String orgName) {

		User valid_user = userDAO.getUserByOrganization(orgName);

		if(valid_user==null){
			return true;
		}
		return false;
	}

	@Override
	public User validateUser(User user) {

		User valid_user = userDAO.read(user.getUser_id());

		// log
		System.out.println(valid_user.getPassword());

		if(valid_user!=null){
			if(!(PasswordHash.checkPassword(user.getPassword(),valid_user.getPassword())))
				return null;
		}
		return valid_user;
	}

	@Override
	public boolean userExists(long id) {

		User valid_user = userDAO.read(id);

		if(valid_user==null){
			return false;
		}
		return true;
	}

	@Override
	public List<Project> getAllProjects(long user_id) {

		if(!userExists(user_id)){
			return null;
		}

		return userDAO.getAllProjects(user_id);
	}

}
