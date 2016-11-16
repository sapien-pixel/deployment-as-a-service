package com.daas.aws.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

/**
 * Base class for common STS activities
 * @author Vivek
 */
public class AmazonSTSCommon {

	AWSSecurityTokenServiceClient sts_client;

	/**
	 * Instantiate AWS STS
	 * @param awsCredentials
	 * 							Credential to AWS
	 */
	public AmazonSTSCommon(AWSCredentials awsCredentials){		
		sts_client = new AWSSecurityTokenServiceClient(awsCredentials);		
	}

	/**
	 * Get the User's AWS account ID
	 * @return
	 */
	public String getUserAWSAccountId(){

		GetCallerIdentityRequest getCallerIdentityRequest = new GetCallerIdentityRequest();

		GetCallerIdentityResult getCallerIdentityResult = sts_client.getCallerIdentity(getCallerIdentityRequest);	
		return getCallerIdentityResult.getAccount();		
	}

}
