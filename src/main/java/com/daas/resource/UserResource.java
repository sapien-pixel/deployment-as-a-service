package com.daas.resource;

import java.util.List;

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

import com.daas.model.Project;
import com.daas.model.User;
import com.daas.service.UserService;
import com.daas.service.impl.UserServiceImpl;
import com.daas.util.DaasUtil;

@Path("/user")
public class UserResource {

	private static UserService userService = new UserServiceImpl();

	
	@POST
	@Path("/signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addUser(User user){
		
		// check for null values
		
		System.out.println(user.toString());
		
		if(!DaasUtil.validEmail(user.getEmail()))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid email").build();
		
		user.setDateRegistered(System.currentTimeMillis());		
		user = userService.create(user);
		user.setPassword(null);
		return Response.ok("Succesfully added User").entity(user).build();
	}
	
	
	@GET
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(User user){
		
		// check for null values
		
		user = userService.validateUser(user);
		
		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid credentials").build();
		
		user.setPassword(null);
		return Response.ok("Succesfully logged in.").entity(user).build();	
	}
	
	
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(User user){
		
		// check for null values
		
		if(!DaasUtil.validEmail(user.getEmail()))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid email").build();
		
		user = userService.update(user);
		
		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user").build();
		
		user.setPassword(null);
		return Response.ok("Succesfully updated User info").entity(user).build();
	}
	
	
	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(User user){
		
		// check for null values
				
		user = userService.delete(user);
		
		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user").build();
		
		user.setPassword(null);
		return Response.ok("Succesfully deleted User").entity(user).build();
	}
	
	
	@GET
	@Path("/{user_id}/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllProjects(@PathParam("user_id") long user_id){
				
		// check for null values
		
		List<Project> projects = userService.getAllProjects(user_id);
		
		if(projects == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user id").build();
		
		return Response.ok("Success").entity(projects).build();		
	}
}
