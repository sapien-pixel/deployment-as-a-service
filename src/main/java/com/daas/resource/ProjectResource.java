package com.daas.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.AmazonEC2Common;
import com.daas.aws.common.AmazonIAMCommon;
import com.daas.common.ConfFactory;
import com.daas.model.Project;
import com.daas.service.ProjectService;
import com.daas.service.UserService;
import com.daas.service.impl.ProjectServiceImpl;
import com.daas.service.impl.UserServiceImpl;
import com.daas.util.DaasUtil;
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
	public Response addProject(Project project, @PathParam("user_id") String user_id) throws Exception{

		if(user_id==null || user_id.isEmpty() || user_id == "")
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user id").build();

		// check if valid user
		if(!userService.userExists(Long.valueOf(user_id)))
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
		project.setProject_id(hf.newHasher().putLong(System.currentTimeMillis()).putLong(Long.valueOf(user_id)).hash().toString());
		
		
		// check if this is User's first project
		// If it is first project, create EC2 Kube MS first
		// if not. start another kubernetes container

		List<Project> projects = userService.getAllProjects(Long.valueOf(user_id));
		
		// first project
		if(projects.size()==0){

			project = createFirstProject(project);			
			
		}
		
		
		
		else{
			
			
			
		}
		
		// create Kube cluster 



		// update User with MS url or instance ID?


		project.setUser_id(userService.read(Long.valueOf(user_id)));
		project.setDateCreated(System.currentTimeMillis());
		project = projectService.create(project);

		// set keys null
		project.setCloud_access_key(null);
		project.setCloud_account_id(null);
		project.setCloud_secret_key(null);
		project.setIam_admin_role(null);

		return Response.ok("Succesfully added Project").entity(project).build();		
	}


	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(@PathParam("id") long id){

		Project project = projectService.read(id);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Success").entity(project).build();			
	}


	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(Project project){

		project = projectService.update(project);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Succesfully updated Project info").entity(project).build();
	}


	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProject(Project project){

		project = projectService.delete(project);

		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();

		return Response.ok("Succesfully deleted Project").entity(project).build();
	}


	public static Project createFirstProject(Project project){

		// share Kube common AMI with User's AWS account
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));
		ec2.shareAMIAcrossAccounts(amiId, project.getCloud_account_id());		

		// create EC2 Kube Management server on User's AWS account
		ec2 = new AmazonEC2Common(new BasicAWSCredentials(project.getCloud_access_key(), project.getCloud_secret_key()));
		String key = ec2.createEC2KeyPair(keypairName);
		project.setAws_key(key);
		ec2.createEC2Instance(amiId, instanceType, project.getIam_admin_role(), securityGroupName, keypairName);

		return project;
	}

}
