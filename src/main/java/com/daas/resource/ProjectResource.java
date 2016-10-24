package com.daas.resource;

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
import com.daas.service.ProjectService;
import com.daas.service.impl.ProjectServiceImpl;


@Path("/project")
public class ProjectResource {

	private static ProjectService projectService = new ProjectServiceImpl();
	
	
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProject(Project project){
		
		// check for null values
		
		project.setDateCreated(System.currentTimeMillis());
		project = projectService.create(project);
		return Response.ok("Succesfully added Project").entity(project).build();		
	}
	
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProject(@PathParam("id") long id){
		
		// check for null values
		
		Project project = projectService.read(id);
		
		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		return Response.ok("Success").entity(project).build();			
	}
	
	
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProject(Project project){
		
		// check for null values
				
		project = projectService.update(project);
		
		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		return Response.ok("Succesfully updated Project info").entity(project).build();
	}
	
	
	@DELETE
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProject(Project project){
		
		// check for null values
				
		project = projectService.delete(project);
		
		if(project==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid project").build();
		
		return Response.ok("Succesfully deleted Project").entity(project).build();
	}
	
	
}
