package com.daas.dao.impl;

import java.util.Properties;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionUtil {

	private static SessionUtil instance=new SessionUtil();
	private SessionFactory sessionFactory;

	public static SessionUtil getInstance(){
		return instance;
	}

	private SessionUtil(){

		Properties dbConnectionProperties = new Properties();
		try {
			SessionUtil.class.getClassLoader();
			dbConnectionProperties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("hibernate.properties"));
		} catch(Exception e) {
			e.printStackTrace();
			// Log
		}

		Configuration configuration = new Configuration().mergeProperties(dbConnectionProperties);
		configuration.configure("hibernate.cfg.xml");

		sessionFactory = configuration.buildSessionFactory();
	}

	public static Session getSession(){
		Session session =  getInstance().sessionFactory.openSession();

		return session;
	}

}
