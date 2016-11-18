package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.exception.DaaSException;


/**
 * Utility class for Kubernetes service related operations
 * @author Vivek
 */
public class KubernetesService {

	private static Logger log = LoggerFactory.getLogger(KubernetesService.class.getName());

	/**
	 * Creating a new kubernetes service reading from YAML file
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream	 
	 * @return {@link Service}
	 */
	public static Service createKubeService(KubernetesClient client, InputStream inputStream) {

		log.info("Creating Kubernetes Service for URL - " + client.getMasterUrl());

		return client.services().load(inputStream).create();
	}

	/**
	 * Creating a new kubernetes service from a existing kubernetes service
	 * @param client
	 * 					Kubernetes client
	 * @param service
	 * 					Kubernetes Service	 
	 * @return {@link Service}
	 */
	public static Service createKubeService(KubernetesClient client, Service service) {

		log.info("Creating Kubernetes Service for URL - " + client.getMasterUrl());

		return client.services().create(service);
	}

	/**
	 * Creating a list of services from existing Kubernetes services
	 * @param client
	 * 					Kubernetes client
	 * @param services
	 * 					List of Service to create
	 * @return list of {@link Service}
	 */
	public static List<Service> createKubeServices(KubernetesClient client, List<Service> services){

		log.info("Creating Kubernetes Services for URL - " + client.getMasterUrl());

		List<Service> createdServices = new ArrayList<Service>();
		
		for (Service service : services){
			createKubeService(client, service);
			createdServices.add(service);
			log.info("Created Kubernetes Service -"+ service.getMetadata().getName()+ " for URL - "+ client.getMasterUrl());
		}
		return createdServices;
	}


	/**
	 * Get a kubernetes service by name
	 * @param client
	 * 					Kubernetes client
	 * @param serviceName
	 * 					Name of the service
	 * @return {@link Service}
	 * @throws DaaSException 
	 */
	public static Service getKubeService(KubernetesClient client, String serviceName) throws DaaSException {

		if(serviceName == null || serviceName.isEmpty() || serviceName==""){
			log.warn("Invalid service name");
			throw new DaaSException("Invalid service name");
		}

		return client.services().withName(serviceName).get();		
	}


	/**
	 * Get List of all the kubernetes services, removing the default services
	 * @param client
	 * 					Kubernetes client
	 * @return list of {@link Service}
	 */
	public static List<Service> getAllKubeServices(KubernetesClient client) {

		ServiceList serviceList = client.services().list();
		return removeAllDefaultServices(serviceList.getItems());
	}
	
	/**
	 * Removes the default kube services and unnecessary resources
	 * Resources removed are - Cluster IP, Resource version
	 * @param services
	 * 					List of Service
	 * @return
	 */
	private static List<Service> removeAllDefaultServices(List<Service> services){
		
		List<Service> updatedServices = new ArrayList<Service>();
		
		for(Service service : services){			
			if(!KubernetesUtils.DEFAULT_KUBE_SERVICES.contains(service.getMetadata().getName())){				
				service.getSpec().setClusterIP(null);
				service.getMetadata().setResourceVersion("v1");
				updatedServices.add(service);
			}				
		}
		return updatedServices;
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

		log.info("Editing Kubernetes Pod for URL - " + client.getMasterUrl());

		return client.services().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes service by name
	 * @param client
	 * 					Kubernetes client
	 * @param serviceName
	 * 					Name of the service
	 * @throws DaaSException 
	 */
	public static void deleteKubeService(KubernetesClient client, String serviceName) throws DaaSException {

		if(serviceName == null || serviceName.isEmpty() || serviceName==""){
			log.warn("Invalid service name");
			throw new DaaSException("Invalid service name");
		}

		log.info("Deleting Kubernetes Service - "+ serviceName+" for URL - " + client.getMasterUrl());

		client.services().withName(serviceName).delete();		
	}


}
