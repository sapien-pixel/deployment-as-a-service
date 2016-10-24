package com.daas.dao.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import junit.framework.TestCase;

import com.daas.model.User;

public class CreateUserTest extends TestCase {

	public void testCreateUser(){

		User user = new User();
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setEmail("johndoe@mockcompany.com");
		user.setPassword("test");
		user.setOrganization("Mock Company");
		user.setDateRegistered(System.currentTimeMillis());

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

	}


}
