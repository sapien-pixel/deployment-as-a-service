package com.daas.aws.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

public class AmazonSTSCommon {

	AWSSecurityTokenServiceClient sts_client;

	public AmazonSTSCommon(AWSCredentials awsCredentials){		
		sts_client = new AWSSecurityTokenServiceClient(awsCredentials);		
	}

	
	public String getUserAWSAccountId(){

		GetCallerIdentityRequest getCallerIdentityRequest = new GetCallerIdentityRequest();

		GetCallerIdentityResult getCallerIdentityResult = sts_client.getCallerIdentity(getCallerIdentityRequest);	
		return getCallerIdentityResult.getAccount();		
	}

}
