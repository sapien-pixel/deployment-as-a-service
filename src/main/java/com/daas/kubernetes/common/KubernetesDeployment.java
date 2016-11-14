package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.exception.DaaSException;

/**
 * Utility class for Kubernetes deployment related operations
 * @author Vivek
 */
public class KubernetesDeployment {

	private static Logger log = LoggerFactory.getLogger(KubernetesDeployment.class.getName());


	/**
	 * Creating a new kubernetes deployment reading from YAML file
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream	 
	 * @return {@link Deployment}
	 */
	public static Deployment createKubeDeployment(KubernetesClient client, InputStream inputStream) {

		log.info("Creating Kubernetes Deployment for URL - " + client.getMasterUrl());
		
		return client.extensions().deployments().load(inputStream).create();				
	}


	/**
	 * Creating a new kubernetes deployment from existing kubernetes deployment
	 * @param client
	 * 					Kubernetes client
	 * @param deployment
	 * 					Kubernetes deployment	 
	 * @return {@link Deployment}
	 */
	public static Deployment createKubeDeployment(KubernetesClient client, Deployment deployment) {

		log.info("Creating Kubernetes Deployment for URL - " + client.getMasterUrl());
		
		return client.extensions().deployments().create(deployment);
	}

	/**
	 * Get a kubernetes deployment by name
	 * @param client
	 * 					Kubernetes client
	 * @param deploymentName
	 * 					Name of the deployment
	 * @return {@link Deployment}
	 * @throws DaaSException 
	 */
	public static Deployment getKubeDeployment(KubernetesClient client, String deploymentName) throws DaaSException {

		if(deploymentName == null || deploymentName.isEmpty() || deploymentName==""){
			log.warn("Invalid deployment name");
			throw new DaaSException("Invalid deployment name");
		}			
		
		return client.extensions().deployments().withName(deploymentName).get();
	}


	/**
	 * Get List of all the kubernetes deployments
	 * @param client
	 * 					Kubernetes client
	 * @return list of {@link Deployment}
	 */
	public static List<Deployment> getAllKubeDeployments(KubernetesClient client) {

		DeploymentList deploymentList = client.extensions().deployments().list();
		return deploymentList.getItems();		
	}


	/**
	 * Edit a kubernetes deployment
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream
	 * @return {@link Deployment}
	 */
	public static Deployment editKubeDeployment(KubernetesClient client, InputStream inputStream) {

		log.info("Editing Kubernetes Deployment for URL - " + client.getMasterUrl());

		return client.extensions().deployments().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes deployment by name
	 * @param client
	 * 					Kubernetes client
	 * @param deploymentName
	 * 					Name of the deployment
	 * @throws DaaSException 
	 */
	public static void deleteKubeDeployment(KubernetesClient client, String deploymentName) throws DaaSException {

		if(deploymentName == null || deploymentName.isEmpty() || deploymentName==""){
			log.warn("Invalid deployment name");
			throw new DaaSException("Invalid deployment name");
		}
		
		log.info("Deleting Kubernetes Deployment - "+ deploymentName+" for URL - " + client.getMasterUrl());

		client.extensions().deployments().withName(deploymentName).delete();
	}


	/**
	 * Scale a kubernetes deployment
	 * @param client
	 * 					Kubernetes client
	 * @param deploymentName
	 * 					Name of the deployment
	 * @return {@link Deployment}
	 * @throws DaaSException 
	 */
	public static Deployment scaleKubeDeployment(KubernetesClient client, String deploymentName, Integer replicas) throws DaaSException {

		if(deploymentName == null || deploymentName.isEmpty() || deploymentName==""){
			log.warn("Invalid deployment name");
			throw new DaaSException("Invalid deployment name");
		}
		
		if(replicas<=0){
			log.warn("Invalid number of replicas to scale deployment - "+ deploymentName+" for URL - " + client.getMasterUrl());
			throw new DaaSException("Invalid number of replicas to scale deployment - "+ deploymentName+" for URL - " + client.getMasterUrl());			
		}
		
		return client.extensions().deployments().withName(deploymentName)
				.edit()
					.editSpec()
						.withReplicas(replicas)
					.endSpec()	
				.done();
	}

}
