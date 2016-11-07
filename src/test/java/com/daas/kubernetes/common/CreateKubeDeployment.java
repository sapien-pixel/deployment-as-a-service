package com.daas.kubernetes.common;

import java.io.FileInputStream;
import java.io.InputStream;

public class CreateKubeDeployment {

	
	
	public void testCreateKubeDeployment() throws Exception{

		String URI = "";
		String masterPassword = "";
		String masterUsername = "";

		InputStream is = new FileInputStream("src/main/resources//daas/src/test/resources//daas/src/test/resources/redis-master-deployment.yaml");

		KubernetesConnection conn = new KubernetesConnection(URI, masterUsername, masterPassword);

		KubernetesDeployment.createKubeDeployment(conn.getClient(), is);
		
	}

}
