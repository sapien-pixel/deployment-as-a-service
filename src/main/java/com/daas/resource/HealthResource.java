package com.daas.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/health")
public class HealthResource {
	
	private static Logger log = LoggerFactory.getLogger(HealthResource.class.getName());
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response Health(){		
		log.info("API Health check");
		return Response.ok("Healthy").build();
	}
}
