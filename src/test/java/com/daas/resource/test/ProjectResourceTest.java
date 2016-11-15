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
		project.setCloud_access_key("AKIAI7PDFDGMIA4K3MLQ");
		project.setCloud_secret_key("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");
		project.setCloud_account_id("022487948790");
		project.setIam_admin_role("kubernetes");
		project.setProject_id("kubePrj-4");
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