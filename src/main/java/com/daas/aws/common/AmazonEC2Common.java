package com.daas.aws.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.codec.binary.Base64;

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
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.LaunchPermission;
import com.amazonaws.services.ec2.model.LaunchPermissionModifications;
import com.amazonaws.services.ec2.model.ModifyImageAttributeRequest;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

/**
 * Base class for common EC2 activities(create, start, stop, getStatus, getLaunchTime)
 * @author vmaheshwari
 */
public class AmazonEC2Common {

	private AmazonEC2 ec2;
	private KeyPair keyPair;

	private static Logger log = LoggerFactory.getLogger(AmazonEC2Common.class.getName());

	/**
	 * Constructor to instantiate EC2 client.
	 * @param awsCredentials of type AWSCredentials - AWSAccessKeyID and AWSSecretKey
	 * @param region of type AWS Region e.g. - "ec2.us-west-1.amazonaws.com" for Oregon
	 */
	public AmazonEC2Common(AWSCredentials awsCredentials) {
		ec2 = new AmazonEC2Client(awsCredentials);
		ec2.setEndpoint("ec2.us-west-2.amazonaws.com");
	}

	/**
	 * create a security group to be attached to the new Management Server EC2 instance creation
	 * @param securityGroupName
	 * 								Name of security group
	 */
	public void createSecurityGroup(String securityGroupName) {

		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();

		csgr.withGroupName(securityGroupName).withDescription("Security Group for Kubernetes Management Server");
		CreateSecurityGroupResult createSecurityGroupResult =
				ec2.createSecurityGroup(csgr);
		IpPermission ipPermission = new IpPermission();
		ipPermission.withIpRanges("0.0.0.0/0").withFromPort(22).withToPort(22).withIpProtocol("tcp");
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				new AuthorizeSecurityGroupIngressRequest();
		authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
		.withIpPermissions(ipPermission);
		ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
	}

	/**
	 * Creates a Key pair to SSH the Management Server
	 * @param keyPairName
	 * 							Name of key apir
	 * @return Private Key to the user
	 */
	public String createEC2KeyPair(String keyPairName) {
		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();
		createKeyPairRequest.withKeyName(keyPairName);
		CreateKeyPairResult createKeyPairResult =
				ec2.createKeyPair(createKeyPairRequest);
		KeyPair keyPair = new KeyPair();
		keyPair = createKeyPairResult.getKeyPair();
		this.keyPair = keyPair;
		return keyPair.getKeyMaterial();
	}

	/**
	 * Creates and launches Management Server on AWS
	 * @param amiId
	 * 							AMI id
	 * @param instanceType
	 * 							Instance type
	 * @param iamName
	 * 							Name of IAM role
	 * @param securityGroupName
	 * 							Name of securtiy group
	 * @param keyPairName
	 * 							Name of keypair 
	 * @throws IOException 
	 */
	public String createEC2Instance(String amiId, String instanceType, String iamName, String securityGroupName, String keyPairName, String projectId, String userId) throws IOException {

		// create security group
		createSecurityGroup(securityGroupName);
		
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		
		runInstancesRequest.withImageId(amiId)
			.withInstanceType(instanceType)
			.withMinCount(1)
			.withMaxCount(1)
			.withKeyName(keyPairName)
			.withSecurityGroups(securityGroupName)
			.withUserData(new String(Base64.encodeBase64(readFile("/cmd.sh", projectId, userId).getBytes())))
			.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(iamName));
		System.out.println(new String(Base64.encodeBase64(readFile("/cmd.sh", projectId, userId).getBytes())));
		RunInstancesResult runInstancesResult =
			      ec2.runInstances(runInstancesRequest);
		
		log.info("Created the instance with AMI ID - "+ amiId);
		
		Instance instance = runInstancesResult.getReservation().getInstances().get(0);		
		Integer instanceState = -1;
		
		while(instanceState != 16) { //Loop until the instance is in the "running" state.
			instanceState = getInstanceStatus(instance.getInstanceId());
			try {
				Thread.sleep(5000);
			} catch(InterruptedException e) {}
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {}
		
		DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instance.getInstanceId());
		DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
		String ip = describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
		
		log.info("Started the instance"+ instance.getInstanceId()+". Launch time is: "+ instance.getLaunchTime());
		log.info("Public IP address is -> " + ip);
		return ip;
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
	
	/**
	 * Share an AMI with an user account
	 * @param amiId
	 * 					AMI id
	 * @param userAccountId
	 * 					AWS User Account Id to share with
	 */
	public void shareAMIAcrossAccounts(String amiId, String userAccountId){
		
		Collection<LaunchPermission> launchPermission = new ArrayList<LaunchPermission>();
		launchPermission.add(new LaunchPermission().withUserId(userAccountId));
		
		LaunchPermissionModifications launchPermissionModifications = new LaunchPermissionModifications().withAdd(launchPermission);
		
		ModifyImageAttributeRequest modifyImageAttributeRequest = new ModifyImageAttributeRequest()
		.withImageId(amiId).withLaunchPermission(launchPermissionModifications);
		
		ec2.modifyImageAttribute(modifyImageAttributeRequest);
		
		log.info("Shared AMI with ID - "+ amiId+" with user account ID - "+ userAccountId);
	}
	
	public  String readFile(String fileName, String projectId, String userId) throws IOException {
	    BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fileName)));
	    
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        String content = sb.toString();
	        content = content.replace("userid",userId);
	        content = content.replace("projectid",projectId);
	        return content;
	    } finally {
	        br.close();
	    }
	}
}