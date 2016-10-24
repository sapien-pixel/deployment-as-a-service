package com.daas.dao.impl;

import junit.framework.TestCase;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.daas.model.User;

public class GetUserTest extends TestCase {

	public void testCreateUser(){
		
		Session session = SessionUtil.getSession();
		Transaction tx =  session.beginTransaction();
		User user = null;
		try{
			user = (User)session.get(User.class,1);
			tx.commit();
		} catch(HibernateException h){
			tx.rollback();
		} finally{
			session.close();
		}

		System.out.println(user.getFirstName());
	}
}
