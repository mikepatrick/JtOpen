package com.cds.fitnesse.fixture;


import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400PackedDecimal;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.AS400ZonedDecimal;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

import fitlibrary.SequenceFixture;

public class PgmCallFixture extends SequenceFixture {
 
	private static final String SERV = "SERV";
	private static final String CHAR = "CHAR";
	private static final String NUM = "NUM";
	private static final String ZON = "ZON";
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

	
	public String runpgm() throws Exception  {
		if (args.length == 0){
			return "No arguments passed";
		}
		// Validate library, program name
		if (args.length < 3){
			return "Not enough arguments found";
		}
		
		int numArgs = 0;
		
		try{
     		numArgs = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			return "First argument is # of parms and must be numeric.";
		}
		int numCells = (numArgs * 4);
		String libName = args[1];
		String pgmName = args[2];
		String qualifiedProgramName = "/QSYS.LIB/".concat(libName).concat(".LIB/").concat(pgmName).concat(".PGM");
		ArrayList<parmInfo> parmsInfo = new ArrayList<parmInfo>();
		
		for(int i = 3; i < numCells; ){
			parmsInfo.add(new parmInfo(args[i], args[i+1], args[i+2], args[i+3]));
			i = i+4;
		}
        // Set up the parameters.
        ProgramParameter[] parameterList = new ProgramParameter[parmsInfo.size()];
      		
		for(int i = 0; i < parmsInfo.size(); i++){
			parameterList[i] = new ProgramParameter(parmsInfo.get(i).dataLength);
			if (parmsInfo.get(i).dataType.equals(CHAR)){
		        AS400Text nametext = new AS400Text(parmsInfo.get(i).dataLength);
		        parameterList[i] = new ProgramParameter(nametext.toBytes(parmsInfo.get(i).dataValue));
		        parameterList[i].setOutputDataLength(parmsInfo.get(i).dataLength);
			}
			if(parmsInfo.get(i).dataType.equals(NUM)){
				AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, 0);
				parameterList[i] = new ProgramParameter(decParm.toBytes(new BigDecimal(parmsInfo.get(i).dataValue)));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).dataLength);
			}
			if(parmsInfo.get(i).dataType.equals(ZON)){
				AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).dataLength, 0);
				parameterList[i] = new ProgramParameter(zonParm.toBytes(new BigDecimal(parmsInfo.get(i).dataValue)));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).dataLength);				
			}			
		}
		
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
			return "Could not create AS400 object @ getAS400() - AS400SecurityException";
		}		
		
	    ProgramCall pgm = new ProgramCall(serv);
	    try
	    {
	        // Initialize the name of the program to run.
	        //String programName = "/QSYS.LIB/MPATRICK.LIB/TESTPROG.PGM";
	        // Set up the 3 parameters.
	        // ProgramParameter[] parameterList = new ProgramParameter[3];
	        // First parameter is to input a name.
	        //AS400Text nametext = new AS400Text(8);
	        //parameterList[0] = new ProgramParameter(nametext.toBytes("John Doe"));
	        // Second parameter is to get the answer, up to 50 bytes long.
	        //parameterList[1] = new ProgramParameter(50);
	        // Third parameter is to input a quantity and return a value up to 30 bytes long.
	        //byte[] quantity = new byte[2];
	        //quantity[0] = 1;  quantity[1] = 44;
	        //parameterList[2] = new ProgramParameter(quantity, 30);
	        // Set the program name and parameter list.
	    	
	        pgm.setProgram(qualifiedProgramName, parameterList);
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
	                returnMsg = returnMsg.concat(messagelist[i].getText());
	                
	            }
	            return returnMsg;
	        }
	        // Else no error, get output data.
	        else
	        { 
	        	
	        	
	    		for(int i = 0; i < parmsInfo.size(); i++){
	    			
	    			if (parmsInfo.get(i).dataType.equals(CHAR)){
	    		        AS400Text text = new AS400Text(parmsInfo.get(i).dataLength);
	    		        returnMsg = returnMsg.concat(parmsInfo.get(i).dataName).concat(": ");
	    		        returnMsg = returnMsg.concat((String) text.toObject(parameterList[i].getOutputData()));
	    		        returnMsg = returnMsg.concat(", ");
	    			}
	    			if(parmsInfo.get(i).dataType.equals(NUM)){
	    				AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, 0);
	    				returnMsg = returnMsg.concat(parmsInfo.get(i).dataName).concat(": ");
	    				returnMsg = returnMsg.concat(((BigDecimal) decParm.toObject(parameterList[i].getOutputData())).toString());
	    				returnMsg = returnMsg.concat(", ");
	    			}
	    			if(parmsInfo.get(i).dataType.equals(ZON)){
	    				AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).dataLength, 0);
	    				returnMsg = returnMsg.concat(parmsInfo.get(i).dataName).concat(": ");
	    				returnMsg = returnMsg.concat(((BigDecimal) zonParm.toObject(parameterList[i].getOutputData())).toString());
	    				returnMsg = returnMsg.concat(", ");	    				
	    			}	    			
	    		}	        	
	        	
	        	
	         //     AS400Text text = new AS400Text(10);
	         //     AS400PackedDecimal packedDec = new AS400PackedDecimal(15, 0);
	         //     String outData = (String) text.toObject(parameterList[0].getOutputData());
	         //     BigDecimal outData2 = (BigDecimal) packedDec.toObject(parameterList[1].getOutputData());
	         //     returnMsg = returnMsg.concat(outData);
	         //     returnMsg = returnMsg.concat(outData2.toString());
	         ////   returnMsg = returnMsg.concat(parameterList[0].getOutputData().toString());
	         ////  returnMsg = returnMsg.concat(parameterList[1].getOutputData().toString());
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
	private class parmInfo{
		public String dataName;
		public String dataType;
		public int dataLength;
		public String dataValue;
		
		public parmInfo(String dataName, String dataType, String dataLength, String dataValue){
			this.dataName = dataName;
			this.dataType = dataType;
			this.dataLength = Integer.parseInt(dataLength);
			this.dataValue = dataValue;
		}
	}
}

