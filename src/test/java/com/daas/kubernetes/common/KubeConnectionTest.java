package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.List;

import org.junit.Test;

public class KubeConnectionTest {
	@Test
	public void checkKubeConn(){
		
		// to get cluster ip, usename, password, gcloud container clusters describe mean-cluster
		
		KubernetesConnection kubernetesConnection = new KubernetesConnection("https://104.154.210.157:443", "admin", "aHdvkw6teCg5dhon");
		
		KubernetesClient client = kubernetesConnection.getClient();
		List<Service> services =  KubernetesService.getAllKubeServices(client);
		String ip = "";
		for(Service service: services){
			// TODO: what to get? couple of options here10
			ip = service.getSpec().getExternalIPs().get(0);
			if(ip != null){
				System.out.println(ip);
			}			
		}
		
	}

}
