package com.daas.kubernetes.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.daas.kubernetes.utils.CertificateUtils;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesConnection {

	private static final Logger logger = LoggerFactory.getLogger(KubernetesConnection.class);

	private String URI; // https://<IP>:<PORT>
	private String masterUsername;
	private String masterPassword;

	public KubernetesConnection(String URI, String masterUsername, String masterPassword){
		this.URI = URI;
		this.masterUsername = masterUsername;
		this.masterPassword = masterPassword;
	}

	public KubernetesClient getClient() {

		try{
			CertificateUtils.trustEveryone(this.URI);
			Config config = new ConfigBuilder().withMasterUrl(URI)
					.withTrustCerts(true)			          
					.withUsername(masterUsername)
					.withPassword(masterPassword)
					.build();

			return new DefaultKubernetesClient(config);
		} catch(Exception e) {
			logger.warn("Couldn't connect to Kubernetes Cluster with URL - "+ this.URI);
			return null;
		}


	}
}
