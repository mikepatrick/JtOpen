package com.cds.fitnesse.fixture;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;

import fitlibrary.DoFixture;
import fitlibrary.SequenceFixture;

public class CmdCallFixture extends SequenceFixture {

	private static final String SERV = "SERV";
	protected String applicationName = null;
	protected Properties dbProperties = null;
	//private static final String url = "jdbc:as400://serv.cdsfulfillment.com/;user=WWWAUTOT;password=cds999;transaction isolation=none;errors=full;";
	//private static final String driverName = "com.ibm.as400.access.AS400JDBCDriver";
	private CdsAS400Connection dbConn = null;
	private String returnMsg = "";
	private String dbFile = "db.properties";
	
	public AS400 getAS400(String sys, String user, String password) throws IOException, AS400SecurityException {
		AS400 serv = new AS400(sys, user, password);
		serv.connectService(AS400.COMMAND);
		return serv;
	}
	
	// Made this public so SbmJobFixture could inherit it.
	// Move this into a utility class that both fixtures can import.
	public Connection getJDBCConnection(String driverName, String driverUrl, String userName, String password) throws Exception
	{
		Class driverClass = Class.forName(driverName);
		return DriverManager.getConnection(driverUrl, userName, password);		  
	}
	
	public String runcmd(String command) throws Exception  {

		dbConn = new CdsAS400Connection(dbFile);
		try {
			Connection conn = getJDBCConnection(dbConn.getDriverName(), dbConn.getDataSource(), dbConn.getUser(), dbConn.getPassword());
		} catch (Exception e1) {
			e1.printStackTrace();
			return "Obtaining Connection failed @ getJDBCConnection()";
		}
		AS400 serv = null;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());			
//		} catch (IOException e) {
//			e.printStackTrace();
//			return "Could not create AS400 object @ getAS400() - IOException";
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "Could not create AS400 object @ getAS400() - AS400SecurityException";
		}		
		CommandCall cmd = new CommandCall(serv, command);
		 
		try{
		
			if (cmd.run() != true){
				System.out.println("Command failed - cmd.run() did not return true");	
			
			}
			
			AS400Message[] messagelist = cmd.getMessageList();
			returnMsg = "";
			for (int i = 0; i < messagelist.length; ++i){
        
            // Show each message.
				System.out.println(messagelist[i].getText());
				returnMsg = returnMsg.concat(messagelist[i].getText());
			}	 
			//return returnMsg;
			return returnMsg;
		}
		catch (Exception e)
		{
	        System.out.println("Command " + cmd.getCommand() + " issued an exception!");
	        e.printStackTrace();	
	        return "Command failed";
		}			
	}
	public String loginUserPassword(String dataSource, String userName, String password){
		
		if (!(dataSource.isEmpty())){
			this.dbConn.setDataSource(dataSource);
		}
		this.dbConn.setUser(userName);
		this.dbConn.setPassword(password);
		dbConn = new CdsAS400Connection(dbFile);
		try {
			Connection conn = getJDBCConnection(dbConn.getDriverName(), dbConn.getDataSource(), dbConn.getUser(), dbConn.getPassword());
		} catch (Exception e1) {
			e1.printStackTrace();
			return "Obtaining Connection failed @ getJDBCConnection()";
		}
		AS400 serv = null;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not create AS400 object @ getAS400() - IOException";
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "Could not create AS400 object @ GETas400() - AS400SecurityException";
		}				
		return "Credentials changed";
	}
	
}
