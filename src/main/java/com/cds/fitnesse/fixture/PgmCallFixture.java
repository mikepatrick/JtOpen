package com.cds.fitnesse.fixture;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

import fitlibrary.DoFixture;

public class PgmCallFixture extends DoFixture {

	private static final String SERV = "SERV";
	protected String applicationName = null;
	protected Properties dbProperties = null;
	private static final String url = "jdbc:as400://serv.cdsfulfillment.com/;user=WWWAUTOT;password=cds999;transaction isolation=none;errors=full;";
	//private static final String driverName = "com.ibm.as400.access.AS400JDBCDriver";
	private CdsAS400Connection dbConn = null;
	private String returnMsg = "";
	private String dbFile = "db.properties";
	
	public AS400 getAS400(String sys, String user, String password) throws IOException, AS400SecurityException {
		AS400 serv = new AS400(sys, user, password);
		serv.connectService(AS400.COMMAND);
		
		return serv;
	}
	
	private Connection getJDBCConnection(String driverName, String driverUrl, String userName, String password) throws Exception
	{
		Class driverClass = Class.forName(driverName);
		return DriverManager.getConnection(driverUrl, userName, password);		  
	}

	
	public String runpgm(String command) throws Exception  {
	/*	dbProperties = getDbProperties();
		driverName = dbProperties.getProperty("database.driver");
		dataSource = dbProperties.getProperty("database.url");
		userName = dbProperties.getProperty("database.user");
		password = dbProperties.getProperty("database.password");  */
		dbConn = new CdsAS400Connection(dbFile);
		try {
			Connection conn = getJDBCConnection(dbConn.getDriverName(), dbConn.getDataSource(), dbConn.getUser(), dbConn.getPassword());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "Obtaining Connection failed @ getJDBCConnection()";
		}
		AS400 serv = null;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Could not create AS400 object @ getAS400() - IOException";
		} catch (AS400SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Could not create AS400 object @ GETas400() - AS400SecurityException";
		}		
		
	    ProgramCall pgm = new ProgramCall(serv);
	    try
	    {
	        // Initialize the name of the program to run.
	        String programName = "/QSYS.LIB/TESTLIB.LIB/TESTPROG.PGM";
	        // Set up the 3 parameters.
	        ProgramParameter[] parameterList = new ProgramParameter[3];
	        // First parameter is to input a name.
	        AS400Text nametext = new AS400Text(8);
	        parameterList[0] = new ProgramParameter(nametext.toBytes("John Doe"));
	        // Second parameter is to get the answer, up to 50 bytes long.
	        parameterList[1] = new ProgramParameter(50);
	        // Third parameter is to input a quantity and return a value up to 30 bytes long.
	        byte[] quantity = new byte[2];
	        quantity[0] = 1;  quantity[1] = 44;
	        parameterList[2] = new ProgramParameter(quantity, 30);
	        // Set the program name and parameter list.
	        pgm.setProgram(programName, parameterList);
	        // Run the program.
	        if (pgm.run() != true)
	        {
	            // Report failure.
	            System.out.println("Program failed - pgm.run() did not return true");
	            // Show the messages.
	            returnMsg = "";
	            AS400Message[] messagelist = pgm.getMessageList();
	            for (int i = 0; i < messagelist.length; ++i)
	            {
	                // Show each message.
	                System.out.println(messagelist[i]);
	                returnMsg.concat(messagelist[i].getText());
	                
	            }
	            return returnMsg;
	        }
	        // Else no error, get output data.
	        else
	        {
	            AS400Text text = new AS400Text(50);
	            System.out.println(text.toObject(parameterList[1].getOutputData()));
	            System.out.println(text.toObject(parameterList[2].getOutputData()));
	            returnMsg.concat(parameterList[1].getOutputData().toString());
	            returnMsg.concat(parameterList[2].getOutputData().toString());
	            return returnMsg;
	        }
	    }
	    catch (Exception e)
	    {
	        System.out.println("Program " + pgm.getProgram() + " issued an exception!");
	        e.printStackTrace();
	        return "Program " + pgm.getProgram() + " issued an exception!";
	        
	    }
	    // Done with the system.
	    // serv.disconnectAllServices();		
	}

}
