package com.daas.aws.common.test;

import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.*;
import com.daas.common.ConfFactory;

public class AmazonEC2CommonTest {

	@Test
	public void createSecurityGroupTest() {

		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("dhruv.aws.accessId"), ConfFactory.getPrivateConf().getString("dhruv.aws.secretKey")));

		String amiId = ConfFactory.getConf().getString("ec2.common.amiId");
		String instanceType = ConfFactory.getConf().getString("ec2.kubeMS.instanceType");
		String securityGroupName = ConfFactory.getConf().getString("ec2.security.groupName");
		String keypairName = ConfFactory.getConf().getString("ec2.keypair.name");		
		String iamName = "kubernetes";

		String key = ec2.createEC2KeyPair(keypairName);
		System.out.println(key);

		ec2.createEC2Instance(amiId, instanceType, iamName, securityGroupName, keypairName);
	}

	@Test
	public void shareAMITest() {

		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));

		String amiId = ConfFactory.getConf().getString("ec2.common.amiId");		
		ec2.shareAMIAcrossAccounts(amiId, "022487948790");

	}

}