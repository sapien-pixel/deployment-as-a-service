package com.daas.kubernetes.utils.test;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import org.junit.Test;

import com.daas.common.ConfFactory;
import com.daas.kubernetes.utils.*;

public class CertificateUtilsTest {
	
	@Test
	public void trustCertificate() {
		String ipAddr = ConfFactory.getConf().getString("kube.master.ip");
		String port = ConfFactory.getConf().getString("kube.master.port");
		String URI = "https://" + ipAddr+ ":" + port;
		
		CertificateUtils.trustEveryone(URI);

		Config config = new ConfigBuilder().withMasterUrl(URI)
				.withTrustCerts(false)			          
				.withUsername(ConfFactory.getConf().getString("kube.master.userName"))
				.withPassword(ConfFactory.getConf().getString("kube.master.password"))
				.build();
		KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
		System.out.println(kubernetesClient.services().get().getAdditionalProperties());	
		PodList pods = kubernetesClient.pods().list();
		for (Pod pod : pods.getItems()) {
			String name = pod.getMetadata().getName();
			String ip = pod.getStatus().getPodIP();
			System.out.println("Pod: " + name +"Pod IP:"+ ip);
		}
		kubernetesClient.close();
	}
}