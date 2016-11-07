package com.daas.kubernetes.common;

import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.InputStream;
import java.util.List;

/**
 * Utility class for Kubernetes deployment related operations
 * @author Vivek
 */
public class KubernetesDeployment {


	/**
	 * Creating a new kubernetes deployment
	 * @param client
	 * 					Kubernetes client
	 * @param inputStream
	 * 					YAML input stream	 
	 * @return {@link Deployment}
	 */
	public static Deployment createKubeDeployment(KubernetesClient client, InputStream inputStream) {

		return client.extensions().deployments().load(inputStream).create();				
	}


	/**
	 * Get a kubernetes deployment by name
	 * @param client
	 * 					Kubernetes client
	 * @param deploymentName
	 * 					Name of the deployment
	 * @return {@link Deployment}
	 */
	public static Deployment getKubeDeployment(KubernetesClient client, String deploymentName) {

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

		return client.extensions().deployments().load(inputStream).edit().done();
	}


	/**
	 * Delete a kubernetes deployment by name
	 * @param client
	 * 					Kubernetes client
	 * @param deploymentName
	 * 					Name of the deployment
	 */
	public static void deleteKubeDeployment(KubernetesClient client, String deploymentName) {

		client.extensions().deployments().withName(deploymentName).delete();
	}

}
