package com.daas.resource;

import java.io.IOException;
import java.util.HashMap;
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
	
	@POST
	@Path("/add/{user_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProject(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("user_id") long user_id) throws Exception{

		log.info("New request to add a Project for userId - "+ project.getUser_id());
		
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

		// check if this is User's first project
		// If it is first project, create EC2 Kube MS first
		// if not. start another kubernetes container

		// first project
		User user = userService.read(user_id);
		String key = null;
		if(user.getManagementEC2InstanceId() == DaaSConstants.TEMP_MGMT_EC2_INSTANCE_ID) {
			// create keypair
			AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
			key = ec2.createEC2KeyPair(keypairName);
			project = createFirstProject(project,user_id);

		} else{
			key = project.getAws_key();
			createKuberntesCluster(project, user.getManagementEC2InstanceId(), key);
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


	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProject(@CookieParam("daas-token") Cookie cookie, Project project){

		log.info("New request to delete a Project for userId - "+ project.getUser_id());
		
		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		project = projectService.delete(project);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Succesfully deleted Project").entity(project).build();
	}




	public static synchronized Project createFirstProject(Project project, Long userId) throws IOException{

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
		ec2.createCluster(project,instanceId, key);
		
		// Setting the Instance ID for User
		User user = userService.read(userId);
		user.setManagementEC2InstanceId(instanceId);
		userService.update(user);

		return project;
	}
	
	public static void createKuberntesCluster(Project project, String instanceId,String key) {
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		ec2.createCluster(project, instanceId, key);
	}
}
