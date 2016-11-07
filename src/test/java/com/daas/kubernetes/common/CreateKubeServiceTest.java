package com.daas.kubernetes.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.TestCase;

public class CreateKubeServiceTest extends TestCase{

	
	public void testCreateKubeService() throws Exception{
		
		String URI = "";
		String masterPassword = "";
		String masterUsername = "";
		
		InputStream is = new FileInputStream("src/main/resources//daas/src/test/resources/redis-master-service.yaml");
		
		
		KubernetesConnection conn = new KubernetesConnection(URI, masterUsername, masterPassword);
		
		KubernetesService.createKubeService(conn.getClient(), is);
		
	}
	
	
}
