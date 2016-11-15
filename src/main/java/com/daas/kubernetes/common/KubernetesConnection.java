package com.daas.kubernetes.common;

import com.daas.kubernetes.utils.CertificateUtils;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesConnection {
	
	private String URI; // https://<IP>:<PORT>
	private String masterUsername;
	private String masterPassword;
	
	KubernetesClient client = null;
	
	public KubernetesConnection(String URI, String masterUsername, String masterPassword){
		this.URI = URI;
		this.masterUsername = masterUsername;
		this.masterPassword = masterPassword;
	}
	
	public KubernetesClient getClient() {
		
		if(client != null)
			return client;
		
		CertificateUtils.trustEveryone(this.URI);
		Config config = new ConfigBuilder().withMasterUrl(URI)
				.withTrustCerts(true)			          
				.withUsername(masterUsername)
				.withPassword(masterPassword)
				.build();
		
		return new DefaultKubernetesClient(config);
	}
}
