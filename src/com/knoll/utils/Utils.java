/**
 * 
 */
package com.knoll.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Methods for I-Series JDE and EBS. 
 * @author jfavara
 *
 */
public class Utils {

	/**
	 *  This method will send an email and optionally attach a file.
	 *  @param To Email Address
	 *  @param Subject Text
	 *  @param Body Text 
	 *  @param file attachment (optional). File attachment must include path
	 *  
	 */

	public void sendEmail(ArrayList<String> toEmails, String subjectText, String bodyText, ArrayList<String> fileName) {

		//Recipient's email ID needs to be mentioned.		

		Address [] addresses = new Address[ toEmails.size() ];
		for( int i = 0; i < toEmails.size(); i++ ) {
			try {
				addresses[ i ] = new InternetAddress( toEmails.get( i ));
			} catch( AddressException ae ) {
				try { addresses[ i ] = new InternetAddress( "noreply@knoll.com" ); } catch( Exception e ) {}
			}
		}
		// Sender's email ID needs to be mentioned
		String from = "noreply@knoll.com";

		// Assuming you are sending email from localhost
		String host = "smtp.knoll.com";

		// Get system properties
		Properties properties = System.getProperties();


		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try{ 
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.

			message.addRecipients(Message.RecipientType.TO, addresses );

			// Set Subject: header field
			message.setSubject(subjectText.trim());

			// Fill the message
			// message.setText("This is actual message");

			// Part two is attachment
			MimeBodyPart  messageBodyPart = new MimeBodyPart();
			MimeBodyPart  messageBodyPart1 = new MimeBodyPart();
			messageBodyPart1.setText(bodyText.trim());

			Multipart multipart = new MimeMultipart(); 
			multipart.addBodyPart(messageBodyPart1);

			// if file name is passed send as attachment

			if (fileName.size() > 0){

				for (String temp : fileName){
					
					File file = new File(temp); 
					DataSource source = new FileDataSource(file);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(temp);
					multipart.addBodyPart(messageBodyPart);

				}

			}

			message.setContent(multipart);
			// Send message
			Transport.send(message);
			System.out.println("Sent email message successfully....");
		}catch (MessagingException mex) {
			System.out.println("Oh no... error sending email.. OH what to do...");
			mex.printStackTrace();
		}

	}

	/**
	 * This method returns a connection to the I-Series. Note the I-Series address is kept in a property file named setup in folder Config
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public Connection getIseriesConnection() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {

		Connection AS400con = null;
		String DRIVER = "com.ibm.as400.access.AS400JDBCDriver";

		Properties props = new Properties();
		props.load(new FileInputStream("Config/setup.properties"));
		System.out.println(props.getProperty("ISERIES"));
		System.out.println(props.getProperty("PASSWORD"));
		System.out.println(props.getProperty("USER"));
		System.out.println(props.getProperty("IURL"));

		Class.forName(DRIVER);
		AS400con = DriverManager.getConnection(props.getProperty("IURL"), props.getProperty("USER"),
				props.getProperty("PASSWORD"));

		return AS400con;

	}

	/**
	 * This method returns a connection to the Oracle EBS system. 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException 
	 */

	public Connection getOracleConnection () throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {

		Connection Oraclecon = null;
		String DRIVER = "oracle.jdbc.driver.OracleDriver";

		Properties props = new Properties();
		props.load(new FileInputStream("Config/setup.properties"));
		System.out.println(props.getProperty("OHOST"));
		System.out.println(props.getProperty("OPORT"));
		System.out.println(props.getProperty("OSERVICENAME"));
		System.out.println(props.getProperty("OUSER"));
		System.out.println(props.getProperty("OPASSWORD"));
		String client = "jdbc:oracle:thin:" + props.getProperty("OHOST") + ":" + props.getProperty("OPORT") + ":"  + props.getProperty("OSERVICENAME");
		System.out.println("Oracle connection is " + client);

		Class.forName(DRIVER);

		Oraclecon = DriverManager.getConnection(
				client,props.getProperty("OUSER"),props.getProperty("OPASSWORD"));

		return Oraclecon;

	}
	
	/**
	 * This method accepts a month in number form and returns the three character alpha month: 01 = JAN.
	 * Month can't be deteremined then null is returned. 
	 * 
	 * @param month
	 * @alpha month 
	 */
	public String getAlphaMonth (String month){

		String monthString;

		switch (Integer.parseInt(month)) {

		case 1: monthString = "JAN";
		break;

		case 2: monthString = "FEB";
		break;

		case 3: monthString = "MAR";
		break;

		case 4: monthString = "APR";
		break;

		case 5: monthString = "MAY";
		break;

		case 6: monthString = "JUN";
		break;

		case 7: monthString = "JUL";
		break;

		case 8: monthString = "AUG";
		break;

		case 9: monthString = "SEP";
		break;

		case 10: monthString = "OCT";
		break;

		case 11: monthString = "NOV";
		break;

		case 12: monthString = "DEC";
		break;

		default: monthString = null;
		break;

		}

		return monthString;

	}


}
