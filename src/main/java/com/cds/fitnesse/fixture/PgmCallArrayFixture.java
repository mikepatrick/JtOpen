package com.cds.fitnesse.fixture;


import java.beans.PropertyVetoException;
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
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

import fitlibrary.ArrayFixture;

public class PgmCallArrayFixture extends ArrayFixture {
 
	private static final String SERV = "SERV";
	private static final String CHAR = "CHAR";
	private static final String NUM = "NUM";
	protected String applicationName = null;
	protected Properties dbProperties = null;
	private static final String url = "jdbc:as400://serv.cdsfulfillment.com/;user=WWWAUTOT;password=cds999;transaction isolation=none;errors=full;";
	//private static final String driverName = "com.ibm.as400.access.AS400JDBCDriver";
	private CdsAS400Connection dbConn = null;
	private String returnMsg = "";
	private String dbFile = "db.properties";
	private ArrayList<parmInfo> parmsInfo = null;
	private ProgramParameter[] parameterList = null;
	private String qualifiedProgramName;
	
	public PgmCallArrayFixture () throws Exception{
		super.getArgs();
		parmsInfo = runpgm();
	}
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
	private ArrayList<parmInfo> setUpParameters() throws PropertyVetoException{
		if (args.length == 0){
			return parmsInfo;
		}
		// Validate library, program name
		if (args.length < 3){
			return parmsInfo;
		}
		
		int numArgs = 0;
		
		try{
     		numArgs = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			return parmsInfo;
		}
		int numCells = (numArgs * 4);
		String libName = args[1];
		String pgmName = args[2];
		qualifiedProgramName = "/QSYS.LIB/".concat(libName).concat(".LIB/").concat(pgmName).concat(".PGM");
		parmsInfo = new ArrayList<parmInfo>();
		
		for(int i = 3; i < numCells; ){
			parmsInfo.add(new parmInfo(args[i], args[i+1], args[i+2], args[i+3]));
			i = i+4;
		}
        // Set up the parameters.
        parameterList = new ProgramParameter[parmsInfo.size()];
      		
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
		}		
		return parmsInfo;
	}
	
	public ArrayList<parmInfo> runpgm() throws Exception  {
		
		parmsInfo = setUpParameters();
		
		dbConn = new CdsAS400Connection(dbFile);
		try {
			Connection conn = getJDBCConnection(dbConn.getDriverName(), dbConn.getDataSource(), dbConn.getUser(), dbConn.getPassword());
		} catch (Exception e1) {
			e1.printStackTrace();
			return parmsInfo;
		}
		AS400 serv = null;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
			return parmsInfo;
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return parmsInfo;
		}		
		
	    ProgramCall pgm = new ProgramCall(serv);
	    try
	    {   	
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
	            return parmsInfo;
	        }
	        // Else no error, get output data.
	        else
	        { 	
	    		for(int i = 0; i < parmsInfo.size(); i++){
	    			
	    			if (parmsInfo.get(i).dataType.equals(CHAR)){
	    		        AS400Text text = new AS400Text(parmsInfo.get(i).dataLength);
	    		     
	    		        parmsInfo.get(i).dataValue = ((String) text.toObject(parameterList[0].getOutputData()));
	    		       
	    			}
	    			if(parmsInfo.get(i).dataType.equals(NUM)){
	    				AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, 0);
	    				
	    				parmsInfo.get(i).dataValue = (((BigDecimal) decParm.toObject(parameterList[1].getOutputData())).toString());
	    			
	    			}
	    		}	        	
	        	
	    		return parmsInfo;
	        }
	    }
	    catch (Exception e)
	    {
	        System.out.println("Program " + pgm.getProgram() + " issued an exception!");
	        e.printStackTrace();
	        return parmsInfo;
	        
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
	@Override
	public Class<?> getTargetClass() {
		return parmInfo.class;
	}

}

