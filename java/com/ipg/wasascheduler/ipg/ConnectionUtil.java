package com.ipg.wasascheduler.ipg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class ConnectionUtil {
	
	static Connection connection;
	
	static public void  init()
	{
		try {

			java.security.Security
            .addProvider(new com.ibm.jsse.IBMJSSEProvider());
		String myDriver = "com.mysql.jdbc.Driver";
		String myUrl = "jdbc:mysql://localhost/billpayment";
		Class.forName(myDriver);
		connection = DriverManager.getConnection(myUrl, "root", "abcd@12345");
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}
	}

	static public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection conn) {
		this.connection = conn;
	}

}
