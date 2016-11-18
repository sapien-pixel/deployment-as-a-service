package com.daas.aws.common;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

/**
 * Base class for SES operations
 * @author Sakshi
 */
public class AmazonSESCommon {
	
	private AmazonSimpleEmailServiceClient sesClient;
	
	/**
	 * Constructor to instantiate SES client 
	 * @param awsCredentials of type AWSCredentials - AWSAccessKey and AWSSecretKey
	 * @param region of type AWS Region e.g. - "US_WEST_2" 
	 */
	public AmazonSESCommon(AWSCredentials awsCredentials) {
		sesClient = new AmazonSimpleEmailServiceClient(awsCredentials);
		Region region = Region.getRegion(Regions.US_WEST_2);
		sesClient.setRegion(region);	
	}
	
	public void sendEmailNotification(String FROM, String TO, String BODY, String SUBJECT) {
		
		Destination destination = new Destination().withToAddresses(new String[]{TO});
    	Content subject = new Content().withData(SUBJECT);
        Content textBody = new Content().withData(BODY); 
        Body body = new Body().withText(textBody);
        
        
        Message message = new Message().withSubject(subject).withBody(body);
        
        SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);
        
        try{
        	System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
        	        	
        	Region REGION = Region.getRegion(Regions.US_WEST_2);
            sesClient.setRegion(REGION);
       
            // Send the email.
            sesClient.sendEmail(request);  
            System.out.println("Email sent!");
        }catch (Exception ex) 
        {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
        
        
	}

}
