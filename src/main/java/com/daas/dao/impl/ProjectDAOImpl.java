package com.daas.dao.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.daas.dao.ProjectDAO;
import com.daas.model.Project;

/**
 * {@link Project} DAO Implementation
 * @author Vivek
 */
public class ProjectDAOImpl implements ProjectDAO {

	@Override
	public Project create(Project project) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.save(project);
			tx.commit();
		}
		catch(HibernateException h){
			tx.rollback();
		}finally{
			session.close();
		}
		return project;
	}

	@Override
	public Project read(long id) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		Project project = null;
		try{
			project = (Project)session.get(Project.class,id);
			tx.commit();
		}
		catch(HibernateException h){
			tx.rollback();
		}finally{
			session.close();
		}
		return project;
	}

	@Override
	public Project update(Project project) {
		
		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.update(project);
			tx.commit();
		}
		catch(Exception h){
			tx.rollback();
			project=null;
		}finally{
			session.close();
		}
		return project;
	}

	@Override
	public Project delete(Project project) {
		
		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{			
			session.delete(project);
			tx.commit();
		}
		catch(HibernateException h){
			h.printStackTrace();
			tx.rollback();
		}finally{
			session.close();
		}
		return project;
	}

}
