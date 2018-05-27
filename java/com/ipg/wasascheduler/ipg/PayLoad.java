package com.ipg.wasascheduler.ipg;

import ipgclient2.CShroff2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;



import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import java.security.Security;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@EnableScheduling


@ComponentScan("com.ipg.wasascheduler.ipg")

public class PayLoad {

	@Autowired
	ConnectionUtil connectionUtil;
	
	@Autowired
	Response res;

	String ReturnURL = "https://wasabillpay.bracbank.com/wasa/decryptSchedule.php";
	String REST_SERVICE_URL = "http://ipaytest.bracbank.com:8080/ipg/servlet_pay";
	static Connection connection;
	
	@Scheduled(fixedRate = 60000)
	public void fixedRateJob() {

		try {
			java.security.Security
            .addProvider(new com.ibm.jsse.IBMJSSEProvider());
			Class.forName("com.mysql.jdbc.Driver");
			connection= DriverManager.getConnection(  
					"jdbc:mysql://127.0.0.1:3306/billpayment?useSSL=false","root","abcd@12345");  
			String query = "SELECT * FROM bp_transactionlog WHERE bpt_transtatus='PROCESSING'";

			Statement st = connection.createStatement();

			ResultSet rs = st.executeQuery(query);

			while (rs.next()) {

				repeatIteration(rs);
			}
			st.close();
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}

	}
	
	@Scheduled(fixedRate = 86400000)
	public void fixedRateJob() {

		try {
			String query = "SELECT * FROM bp_transactionlog where bpt_tran_status='PROCESSING'";

			Statement st = ConnectionUtil.getConnection().createStatement();

			ResultSet rs = st.executeQuery(query);
			int finishedTotal = 0;
			int underProcessingTotal = 0;
			int unSuccessfulTotal = 0;
			while (rs.next()) {

				if (rs.getString("bpt_status").equals("PROCESSING"))
					underProcessingTotal++;
				else if (rs.getString("bpt_status").equals("UNSUCCESSFUL"))
					unSuccessfulTotal++;
				else
					finishedTotal++;
			}

			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Calendar cal = Calendar.getInstance();
			System.out.println(dateFormat.format(cal)); // 2016/11/16 12:08:43

			String mailBody = "Todays " + dateFormat.format(cal) + " status of Proccessings: "

					+ System.getProperty("line.separator") + System.getProperty("line.separator")

					+ "Under process : " + underProcessingTotal + System.getProperty("line.separator")
					+ "Unsuccessful process: " + unSuccessfulTotal + System.getProperty("line.separator")
					+ System.getProperty("line.separator") + "Finished process: " + finishedTotal;

			SendEmailUsingGMailSMTP.sendMail(emailAddress, "Today's Status", mailBody);

			st.close();
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}

	}

	private void repeatIteration(ResultSet rs) throws SQLException {	
		System.out.println(rs.getString("bpt_transtatus"));
		if (rs.getString("bpt_transtatus").equals("PROCESSING")) {
			int Result = -1;
			String ErrorMessage = "";

			String EInvoice = "";
			String PTInvoice = "";

			String Action = "SaleTxnVerify";
			String MerchantID = "wasa";
			String MerRefID = rs.getString("bpt_mer_ref_id");
			System.out.println(MerRefID);

			PTInvoice = "<req>" + "<mer_id>" + MerchantID + "</mer_id>" + "<mer_txn_id>" + MerRefID + "</mer_txn_id>"
					+ "<action>" + Action + "</action>";

			if ((ReturnURL != null) && (ReturnURL.length() > 0)) {
				PTInvoice = PTInvoice + "<ret_url>" + ReturnURL + "</ret_url>";
			}

			PTInvoice = PTInvoice + "</req>";
			java.security.Security
            .addProvider(new com.ibm.jsse.IBMJSSEProvider());
			CShroff2 cShroff2 = new CShroff2("C:\\Merchant\\IPG\\keys\\", "C:\\Merchant\\IPG\\logs\\");
			Result = cShroff2.getErrorCode();
			if (Result == 0) {
				Result = cShroff2.setPlainTextInvoice(PTInvoice);
				if (Result == 0) {
					EInvoice = cShroff2.getEncryptedInvoice();
				} else {
					ErrorMessage = cShroff2.getErrorMsg();
				}
			} else {
				ErrorMessage = cShroff2.getErrorMsg();
			}
			if (Result >= 0) {
				Client client;

				client = ClientBuilder.newClient();
				Form form = new Form();
				form.param("encryptedInvoicePay", EInvoice);

				client.target(REST_SERVICE_URL).request(javax.ws.rs.core.MediaType.APPLICATION_XML)
						.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
			}
		}
	}

	@Bean
	public TaskScheduler poolScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setThreadNamePrefix("poolScheduler");
		scheduler.setPoolSize(10);
		return scheduler;
	}

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PayLoad.class);
		context.start();
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			//context.close();
		}
	}
}
