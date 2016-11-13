package com.daas.resource;

import java.util.Calendar;
import java.util.List;

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
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daas.common.DaaSConstants;
import com.daas.model.Project;
import com.daas.model.User;
import com.daas.service.UserService;
import com.daas.service.impl.UserServiceImpl;
import com.daas.util.DaasUtil;
import com.daas.util.JWTUtil;

@Path("/user")
public class UserResource {

	private static Logger log = LoggerFactory.getLogger(UserResource.class.getName());	
	private static UserService userService = new UserServiceImpl();


	@POST
	@Path("/signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signup(User user) throws Exception {
		
		log.info("New request to add a User with email - "+ user.getEmail());

		//set managementEC2InstanceId as temp to avoid Input Invalid Exception
		user.setManagementEC2InstanceId(DaaSConstants.TEMP_MGMT_EC2_INSTANCE_ID);
		// check for null values
		DaasUtil.checkForNull(user);

		if(!DaasUtil.validEmail(user.getEmail()))
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid email").build();

		user.setDateRegistered(System.currentTimeMillis());		
		user = userService.create(user);
		user.setPassword(null);
		return Response.ok("Succesfully added User").entity(user).build();
	}


	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(User user){
		
		log.info("New request to login a User with email - "+ user.getEmail());

		user = userService.validateUser(user);

		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid credentials").build();

		user.setPassword(null);

		// get a JWT token
		Cookie cookie = new Cookie("daas-token", JWTUtil.createJWT(String.valueOf(user.getUser_id()), DaaSConstants.JWT_ISSUER, user.getEmail(), -1));
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);				// set cookie expiry to 1 year

		// get cookie
		NewCookie newCookie = new NewCookie(cookie, "", 315360000, cal.getTime(), false, true);
		return Response.ok("Succesfully logged in.").cookie(newCookie).entity(user).build();	
	}


	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateUser(@CookieParam("daas-token") Cookie cookie, User user){

		log.info("New request to update a User with email - "+ user.getEmail());

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}
		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);	

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

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
	public Response deleteUser(@CookieParam("daas-token") Cookie cookie, User user){

		log.info("New request to delete a User with email - "+ user.getEmail());

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}
		
		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);	

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		user = userService.delete(user);

		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user").build();

		user.setPassword(null);
		return Response.ok("Succesfully deleted User").entity(user).build();
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_PLAIN)
	public Response logout(@CookieParam("daas-token") Cookie cookie) {

		// This removes the cookie in the browser.
		if (cookie != null) {
			NewCookie newCookie = new NewCookie(cookie, null, 0, false);
			return Response.ok("Succesfully logout").cookie(newCookie).build();
		}

		return Response.ok("OK - No session").build();
	}


	@GET
	@Path("/{user_id}/projects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllProjects(@CookieParam("daas-token") Cookie cookie, @PathParam("user_id") long user_id){

		log.info("New request to get All projects of User with id - "+ user_id);

		if (cookie == null) {
			return Response.serverError().entity("ERROR").build();
		}

		// validate jwt
		String token = cookie.getValue();
		boolean validToken = JWTUtil.parseJWT(token);

		if(!validToken)
			return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").build();

		User user = userService.read(user_id);
		if(user==null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid User").build();

		List<Project> projects = userService.getAllProjects(user_id);

		if(projects == null)
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid user id").build();

		return Response.ok("Success").entity(projects).build();		
	}
}
