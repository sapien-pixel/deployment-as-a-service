package com.daas.resource;

import io.fabric8.kubernetes.api.model.AWSElasticBlockStoreVolumeSource;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.AmazonEC2Common;
import com.daas.aws.common.AmazonIAMCommon;
import com.daas.aws.common.AmazonSTSCommon;
import com.daas.common.ConfFactory;
import com.daas.common.DaaSConstants;
import com.daas.kubernetes.common.KubernetesConnection;
import com.daas.kubernetes.common.KubernetesDeployment;
import com.daas.kubernetes.common.KubernetesService;
import com.daas.model.Project;
import com.daas.model.User;
import com.daas.service.ProjectService;
import com.daas.service.UserService;
import com.daas.service.impl.ProjectServiceImpl;
import com.daas.service.impl.UserServiceImpl;
import com.daas.util.DaasUtil;
import com.daas.util.JWTUtil;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


@Path("/project")
public class ProjectResource {

	private static Logger log = LoggerFactory.getLogger(ProjectResource.class.getName());

	static HashFunction hf = Hashing.md5();
	private static UserService userService = new UserServiceImpl();
	private static ProjectService projectService = new ProjectServiceImpl();

	static String amiId = ConfFactory.getConf().getString("ec2.common.amiId");
	static String instanceType = ConfFactory.getConf().getString("ec2.kubeMS.instanceType");
	static String securityGroupName = ConfFactory.getConf().getString("ec2.security.groupName");
	static String keypairName = ConfFactory.getConf().getString("ec2.keypair.name");
	static String iamRoleName = ConfFactory.getConf().getString("iam.role.admin");
	static String mosquittoHostIP = ConfFactory.getConf().getString("mosquitto.broker.ip");

	/**
	 * Add a new Kubernetes DaaS project
	 * This creates a Kubernetes cluster with specified configurations.
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					Project object
	 * @param user_id
	 * 					User Id
	 * @return created/updated Project object
	 * @throws Exception
	 */
	@POST
	@Path("/add/{user_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProject(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("user_id") long user_id) throws Exception{

		log.info("New request to add a Project for userId - "+ user_id);

		if (cookie == null) { 
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		// check if valid user
		if(!userService.userExists(user_id))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid User").build();

		// check for mandatory fields, if null values
		Map<String,Object> map = new  HashMap<String,Object>();
		map.put("cloud_access_key", project.getCloud_access_key());
		map.put("cloud_secret_key", project.getCloud_secret_key());
		DaasUtil.checkForNull(map);

		// create IAM role
		AmazonIAMCommon iam = new AmazonIAMCommon(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		if(!iam.createAdminAccessIAMRole(iamRoleName))	
			return Response.status(Response.Status.BAD_REQUEST).entity("Problem creating IAM role").build();

		// generate project ID
		project.setProject_id(hf.newHasher().putLong(System.currentTimeMillis()).putLong(user_id).hash().toString());

		// first project
		User user = userService.read(user_id);
		project.setUser_id(user);
		String key = null;
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		
		if(project.getVolume_size()!=null) {
			project.setVolume_id(ec2.createVolume(project.getVolume_size()));
		}
		
		if(user.getManagementEC2InstanceId().equals(DaaSConstants.TEMP_MGMT_EC2_INSTANCE_ID)) {
			project = createFirstProject(project,user_id, user.getOrganization());
			key = project.getAws_key();
		} else{
			key = project.getAws_key();
			ec2.createCluster(project, user.getManagementEC2InstanceId(), key, user.getOrganization(),mosquittoHostIP);
		}

		project.setDateCreated(System.currentTimeMillis());
		projectService.create(project);

		// set keys null
		project.setCloud_access_key(null);
		project.setCloud_secret_key(null);

		return Response.ok("Succesfully added Project").entity(project).build();		
	}
	
	/**
	 * Updates the Project with Cluster master IP(project_url).
	 * Called upon successful creation/setup of Kubernetes cluster
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					Project Object
	 * @param id
	 * 					Project Id
	 * @return updated Project object
	 * @throws Exception
	 */
	@POST
	@Path("/updateClusterMaster/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateClusterMaster(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("id") String id) throws Exception{

		log.info("New request to add Cluster IP for a Project with ID - "+ id);

		if (cookie == null) { 
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		String project_url = project.getProject_url();
		
		// check for mandatory fields, if null values
		Map<String,Object> map = new  HashMap<String,Object>();
		map.put("project_id", id);
		map.put("project_url", project_url);
		map.put("project_username", project.getProject_username());
		map.put("project_password", project.getProject_password());
		
		DaasUtil.checkForNull(map);
				
		project = projectService.read(id);
		
		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		project.setProject_url(project_url);
		project.setProject_username(project.getProject_username());
		project.setProject_password(project.getProject_password());
		projectService.update(project);
		
		return Response.ok("Succesfully updated Project with Cluster IP").entity(project).build();		
	}

	/**
	 * Get a Kubernetes cluster details(services and deployments)
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					Project Object
	 * @param id
	 * 					Project Id
	 * @return updated Project object
	 * @throws Exception
	 */
	@POST
	@Path("/clusterDetails/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getClusterDetails(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("id") String id) throws Exception{

		log.info("New request to get Cluster details for Project with ID - "+ id);

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		if(projectService.read(id)==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		// check for mandatory fields, if null values
		Map<String,Object> map = new  HashMap<String,Object>();
		map.put("old_cluster_url", project.getOld_clusterURL());
		map.put("clusterMasterUsername", project.getClusterMasterUsername());
		map.put("clusterMasterPassword", project.getClusterMasterPassword());
		DaasUtil.checkForNull(map);

		// check if URL is valid - https://<IP>:<PORT>
		if(!DaasUtil.validURL(project.getOld_clusterURL()))
			return Response.status(Response.Status.BAD_REQUEST).entity("Not a valid Master URL").build();

		// Connect to Kubernetes cluster
		KubernetesConnection kubernetesConnection = new KubernetesConnection(project.getOld_clusterURL(), project.getClusterMasterUsername(), project.getClusterMasterPassword());
		KubernetesClient client = kubernetesConnection.getClient();
		if(client == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Could Not Connect to Kubernetes Cluster").build();			

		// Get Cluster info, services and deployments
		// removes the default services, deployments and unnecessary resources
		project.setServices(KubernetesService.getAllKubeServices(client));
		project.setDeployments(KubernetesDeployment.getAllKubeDeployments(client));
		
		// TODO: Have to see the format once
		return Response.ok("Success").entity(project).build();
	}

	
	/**
	 * This method creates/deploys application(services and deployments) on the cluster created using DaaS.
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					Project object
	 * @param id
	 * 					Project Id
	 * @return updated Project object
	 * @throws Exception
	 */
	@POST
	@Path("/deployApp/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deployApp(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("id") String id) throws Exception{

		log.info("New request to deploy application on Kubernetes Cluster for Project with ID - "+ id);

		if (cookie == null) { 
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
		
		if(projectService.read(id)==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		// check for mandatory fields, if null values
		Map<String,Object> map = new  HashMap<String,Object>();
		map.put("project_url", project.getProject_url());
		map.put("project_username", project.getProject_username());
		map.put("project_password", project.getProject_password());
		DaasUtil.checkForNull(map);
		
		// check if project_url exists
		if(!projectService.projectURLExists(id))
			return Response.status(Response.Status.BAD_REQUEST).entity("There's something wrong with the Cluster IP").build();
		
		// check if services and deployments are not empty
		if(project.getServices().size()==0 || project.getDeployments().size()==0)
			return Response.ok("No services/deployments to deploy").entity(project).build();
		
		// Connect to Kubernetes cluster
		KubernetesConnection kubernetesConnection = new KubernetesConnection(project.getProject_url(), project.getProject_username(), project.getProject_password());
		KubernetesClient client = kubernetesConnection.getClient();
		if(client == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Could Not Connect to Project's Kubernetes Cluster").build();
				
		// create services and deployments		
		
		//Add volume to deployments
		for(int i=0; i<project.getDeployments().size();i++) {
			
			if(project.getDeployments().get(i).getSpec().getTemplate().getSpec().getVolumes().size() >0) {
				project.getDeployments().get(i).getSpec().getTemplate().getSpec().setVolumes(null);
				Volume vol = new Volume();
				vol.setName(project.getDeployments().get(i).getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts().get(0).getName());
				AWSElasticBlockStoreVolumeSource src = new AWSElasticBlockStoreVolumeSource();
				src.setVolumeID(project.getVolume_id());
				src.setFsType("ext4");
				vol.setAwsElasticBlockStore(src);
				List<Volume> list = new ArrayList<Volume>();
				list.add(vol);
				project.getDeployments().get(i).getSpec().getTemplate().getSpec().setVolumes(list);
			}
		}
		
		
		//Create Services and Deployments
		for(int i=0, j=0;(i<project.getDeployments().size() || j<project.getServices().size());i++, j++) {
			
			
			if(j < project.getServices().size()) {
				KubernetesService.createKubeService(client, project.getServices().get(j));
			}
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(i < project.getDeployments().size()) {
				KubernetesDeployment.createKubeDeployment(client, project.getDeployments().get(i));
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		List<Service> newServices = KubernetesService.getAllKubeServices(client);
		
		project.setServices(project.getServices());
		project.setDeployments(project.getDeployments());
		
		// TODO: the external IP takes some time
		// set App IP
		project = addAppIpToProject(project, newServices);
		projectService.update(project);
		
		
		//Wait for the application to become up and running
		Thread.sleep(60000);
		
		return Response.ok("Succesfully deployed app").entity(project).build();		
	}
	

	/**
	 * Gets a project(cluster)
	 * @param cookie
	 * 					JWT cookie
	 * @param id
	 * 					Project Id
	 * @return Project object
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(@CookieParam("daas-token") Cookie cookie, @PathParam("id") String id){

		log.info("New request to get a Project with Project ID - "+ id);

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		Project project = projectService.read(id);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Success").entity(project).build();			
	}


	/**
	 * Updates a DaaS project
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					Project object
	 * @return updated Project object
	 */
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(@CookieParam("daas-token") Cookie cookie, Project project){

		log.info("New request to update a Project for userId - "+ project.getUser_id());

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		project = projectService.update(project);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Succesfully updated Project info").entity(project).build();
	}


	/**
	 * Delete a DaaS project i.e. the kube cluster
	 * @param cookie
	 * 					JWT cookie
	 * @param project
	 * 					project object
	 * @param user_id
	 * 					User Id
	 * @return deleted Project
	 */
	@POST
	@Path("/delete/{user_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProject(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("user_id") long user_id){

		log.info("New request to delete a Project for userId - "+ project.getUser_id());

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		// check if valid user
		if(!userService.userExists(user_id))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid User").build();
		
		// check if valid project
		if(projectService.read(project.getProject_id())==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		//Delete cluster
		String key = project.getAws_key();
		User user = userService.read(user_id);

		AmazonEC2Common.deleteCluster(project, user.getManagementEC2InstancePulicIp(),key);

		project = projectService.delete(project);

		return Response.ok("Succesfully deleted Project").entity(project).build();
	}

	/**
	 * Extension static method from /add REST API to create EC2 instance and create Kubernetes Cluster
	 * @param project
	 * 					Project Object
	 * @param userId
	 * 					User Id
	 * @param orgName
	 * 					Organization Name
	 * @return updated Project object
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Project createFirstProject(Project project, Long userId, String orgName) throws IOException, InterruptedException{

		// share Kube common AMI with User's AWS account
		AmazonSTSCommon sts = new AmazonSTSCommon(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		String userAccountId = sts.getUserAWSAccountId();
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));
		ec2.shareAMIAcrossAccounts(amiId, userAccountId);		

		// create EC2 Kube Management server on User's AWS account
		ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		String key = ec2.createEC2KeyPair(keypairName);
		project.setAws_key(key);

		String instanceId = ec2.createEC2Instance(amiId, instanceType, iamRoleName, securityGroupName, keypairName, project.getProject_id(), String.valueOf(userId));

		// Create Kubernetes Cluster
		ec2.createCluster(project,instanceId, key,orgName,mosquittoHostIP);

		// Setting the Instance ID for User
		User user = userService.read(userId);
		user.setManagementEC2InstanceId(instanceId);
		user.setManagementEC2InstancePulicIp(ec2.getPublicIp(instanceId));
		userService.update(user);

		return project;
	}
	
	/**
	 * Upon successful deployment of application on Kube Cluster(created through DaaS), update the App IP
	 * @param project
	 * 						Project object
	 * @param services
	 * 						List of Kubernetes services
	 * @return updated Project object
	 */
	private Project addAppIpToProject(Project project, List<Service> services) {
		
		String ip;
		for(Service service: services){
			if(service.getSpec().getType().equals("LoadBalancer")) {
				ip = service.getStatus().getLoadBalancer().getIngress().get(0).getHostname();
				if(ip != null){
					project.setApp_url(ip);
					break;
				}			
			}
		}
		return project;
	}
}
