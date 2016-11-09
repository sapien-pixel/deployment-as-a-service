package com.daas.aws.common.test;

import org.junit.Test;
import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.*;

public class AmazonEC2CommonTest {
	
	@Test
	public void createSecurityGroupTest() {
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials("", ""));
		ec2.createSecurityGroup();
		ec2.createEC2KeyPair();
		ec2.createEC2Instance("t2.micro");
	}
}