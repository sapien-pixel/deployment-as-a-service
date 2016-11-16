package com.daas.aws.common.test;

import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.AmazonSTSCommon;
import com.daas.common.ConfFactory;

public class AmazonSTSCommonTest {

	@Test
	public void getUserAccountId(){

		AmazonSTSCommon sts = new AmazonSTSCommon(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));
		System.out.println(sts.getUserAWSAccountId());
	}

}
