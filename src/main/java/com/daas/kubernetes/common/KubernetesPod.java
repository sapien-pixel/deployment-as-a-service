package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesPod {

	private static Logger log = LoggerFactory.getLogger(KubernetesPod.class.getName());


	/**
	 * Creating a new kubernetes pod
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream 				
	 * @return {@link Pod}
	 */
	public static Pod createKubePod(KubernetesClient client, InputStream inputStream) {

		return client.pods().load(inputStream).create();
	}


	/**
	 * Get a kubernetes pod by name
	 * @param client
	 * 					Kubernetes client
	 * @param podName	
	 * 					Name of the pod
	 * @return {@link Pod}
	 */
	public static Pod getKubePod(KubernetesClient client, String podName) {

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

		return client.pods().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes pod by name
	 * @param client
	 * 					Kubernetes client
	 * @param podName
	 * 					Name of the pod
	 */
	public static void deleteKubePod(KubernetesClient client, String podName) {

		client.pods().withName(podName).delete();		
	}

	

}
