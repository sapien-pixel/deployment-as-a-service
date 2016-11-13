package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for Kubernetes service related operations
 * @author Vivek
 */
public class KubernetesService {

	private static Logger log = LoggerFactory.getLogger(KubernetesService.class.getName());

	/**
	 * Creating a new kubernetes service
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream	 
	 * @return {@link Service}
	 */
	public static Service createKubeService(KubernetesClient client, InputStream inputStream) {

		return client.services().load(inputStream).create();
	}


	/**
	 * Get a kubernetes service by name
	 * @param client
	 * 					Kubernetes client
	 * @param serviceName
	 * 					Name of the service
	 * @return {@link Service}
	 */
	public static Service getKubeService(KubernetesClient client, String serviceName) {

		return client.services().withName(serviceName).get();		
	}


	/**
	 * Get List of all the kubernetes services
	 * @param client
	 * 					Kubernetes client
	 * @return list of {@link Service}
	 */
	public static List<Service> getAllKubeServices(KubernetesClient client) {

		ServiceList serviceList = client.services().list();		
		return serviceList.getItems();		
	}


	/**
	 * Edit a kubernetes service
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream
	 * @return {@link Service}
	 */
	public static Service editKubeService(KubernetesClient client, InputStream inputStream) {

		return client.services().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes service by name
	 * @param client
	 * 					Kubernetes client
	 * @param serviceName
	 * 					Name of the service
	 */
	public static void deleteKubeService(KubernetesClient client, String serviceName) {

		client.services().withName(serviceName).delete();		
	}



}
