package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.AWSElasticBlockStoreVolumeSource;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimSpec;
import io.fabric8.kubernetes.api.model.PersistentVolumeSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class KubeConnectionTest {

	@Test
	public void checkKubeConn() {

		Set<String> set = new HashSet<String>();
		set.add("heapster");
		set.add("kube-dns");
		set.add("kibana-logging");
		set.add("monitoring-influxdb");
		set.add("kubernetes");
		set.add("elasticsearch-logging");
		set.add("kubernetes-dashboard");
		set.add("monitoring-grafana");			
		set.add("heapster-v1.2.0");

		// to get cluster ip, usename, password
		// gcloud container clusters describe mean-cluster
		// give permissions - gsutil acl ch -r -u AllUsers:R gs://artifacts.kubernetes-mean.appspot.com	
		

		// GET services, deployments from existing cluster
		KubernetesConnection kubernetesConnection = new KubernetesConnection("https://104.154.210.157:443", "admin", "aHdvkw6teCg5dhon");
		KubernetesClient client = kubernetesConnection.getClient();
		List<Service> services = KubernetesService.getAllKubeServices(client) ;
		List<Deployment> deployments = KubernetesDeployment.getAllKubeDeployments(client);

		// create on new cluster
		KubernetesConnection kubernetesConnection1 = new KubernetesConnection("https://35.162.104.154:443", "admin", "ib4KbtfVSZ773IJA");
		client = kubernetesConnection1.getClient();
		
		// If the volume tag exists, then create Persistent Volume and Persistent Volume Claim
		// and attach the AWS EBS volume-id to claim 
		
		
		// Create @PersistentVolume - Attach to metadata context of RC
		PersistentVolume volume = new PersistentVolume();
		volume.setApiVersion("v1");
		
		// Insert MetaData
		ObjectMeta meta = new ObjectMeta();
		meta.setName("project-id-pv");
		Map<String, String> lbls = new HashMap<String, String>();
		lbls.put("type", "amazonEBS");
		meta.setLabels(lbls);
		volume.setMetadata(meta);
		
		// Insert Spec
		PersistentVolumeSpec volumeSpec = new PersistentVolumeSpec();

		AWSElasticBlockStoreVolumeSource volumeSource = new AWSElasticBlockStoreVolumeSource();
		volumeSource.setVolumeID("volumeID");
		volumeSource.setFsType("ext4");		
		volumeSpec.setAwsElasticBlockStore(volumeSource);
		
		Map<String, Quantity> capacity = new HashMap<String, Quantity>();
		Quantity qt = new Quantity();
		qt.setAmount("5Gi");
		capacity.put("storage",qt);
		volumeSpec.setCapacity(capacity);
		
		volume.setSpec(volumeSpec);
		
		//Create @PersistentVolumeClaim - Attach to claim name
		PersistentVolumeClaim claim = new PersistentVolumeClaim();
		claim.setApiVersion("v1");
		
		// Insert Metadata
		ObjectMeta metadata = new ObjectMeta();
		metadata.setName("project-id-pvc");
		Map<String, String> labels = new HashMap<String, String>();
		labels.put("type", "amazonEBS");
		metadata.setLabels(labels);
		claim.setMetadata(metadata);
		
		// Insert Spec
		PersistentVolumeClaimSpec claimSpec = new PersistentVolumeClaimSpec();
		ResourceRequirements resources = new ResourceRequirements();
		Quantity quantity = new Quantity();
		quantity.setAmount("5Gi");
		Map<String, Quantity> requests = new HashMap<String, Quantity>();
		requests.put("storage", quantity);
		resources.setRequests(requests);
		claimSpec.setResources(resources);
		claim.setSpec(claimSpec);
		
		
		for(int i=services.size()-1;i>=0;i--) {

			if(set.contains(services.get(i).getMetadata().getName())){
				services.remove(i);
			}			
		}		

		for(int i=0; i<services.size();i++) {
			services.get(i).getSpec().setClusterIP(null);
			services.get(i).getMetadata().setResourceVersion("0");
		}

		for(int i=deployments.size()-1;i>=0;i--) {

			if(set.contains(deployments.get(i).getMetadata().getName())){
				deployments.remove(i);
			}			
		}		

		for(int i=0; i<deployments.size();i++) {
			
			deployments.get(i).getMetadata().setResourceVersion("0");
		}		
		
		List<Service> newServices = KubernetesService.createKubeServices(client, services);
		List<Deployment> newDeployments = KubernetesDeployment.createKubeDeployments(client, deployments);		

		// get external IP(app)
		String ip;
		for(Service service: newServices){
			ip = service.getStatus().getLoadBalancer().getIngress().get(0).getIp();
			if(ip != null){
				System.out.println(ip);
				break;
			}			
		}

	}
}
