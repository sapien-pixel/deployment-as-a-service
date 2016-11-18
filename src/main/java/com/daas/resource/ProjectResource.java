package com.daas.resource;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.io.IOException;
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
		String key = null;
		if(user.getManagementEC2InstanceId() == DaaSConstants.TEMP_MGMT_EC2_INSTANCE_ID) {
			// create keypair
			AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
			key = ec2.createEC2KeyPair(keypairName);
			if(project.getVolume_size()!=null) {
				project.setVolume_id(ec2.createVolume(project.getVolume_size()));
			}
			project = createFirstProject(project,user_id, user.getOrganization());

		} else{
			key = project.getAws_key();
			AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
			ec2.createCluster(project, user.getManagementEC2InstanceId(), key, user.getOrganization());
		}

		project.setUser_id(user);
		project.setDateCreated(System.currentTimeMillis());
		project = projectService.create(project);

		// set keys null
		project.setCloud_access_key(null);
		project.setCloud_secret_key(null);

		// send keyPair in header, send null if not first project
		return Response.ok("Succesfully added Project").header("AWS_Key", key).entity(project).build();		
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
		DaasUtil.checkForNull(map);
				
		project = projectService.read(id);
		
		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		project.setProject_url(project_url);
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
	@GET
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
		project.setServices(KubernetesService.getAllKubeServices(client));
		project.setDeployments(KubernetesDeployment.getAllKubeDeployments(client));

		// TODO: Have to see thhe format once
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
		
		// TODO: confirm if UI can handle the number of replicas in same object
		// create services and deployments		
		List<Service> services= KubernetesService.createKubeServices(client, project.getServices());
		List<Deployment> deployments = KubernetesDeployment.createKubeDeployments(client, project.getDeployments());		
		
		project.setServices(services);
		project.setDeployments(deployments);
		
		// set App IP
		project = addAppIpToProject(project, services);		
		
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
	@DELETE
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

		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		ec2.deleteCluster(project, user.getManagementEC2InstanceId(),key);

		project = projectService.delete(project);

		return Response.ok("Succesfully deleted Project").entity(project).build();
	}

	// TODO: make this private?
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
		ec2.createCluster(project,instanceId, key,orgName);

		// Setting the Instance ID for User
		User user = userService.read(userId);
		user.setManagementEC2InstanceId(instanceId);
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
			// TODO: what to get? couple of options here10
			ip = service.getSpec().getLoadBalancerIP();
			if(ip != null){
				project.setApp_url(ip);
				break;
			}			
		}
		return project;
	}

	
}
