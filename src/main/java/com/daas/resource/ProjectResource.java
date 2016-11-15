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

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.AmazonEC2Common;
import com.daas.aws.common.AmazonIAMCommon;
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

	static HashFunction hf = Hashing.md5();
	private static UserService userService = new UserServiceImpl();
	private static ProjectService projectService = new ProjectServiceImpl();

	static String amiId = ConfFactory.getConf().getString("ec2.common.amiId");
	static String instanceType = ConfFactory.getConf().getString("ec2.kubeMS.instanceType");
	static String securityGroupName = ConfFactory.getConf().getString("ec2.security.groupName");
	static String keypairName = ConfFactory.getConf().getString("ec2.keypair.name");

	@POST
	@Path("/add/{user_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProject(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("user_id") long user_id) throws Exception{

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
		map.put("iam_admin_role", project.getIam_admin_role());
		map.put("cloud_account_id", project.getCloud_account_id());
		DaasUtil.checkForNull(map);

		// check if IAM role exists
		AmazonIAMCommon iam = new AmazonIAMCommon(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		if(!iam.checkIAMRole(project.getIam_admin_role()))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid IAM role name").build();

		// generate project ID
		project.setProject_id(hf.newHasher().putLong(System.currentTimeMillis()).putLong(user_id).hash().toString());

		// first project
		User user = userService.read(user_id);
		String key = null;
		if(user.getManagementEC2InstanceId() == DaaSConstants.TEMP_MGMT_EC2_INSTANCE_ID) {
			// create keypair
			AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
			key = ec2.createEC2KeyPair(keypairName);
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
		project.setCloud_account_id(null);
		project.setCloud_secret_key(null);
		project.setIam_admin_role(null);

		// send keyPair in header, send null if not first project
		return Response.ok("Succesfully added Project").header("AWS_Key", key).entity(project).build();		
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(@CookieParam("daas-token") Cookie cookie, @PathParam("id") String id){

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
	@Path("/delete/{user_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProject(@CookieParam("daas-token") Cookie cookie, Project project, @PathParam("user_id") long user_id){

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();
		
		
		//Delete cluster
		String key = project.getAws_key();
		User user = userService.read(user_id);
		
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		ec2.deleteCluster(project, user.getManagementEC2InstanceId(),key);
		
		project = projectService.delete(project);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Succesfully deleted Project").entity(project).build();
	}

	/**
	 * Extension static method from /add REST API to create EC2 instance and create Kubernetes Cluster
	 * @param project
	 * @param userId
	 * @param orgName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */

	public static Project createFirstProject(Project project, Long userId, String orgName) throws IOException, InterruptedException{

		// share Kube common AMI with User's AWS account
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));
		ec2.shareAMIAcrossAccounts(amiId, project.getCloud_account_id());		

		// create EC2 Kube Management server on User's AWS account

		ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		String key = ec2.createEC2KeyPair(keypairName);
		project.setAws_key(key);
		
		String instanceId = ec2.createEC2Instance(amiId, instanceType, project.getIam_admin_role(), securityGroupName, keypairName, project.getProject_id(), String.valueOf(userId));
		
		// Create Kubernetes Cluster
		ec2.createCluster(project,instanceId, key,orgName);
		
		// Setting the Instance ID for User
		User user = userService.read(userId);
		user.setManagementEC2InstanceId(instanceId);
		userService.update(user);

		return project;
	}
}
