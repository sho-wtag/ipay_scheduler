package com.ipg.wasascheduler.ipg;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 * 
 * @author Sumeeth Kumar
 */
public class SendEmailUsingGMailSMTP {
   public static void main(String[] args) {
	   sendMail("anish424@gmail.com","test","to test");
   }
   
  // public static void sendMail(String to,String from,final String username,final String password, String subject,String text )
   public static void sendMail(String to, String subject,String text )
   {
	   String from = "loginframework.sumeeth@gmail.com";//change accordingly
	      final String username = "loginframework.sumeeth@gmail.com";//change accordingly
	      final String password = "sumeeth.kumar";//change accordingly

	   String host = "smtp.gmail.com";

	      Properties props = new Properties();
	      props.put("mail.smtp.auth", "true");
	      props.put("mail.smtp.starttls.enable", "false");
	      
	      props.put("mail.smtp.starttls.enable","true");
	      props.put("mail.smtp.ssl.trust","theRemoteSmtpServer");
	      props.put("mail.smtp.host", host);
	      props.put("mail.smtp.port", "587");
	      props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

	      // Get the Session object.
	      Session session = Session.getInstance(props,
	      new javax.mail.Authenticator() {
	         protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(username, password);
	         }
	      });

	      try {
	         // Create a default MimeMessage object.
	         Message message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set To: header field of the header.
	         message.setRecipients(Message.RecipientType.TO,
	         InternetAddress.parse(to));

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Now set the actual message
	         message.setText(text);

	         // Send message
	         Transport.send(message);

	         System.out.println("Sent message successfully....");

	      } catch (MessagingException e) {
	            throw new RuntimeException(e);
	      }
   }
}