package com.daas.service.impl;

import com.daas.dao.ProjectDAO;
import com.daas.dao.impl.ProjectDAOImpl;
import com.daas.model.Project;
import com.daas.service.ProjectService;

/**
 * {@link Project} Service Implementation
 * @author Vivek
 */
public class ProjectServiceImpl implements ProjectService{

	private static ProjectDAO projectDAO = new ProjectDAOImpl();
	
	
	@Override
	public Project create(Project project) {

		return projectDAO.create(project);
	}

	@Override
	public Project read(String id) {

		return projectDAO.read(id);
	}

	@Override
	public Project update(Project project) {

		if(!projectExists(project.getProject_id())){
			return null;
		}
		
		return projectDAO.update(project);
	}

	@Override
	public Project delete(Project project) {
		if(!projectExists(project.getProject_id())){
			return null;
		}
		
		return projectDAO.delete(project);
	}

	@Override
	public boolean projectExists(String id) {

		Project valid_project = projectDAO.read(id);

		if(valid_project==null){
			return false;
		}
		return true;
	}

	
	
}
