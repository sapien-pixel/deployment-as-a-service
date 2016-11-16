package com.daas.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.daas.dao.UserDAO;
import com.daas.model.Project;
import com.daas.model.User;

/**
 * {@link User} DAO Implementation
 * @author Vivek
 */
public class UserDAOImpl implements UserDAO {

	@Override
	public User create(User user) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.save(user);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	@Override
	public User read(long id) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		try{
			user = (User)session.get(User.class,id);
			tx.commit();
		} catch(HibernateException h){
			tx.rollback();
		} finally{
			session.close();
		}
		return user;
	}

	@Override
	public User update(User user) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.update(user);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;	
	}

	@Override
	public User delete(User user) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		try{
			session.delete(user);
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getUserByEmail(String email) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		List<User> users = null;
		try{
			String hql = "FROM com.daas.model.User as user WHERE user.email = :email";
			users = session.createQuery(hql)
					.setParameter("email", email)
					.getResultList();
			if(users.size()!=0)
				user = users.get(0);			
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public User getUserByOrganization(String orgName) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		List<User> users = null;
		try{
			String hql = "FROM com.daas.model.User as user WHERE user.organization = :orgName";
			users = session.createQuery(hql)
					.setParameter("orgName", orgName)
					.getResultList();
			if(users.size()!=0)
				user = users.get(0);			
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Project> getAllProjects(long user_id) {

		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		List<Project> projects = null;
		try{

			String hql = "FROM com.daas.model.Project as p where p.user_id = :user_id";
			projects = session.createQuery(hql)
					.setParameter("user_id", user_id)
					.getResultList();
			tx.commit();
		} catch(HibernateException h) {
			tx.rollback();
		} finally {
			session.close();
		}
		return projects;
	}

}
