package com.daas.aws.common;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
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
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;


/**
 * Base class for common EC2 activities(create, start, stop, getStatus, getLaunchTime)
 * @author Vivek
 * @author Dhruv
 */
public class AmazonEC2Common {

	private AmazonEC2 ec2;

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
		ec2.createSecurityGroup(csgr);
		ec2.authorizeSecurityGroupIngress(createIpPermissions(securityGroupName, 22));
		ec2.authorizeSecurityGroupIngress(createIpPermissions(securityGroupName, 1883));
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
		log.info("Created key pair " + keyPairName);
		System.out.println(keyPair.getKeyMaterial());
		return keyPair.getKeyMaterial();
	}
	
	
	/**
	 * Creates a volume for persistant storage stateful applications
	 * @param volumeSize
	 * @return
	 * @throws InterruptedException
	 */
	public String createVolume(String volumeSize) throws InterruptedException {
		CreateVolumeRequest request = new  CreateVolumeRequest(Integer.valueOf(volumeSize),"us-west-2a");
		CreateVolumeResult volumeResponse = ec2.createVolume(request);
		Thread.sleep(10000);
		return volumeResponse.getVolume().getVolumeId();
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
	 * @throws InterruptedException 
	 */
	public String createEC2Instance(String amiId, String instanceType, String iamName, String securityGroupName, String keyPairName, String projectId, String userId) throws IOException, InterruptedException {

		// create security group
		createSecurityGroup(securityGroupName);

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId(amiId)
		.withInstanceType(instanceType)
		.withMinCount(1)
		.withMaxCount(1)
		.withKeyName(keyPairName)
		.withSecurityGroups(securityGroupName)
		.withIamInstanceProfile(new IamInstanceProfileSpecification().withName(iamName));

		RunInstancesResult runInstancesResult =
				ec2.runInstances(runInstancesRequest);

		log.info("Created the instance with AMI ID - "+ amiId);

		Instance instance = runInstancesResult.getReservation().getInstances().get(0);		
		Integer instanceState = -1;

		while(instanceState != 16) { //Loop until the instance is in the "running" state.			
			log.info("Waiting for instance to be in running state...");
			instanceState = getInstanceStatus(instance.getInstanceId());
				Thread.sleep(5000);
		}
		
		// Buffer wait 
		Thread.sleep(3000); 
		
		log.info("Started the instance"+ instance.getInstanceId()+". Launch time is: "+ instance.getLaunchTime());
		return instance.getInstanceId();
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
	
	/**
	 * Creates a Kuberntes Cluster by SSHing inside the machine and executing commands
	 * Synchronous Method does not return until whole script is executed
	 * 
	 * @param project
	 * @param instanceId
	 * @param key
	 * @param orgName
	 */
	
	public void createCluster(com.daas.model.Project project, String instanceId, String key, String orgName, String mosquittoHostIP) {
		String publicIP = getPublicIp(instanceId);

		try {
			Thread.sleep(80000);
			Shell shell = new SSH(publicIP, 22, "ec2-user", key);
			List<String> cmdString = new ArrayList<String>();
			cmdString.add("echo 'KUBERNETES_PROVIDER=aws' >> ~/.bashrc");
			cmdString.add("echo 'MOSQUITTO_HOST=MOST' >> ~/.bashrc".replace("MOST",mosquittoHostIP));
			cmdString.add("echo 'MASTER_SIZE=MRSE' >> ~/.bashrc".replace("MRSE", project.getMaster_size()));
			cmdString.add("echo 'NODE_SIZE=NESE' >> ~/.bashrc".replace("NESE", project.getNode_size()));
			cmdString.add("echo 'NUM_NODES=XN' >> ~/.bashrc".replace("XN",project.getNode_numbers()));
			cmdString.add("echo 'AWS_S3_BUCKET=AWBT' >> ~/.bashrc".replace("AWBT", project.getProject_id()+"kube_"+"s3"));
			cmdString.add("echo 'KUBE_AWS_INSTANCE_PREFIX=KUIX' >> ~/.bashrc".replace("KUIX", project.getProject_id()));
			cmdString.add("echo 'USER_ID=UID' >> ~/.bashrc".replace("UID", String.valueOf(project.getUser_id().getUser_id())));
			cmdString.add("echo 'ORG_NAME=ONE' >> ~/.bashrc".replace("ONE", orgName));
			cmdString.add("source ~/.bashrc");
			cmdString.add("sleep 2");
			cmdString.add("sudo service mosquitto restart");
			cmdString.add("sleep 4");
			cmdString.add("source /home/ec2-user/kubernetes/cluster/kube-up.sh");
			executeSSHCommand(shell, cmdString);
		} catch (UnknownHostException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes the Kubernetes Cluster based on the KUBE_INSTANCE_PREFIX value set
	 * @param project
	 * @param instanceId
	 * @param key
	 * @param orgName
	 */
	
	public void deleteCluster(com.daas.model.Project project, String instanceId, String key) {
		String publicIP = getPublicIp(instanceId);
		
		try {
			Shell shell = new SSH(publicIP, 22, "ec2-user", key);
			List<String> cmdString = new ArrayList<String>();
			cmdString.add("echo 'KUBE_AWS_INSTANCE_PREFIX=KUIX' >> ~/.bashrc".replace("KUIX", project.getProject_id()));
			cmdString.add("source ~/.bashrc");
			cmdString.add("sleep 2");
			cmdString.add("source /home/ec2-user/kubernetes/cluster/kube-down.sh");
			executeSSHCommand(shell, cmdString);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the Public IP of the EC2 instance based on the instance id
	 * @param instanceId
	 * @return
	 */
	
	public String getPublicIp(String instanceId) {
		DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest);
		return describeInstanceResult.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
	}
	
	/**
	 * Creates a SSH connection and executes the command
	 * @param shell
	 * @param cmdString - Shell Command to Execute
	 */
	
	public static void executeSSHCommand(Shell shell, List<String> cmdString) {
		for(String cmd: cmdString) {
			try {
				new Shell.Plain(shell).exec(cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens Ingress port to the security group
	 * @param securityGroupName
	 * @param port
	 * @return AuthorizeSecurityGroupIngressRequest Object to be passed to AmazonEC2 object
	 */
	
	public static AuthorizeSecurityGroupIngressRequest createIpPermissions(String securityGroupName, int port) {
		IpPermission ipPermission = new IpPermission();
		ipPermission.withIpRanges("0.0.0.0/0").withFromPort(port).withToPort(port).withIpProtocol("tcp");
		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
				new AuthorizeSecurityGroupIngressRequest();
		authorizeSecurityGroupIngressRequest.withGroupName(securityGroupName)
		.withIpPermissions(ipPermission);
		log.info("Created security group " + securityGroupName);
		return authorizeSecurityGroupIngressRequest;
	}
}