package com.daas.resource.test;

import java.io.IOException;

import org.junit.Test;

import com.daas.model.Project;
import com.daas.model.User;
import com.daas.resource.ProjectResource;

public class ProjectResourceTest {
	@Test
	public void createFirstProjectTest() throws InterruptedException {
		Project project = new Project();
		project.setCloud_access_key("");
		project.setCloud_secret_key("");
		project.setProject_id("kubePrj-4");
		project.setCloud_access_key("");
		project.setCloud_secret_key("");
		project.setProject_id("kubePrj-1");
		project.setMaster_size("t2.micro");
		project.setNode_size("t2.micro");
		project.setNode_numbers("1");
		project.setProject_id("damnit987");
		
		User usr = new User();
		usr.setUser_id(111222);
		project.setUser_id(usr);
		
		try {
			ProjectResource.createFirstProject(project,123L,"foo");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}