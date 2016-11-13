package com.daas.resource.test;

import java.io.IOException;

import org.junit.Test;

import com.daas.model.Project;
import com.daas.model.User;
import com.daas.resource.ProjectResource;

public class ProjectResourceTest {
	@Test
	public void createFirstProjectTest() {
		Project project = new Project();
		project.setCloud_access_key("AKIAIGVSMFWKLGICFCMA");
		project.setCloud_secret_key("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");
		project.setCloud_account_id("022487948790");
		project.setIam_admin_role("kubernetes");
		project.setProject_id("kubePrj-1");
		project.setMaster_size("t2.micro");
		project.setNode_size("t2.micro");
		project.setNode_numbers("1");
		project.setProject_id("awxwds");
		
		User usr = new User();
		usr.setUser_id(123);
		project.setUser_id(usr);
		
		try {
			ProjectResource.createFirstProject(project,"1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}