package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.AWSElasticBlockStoreVolumeSource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class KubeConnectionTest {

	@Test
	public void checkKubeConn() {

		// GET services, deployments from existing cluster
		KubernetesConnection kubernetesConnection = new KubernetesConnection("https://104.197.205.19:443", "", "");
		KubernetesClient client = kubernetesConnection.getClient();
		List<Service> services = KubernetesService.getAllKubeServices(client) ;
		List<Deployment> deployments = KubernetesDeployment.getAllKubeDeployments(client);

		// to get cluster ip, usename, password
		// gcloud container clusters describe mean-cluster
		// give permissions - gsutil acl ch -r -u AllUsers:R gs://artifacts.kubernetes-mean.appspot.com	

		// create on new cluster
		KubernetesConnection kubernetesConnection1 = new KubernetesConnection("https://35.164.188.24:443", "", "");
		client = kubernetesConnection1.getClient();


		for(int i=0; i<deployments.size();i++) {
	
			if(deployments.get(i).getSpec().getTemplate().getSpec().getVolumes().size() >0) {
				deployments.get(i).getSpec().getTemplate().getSpec().setVolumes(null);
				Volume vol = new Volume();
				vol.setName(deployments.get(i).getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts().get(0).getName());
				AWSElasticBlockStoreVolumeSource src = new AWSElasticBlockStoreVolumeSource();
				src.setVolumeID("vol-175d5ca3");
				src.setFsType("ext4");
				vol.setAwsElasticBlockStore(src);
				List<Volume> list = new ArrayList<Volume>();
				list.add(vol);
				deployments.get(i).getSpec().getTemplate().getSpec().setVolumes(list);
			}
		}
		
		for(int i=0, j=0;(i<deployments.size() || j<services.size());i++, j++) {
			
			
			if(j < services.size()) {
				KubernetesService.createKubeService(client, services.get(j));
			}
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(i < deployments.size()) {
				KubernetesDeployment.createKubeDeployment(client, deployments.get(i));
			}
		}
		
		List<Service> newServices = KubernetesService.getAllKubeServices(client);
		
		// get external IP(app)
		String ip;
		for(Service service: newServices){
			if(service.getSpec().getType().equals("LoadBalancer")) {
				ip = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
				if(ip != null){
					System.out.println(ip);
					break;
				}			
			}
		}
	}
}
