package com.daas.aws.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;

/**
 * Base class for common IAM activities
 * @author Vivek
 */
public class AmazonIAMCommon {

	private AmazonIdentityManagement iam;

	private static Logger log = LoggerFactory.getLogger(AmazonIAMCommon.class.getName());

	/**
	 * Instantiate AWS IAM 
	 * @param awsCredentials
	 * 						Credential to AWS
	 */
	public AmazonIAMCommon(AWSCredentials awsCredentials) {
		iam = new AmazonIdentityManagementClient(awsCredentials);
	}

	/**
	 * Create a IAM role with Administrator access
	 * @param roleName
	 * 						Name of role
	 * @return true if successfully created role (or already exists), false if not
	 */
	public boolean createAdminAccessIAMRole(String roleName){

		try{

			if(checkIAMRole(roleName)){
				log.info("IAM role with name "+ roleName+ "already exists!");
				return true;
			}

			String document = "{\"Version\": \"2012-10-17\",\"Statement\": [{\"Effect\": \"Allow\",\"Principal\": {\"Service\": \"ec2.amazonaws.com\"},\"Action\": \"sts:AssumeRole\"}]}";

			// first create role
			log.info("Creating IAM role with name "+ roleName);
			CreateRoleRequest createRoleRequest = new CreateRoleRequest()
			.withRoleName(roleName)
			.withAssumeRolePolicyDocument(document);

			iam.createRole(createRoleRequest);
			log.info("Created IAM role with name "+ roleName);
						
			// Now, attach admin access policy
			log.info("Attaching admin policy to role "+ roleName);
			AttachRolePolicyRequest attachRolePolicyRequest = new AttachRolePolicyRequest()
			.withPolicyArn("arn:aws:iam::aws:policy/AdministratorAccess")		
			.withRoleName(roleName);

			iam.attachRolePolicy(attachRolePolicyRequest);
			log.info("Attached admin policy to role "+ roleName);
			
			// now create instance profile
			log.info("Creating instance profile with name "+ roleName);
			CreateInstanceProfileRequest createInstanceProfileRequest = new CreateInstanceProfileRequest()
			.withInstanceProfileName(roleName);

			iam.createInstanceProfile(createInstanceProfileRequest);
			log.info("Created instance profile with name "+ roleName);			
			
			// Lastly, attach this instance profile to the role
			log.info("Adding role " + roleName+ " to instance profile "+ roleName);
			AddRoleToInstanceProfileRequest addRoleToInstanceProfileRequest = new AddRoleToInstanceProfileRequest()
			.withInstanceProfileName(roleName)
			.withRoleName(roleName);

			iam.addRoleToInstanceProfile(addRoleToInstanceProfileRequest);
			log.info("Added role " + roleName+ " to instance profile "+ roleName);

			log.info("Successfully Created role "+ roleName + " with administrator access.");			
			return true;			
		} catch(Exception e){			
			e.printStackTrace();
			log.error("Could not create/get IAM role.");
			return false;
		}
	}

	/**
	 * Check if IAM role exists.
	 * @param iamRoleName
	 * 						Name of IAM role
	 * @return true if exists, false if not.
	 */
	public boolean checkIAMRole(String iamRoleName) {

		GetRoleRequest getRoleRequest = new GetRoleRequest().withRoleName(iamRoleName);

		try{
			iam.getRole(getRoleRequest);			
		}
		catch(NoSuchEntityException  e){
			log.info("No IAM role exists with Name -"+ iamRoleName);
			return false;
		}
		return true;
	}	

}
