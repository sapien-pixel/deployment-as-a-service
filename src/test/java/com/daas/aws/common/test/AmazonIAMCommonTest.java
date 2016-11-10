package com.daas.aws.common.test;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.AmazonIAMCommon;
import com.daas.common.ConfFactory;

public class AmazonIAMCommonTest {

	@Test
	public void checkIAMExists() {
		
		AmazonIAMCommon iam = new AmazonIAMCommon(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));

		Assert.assertTrue("Not Exists", iam.checkIAMRole("kubernetes"));
	}
	
}
