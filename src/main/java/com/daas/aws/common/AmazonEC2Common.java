package com.daas.aws.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;

/**
 * Base class for common EC2 activities(start,stop,getStatus, getLaunchTime)
 * @author vmaheshwari
 */
public class AmazonEC2Common {

	private AmazonEC2 ec2;
	private KeyPair keyPair;

	private static Logger log = LoggerFactory.getLogger(AmazonEC2Common.class.getName());

	/**
	 * Constructor to instantiate EC2 client.
	 * @param awsCredentials of type AWSCredentials - AWSAccessKeyID and AWSSecretKey
	 * @param region of type AWS Region e.g. - "ec2.us-west-1.amazonaws.com" for N. California
	 */
	public AmazonEC2Common(AWSCredentials awsCredentials) {
		ec2 = new AmazonEC2Client(awsCredentials);
		ec2.setEndpoint("ec2.us-west-1.amazonaws.com");
	}

	/**
	 * create a security group to be attached to the new Management Server EC2 instance creation
	 * 
	 */
	public void createSecurityGroup() {

		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();

		csgr.withGroupName("KubernetesSecGrp").withDescription("Security Group for Kubernetes Management Server");
		CreateSecurityGroupResult createSecurityGroupResult =
				ec2.createSecurityGroup(csgr);
		IpPermission ipPermission = new IpPermission();
		ipPermission.withIpRanges("0.0.0.0/0").withFromPort(22).withToPort(22).withIpProtocol("tcp");
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				new AuthorizeSecurityGroupIngressRequest();
		authorizeSecurityGroupIngressRequest.withGroupName("KubernetesSecGrp")
		.withIpPermissions(ipPermission);
		ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
	}

	/**
	 * Creates a Key pair to SSH the Management Server
	 * @return Private Key to the user
	 */

	public void createEC2KeyPair() {
		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
		createKeyPairRequest.withKeyName("KubernetesKey");
		CreateKeyPairResult createKeyPairResult =
				ec2.createKeyPair(createKeyPairRequest);
		KeyPair keyPair = new KeyPair();
		keyPair = createKeyPairResult.getKeyPair();
		this.keyPair = keyPair;
		System.out.println(this.keyPair);
	}

	/**
	 * Creates and launches Management Server on AWS
	 * @param instanceType
	 */
	public void createEC2Instance(String instanceType) {

		RunInstancesRequest runInstancesRequest =
			new RunInstancesRequest();
		
		runInstancesRequest.withImageId("ami-23e8a343")
			.withInstanceType(instanceType)
			.withMinCount(1)
			.withMaxCount(1)
			.withKeyName("KubernetesKey")
			.withSecurityGroups("KubernetesSecGrp")
			.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(""));
		
		RunInstancesResult runInstancesResult =
			      ec2.runInstances(runInstancesRequest);
	}

	/**
	 * start an ec2 instance
	 * @param instanceId
	 * 					instanceId to start	
	 */
	public void startEC2Instance(String instanceId){

		log.info("Starting instance "+ instanceId+".....");
		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instanceId);
		ec2.startInstances(request);

		Integer instanceState = -1;
		while(instanceState != 16) { //Loop until the instance is in the "running" state.
			instanceState = getInstanceStatus(instanceId);
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {}
		}
		log.info("Started the instance"+ instanceId+". Launch time is: "+ LocalDateTime.now());
	}

	/**
	 * stop an ec2 instance
	 * @param instanceId
	 * 					instanceId to stop
	 */
	public void stopEC2Instance(String instanceId){

		log.info("Stopping instance "+ instanceId+".....");
		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instanceId);
		ec2.stopInstances(request);

		Integer instanceState = -1;
		while(instanceState != 80) { //Loop until the instance is in the "stopped" state.
			instanceState = getInstanceStatus(instanceId);
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {}
		}
		log.info("Stopped the instance"+ instanceId+" at time - "+ LocalDateTime.now());
	}

	/**
	 * get list of status codes for multiple instances
	 * @param instanceIds
	 * 					  String array of instanceIds to get codes for
	 */
	public ArrayList<Integer> getInstanceStatus(String[] instanceIds) {

		ArrayList<Integer> instanceStateCodes = new ArrayList<Integer>();

		if(instanceIds!=null && instanceIds.length>0){
			for(String instanceId: instanceIds){
				instanceStateCodes.add(getInstanceStatus(instanceId));
			}
		}
		return instanceStateCodes;
	}

	/**
	 * get status code for a ec2 instance
	 * @param instanceId
	 * 					  instanceId to get status for
	 */
	public Integer getInstanceStatus(String instanceId) {

		DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
		InstanceState state = describeInstanceResult.getReservations().get(0).getInstances().get(0).getState();
		return state.getCode();
	}

	/**
	 * get launch time for a ec2 instance
	 * @param instanceId
	 * 					instanceId to get Launch time for
	 */
	public long getInstanceLaunchTime(String instanceId) {

		DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest); 
		Date date = describeInstanceResult.getReservations().get(0).getInstances().get(0).getLaunchTime();		
		return date.getTime();
	}

}