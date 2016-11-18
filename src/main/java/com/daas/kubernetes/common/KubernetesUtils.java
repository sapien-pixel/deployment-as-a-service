package com.daas.kubernetes.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for Kubernetes operations
 * @author Vivek
 */
public class KubernetesUtils {

	public static final Set<String> DEFAULT_KUBE_SERVICES;
	
	public static final Set<String> DEFAULT_KUBE_DEPLOYMENTS;
	
	//init sets with default values
	static {
		
		DEFAULT_KUBE_SERVICES = new HashSet<String>(Arrays.asList("heapster",
				"kube-dns", "kibana-logging", "monitoring-influxdb", "kubernetes",
				"elasticsearch-logging", "kubernetes-dashboard", "monitoring-grafana"));
				
		
		DEFAULT_KUBE_DEPLOYMENTS = new HashSet<String>(Arrays.asList("heapster-v1.2.0"));
		
	}
	
}
