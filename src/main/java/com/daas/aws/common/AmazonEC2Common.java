package com.daas.aws.common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

/**
 * Base class for common EC2 activities(start,stop,getStatus, getLaunchTime)
 * @author vmaheshwari
 */
public class AmazonEC2Common {

	private final static AmazonEC2 ec2 = new AmazonEC2Client();

	private static Logger log = LoggerFactory.getLogger(AmazonEC2Common.class.getName());


	/**
	 * start an ec2 instance
	 * @param instanceId
	 * 					instanceId to start	
	 */
	public static void startEC2Instance(String instanceId){

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
	public static void stopEC2Instance(String instanceId){

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
	public static ArrayList<Integer> getInstanceStatus(String[] instanceIds) {

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
	public static Integer getInstanceStatus(String instanceId) {

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
	public static long getInstanceLaunchTime(String instanceId) {

		DescribeInstancesRequest describeInstanceRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult describeInstanceResult = ec2.describeInstances(describeInstanceRequest); 
		Date date = describeInstanceResult.getReservations().get(0).getInstances().get(0).getLaunchTime();		
		return date.getTime();
	}

}
