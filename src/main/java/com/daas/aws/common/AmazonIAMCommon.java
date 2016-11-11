package com.daas.aws.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;

public class AmazonIAMCommon {

	private AmazonIdentityManagement iam;

	private static Logger log = LoggerFactory.getLogger(AmazonEC2Common.class.getName());

	/**
	 * Instantiate AWS IAM 
	 * @param awsCredentials
	 * 						Credential to AWS
	 */
	public AmazonIAMCommon(AWSCredentials awsCredentials) {
		iam = new AmazonIdentityManagementClient(awsCredentials);
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
