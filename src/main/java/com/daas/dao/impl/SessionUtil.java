package com.daas.dao.impl;

import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionUtil {

	static SessionUtil instance;
	static boolean sessionInit = false;
	private static SessionFactory sessionFactory;

	public static SessionUtil getInstance(){
		
		if(sessionInit)
			return instance;
		
		instance=new SessionUtil();
		
		Properties dbConnectionProperties = new Properties();
		try {
			dbConnectionProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("hibernate.properties"));
		} catch(Exception e) {
			e.printStackTrace();
			// Log
		}

		Configuration configuration = new Configuration().mergeProperties(dbConnectionProperties);
		configuration.configure("hibernate.cfg.xml");
		sessionFactory = configuration.buildSessionFactory();
		
		sessionInit = true;
		return instance;
	}

	public static Session getSession(){
		
		Session session =  getInstance().sessionFactory.openSession();
		return session;
	}

}
