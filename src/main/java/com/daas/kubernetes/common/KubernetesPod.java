package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.exception.DaaSException;

public class KubernetesPod {

	private static Logger log = LoggerFactory.getLogger(KubernetesPod.class.getName());


	/**
	 * Creating a new kubernetes pod reading from YAML file
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream 				
	 * @return {@link Pod}
	 */
	public static Pod createKubePod(KubernetesClient client, InputStream inputStream) {

		log.info("Creating Kubernetes Pod for URL - " + client.getMasterUrl());

		return client.pods().load(inputStream).create();
	}

	
	/**
	 * Creating a new kubernetes pod from existing Pod
	 * @param client
	 * 					Kubernetes client
	 * @param pod
	 * 					Kubernetes Pod				
	 * @return {@link Pod}
	 */
	public static Pod createKubePod(KubernetesClient client, Pod pod) {

		log.info("Creating Kubernetes Pod for URL - " + client.getMasterUrl());

		return client.pods().create(pod);
	}
	

	/**
	 * Get a kubernetes pod by name
	 * @param client
	 * 					Kubernetes client
	 * @param podName	
	 * 					Name of the pod
	 * @return {@link Pod}
	 * @throws DaaSException 
	 */
	public static Pod getKubePod(KubernetesClient client, String podName) throws DaaSException {

		if(podName == null || podName.isEmpty() || podName==""){
			log.warn("Invalid pod name");
			throw new DaaSException("Invalid pod name");
		}
		
		return client.pods().withName(podName).get();		
	}

	/**
	 * Get List of all the kubernetes pods
	 * @param client
	 * 					Kubernetes client
	 * @return list of {@link Pod}
	 */
	public static List<Pod> getAllKubePods(KubernetesClient client) {

		PodList podList = client.pods().list();		
		return podList.getItems();		
	}


	/**
	 * Edit a kubernetes pod
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream
	 * @return {@link Pod}
	 */
	public static Pod editKubePod(KubernetesClient client, InputStream inputStream) {

		log.info("Editing Kubernetes Pod for URL - " + client.getMasterUrl());

		return client.pods().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes pod by name
	 * @param client
	 * 					Kubernetes client
	 * @param podName
	 * 					Name of the pod
	 * @throws DaaSException 
	 */
	public static void deleteKubePod(KubernetesClient client, String podName) throws DaaSException {

		if(podName == null || podName.isEmpty() || podName==""){
			log.warn("Invalid pod name");
			throw new DaaSException("Invalid pod name");
		}
		
		log.info("Deleting Kubernetes Pod - "+ podName+" for URL - " + client.getMasterUrl());
		
		client.pods().withName(podName).delete();		
	}

}
