package com.daas.kubernetes.common;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesConnection {
	
	public static KubernetesClient getConnection(String URI, String masterUsername, String masterPassword) {

		// URI = "https://" + ipAddr+ ":" + port;	

		Config config = new ConfigBuilder().withMasterUrl(URI)
				.withTrustCerts(true)			          
				.withUsername(masterUsername)
				.withPassword(masterPassword)
				.build();
		
		KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
		return kubernetesClient;
	}
	
	
}
