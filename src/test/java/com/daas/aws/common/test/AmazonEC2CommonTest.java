package com.daas.aws.common.test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.daas.aws.common.*;
import com.daas.common.ConfFactory;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.SSH;

public class AmazonEC2CommonTest {

	@Test
	public void createSecurityGroupTest() throws IOException, InterruptedException {
		
		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("dhruv.aws.accessId"), ConfFactory.getPrivateConf().getString("dhruv.aws.secretKey")));

		String amiId = ConfFactory.getConf().getString("ec2.common.amiId");
		String instanceType = ConfFactory.getConf().getString("ec2.kubeMS.instanceType");
		String securityGroupName = ConfFactory.getConf().getString("ec2.security.groupName");
		String keypairName = ConfFactory.getConf().getString("ec2.keypair.name");		
		String iamName = "kubernetes";

		String key = ec2.createEC2KeyPair(keypairName);
		System.out.println(key);

		String ip = ec2.createEC2Instance(amiId, instanceType, iamName, securityGroupName, keypairName, "prj-123","usr-123");
		sshToEC2(ip, key);
	}

	@Test
	public void shareAMITest() {

		AmazonEC2Common ec2 = new AmazonEC2Common(new BasicAWSCredentials(ConfFactory.getPrivateConf().getString("vivek.aws.accessId"), ConfFactory.getPrivateConf().getString("vivek.aws.secretKey")));

		String amiId = ConfFactory.getConf().getString("ec2.common.amiId");		
		ec2.shareAMIAcrossAccounts(amiId, "022487948790");

	}
	
	public void sshToEC2(String ip, String key) throws IOException, InterruptedException {
		
		System.out.println(ip);
		TimeUnit.MINUTES.sleep(2);
		Shell shell = new SSH(ip, 22, "ec2-user", key);
		String stdout = new Shell.Plain(shell).exec("echo 'KUBERNETES_PROVIDER=aws' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'MASTER_SIZE=t2.micro' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'NODE_SIZE=t2.nano' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'NUM_NODES=1' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'AWS_S3_BUCKET=${RANDOM}-kube' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'KUBE_AWS_INSTANCE_PREFIX=projectid' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'NUM_NODES=1' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("echo 'USER_ID=userid' >> ~/.bashrc");
		stdout = new Shell.Plain(shell).exec("sudo service docker restart");
		stdout = new Shell.Plain(shell).exec("docker run -e KUBERNETES_PROVIDER=$KUBERNETES_PROVIDER -e MASTER_SIZE=$MASTER_SIZE -e NODE_SIZE=$NODE_SIZE -e NUM_NODES=$NUM_NODES -e AWS_S3_BUCKET=$AWS_S3_BUCKET -e KUBE_AWS_INSTANCE_PREFIX=$KUBE_AWS_INSTANCE_PREFIX -e USER_ID=$USER_ID dhruvkalaria/kubernetes-docker-daas /bin/bash -c 'bash kubernetes/cluster/kube-up.sh'");
	}

}