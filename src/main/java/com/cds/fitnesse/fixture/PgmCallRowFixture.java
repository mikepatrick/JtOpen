package com.cds.fitnesse.fixture;


import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
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

import fit.RowFixture;

public class PgmCallRowFixture extends RowFixture {
 
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
	private ArrayList<parmInfo> parmsInfo = null;
	private ProgramParameter[] parameterList = null;
	private String qualifiedProgramName;
	private byte[] linkparm;
	private boolean singleLinkparm = false;
	
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
		singleLinkparm = false;
		if (args.length == 0){
			return parmsInfo;
		}
		// Validate library, program name
		if (args.length < 4){
			return parmsInfo;
		}
		
		int numArgs = 0;
		
		try{
     		numArgs = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			return parmsInfo;
		}
		linkparm = new byte[500];
		int lpIndex = 0;
		int numCells = ((numArgs * 4) + 4);
		String libName = args[2];
		String pgmName = args[3];
		String useLinkparm = args[1];
		if (useLinkparm.equals("linkparm")){
			singleLinkparm = true;
		}
		qualifiedProgramName = "/QSYS.LIB/".concat(libName).concat(".LIB/").concat(pgmName).concat(".PGM");
		parmsInfo = new ArrayList<parmInfo>();
		
		for(int i = 4; i < numCells; ){
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
		        linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;
		        parmsInfo.get(i).byteLength = parameterList[i].getInputData().length;
		        parmsInfo.get(i).storedParm = new byte[parmsInfo.get(i).byteLength];
		        parmsInfo.get(i).storedParm = parameterList[i].getInputData();
		        
			}
			if(parmsInfo.get(i).dataType.equals(NUM)){
				AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
				parameterList[i] = new ProgramParameter(decParm.toBytes(new BigDecimal(parmsInfo.get(i).dataValue)));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).dataLength);
				linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;				
		        parmsInfo.get(i).byteLength = parameterList[i].getInputData().length;
		        parmsInfo.get(i).storedParm = new byte[parmsInfo.get(i).byteLength];
		        parmsInfo.get(i).storedParm = parameterList[i].getInputData();
			}
			if(parmsInfo.get(i).dataType.equals(ZON)){
				AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
				parameterList[i] = new ProgramParameter(zonParm.toBytes(new BigDecimal(parmsInfo.get(i).dataValue)));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).dataLength);
				linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;		
		        parmsInfo.get(i).byteLength = parameterList[i].getInputData().length;
		        parmsInfo.get(i).storedParm = new byte[parmsInfo.get(i).byteLength];
		        parmsInfo.get(i).storedParm = parameterList[i].getInputData();
			}
		}		
		return parmsInfo;
	}
	
	public byte[] concatenateLinkparm(byte[] linkparm, int offset, ProgramParameter progParm){
		int numBytes = progParm.getOutputDataLength();
		byte[] thisparm = progParm.getInputData();
		int parmSize = thisparm.length;
		System.arraycopy(thisparm, 0, linkparm, offset, parmSize);
		
		return linkparm;
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
	    	if(singleLinkparm){
	    		parameterList = new ProgramParameter[1];
	    		parameterList[0] = new ProgramParameter(linkparm);
	    	}
	        pgm.setProgram(qualifiedProgramName, parameterList);
	        // Run the program.
	        if (pgm.run() != true)
	        {
	            // Report failure.
	            System.out.println("Program failed - pgm.run() did not return true");
	            // Show the messages.
	            returnMsg = "";
	            AS400Message[] messagelist = pgm.getMessageList();
	         // for (int i = 0; i < messagelist.length; ++i)
	            for(int i = 0; i < parmsInfo.size(); i++)
	            {
	                // Show each message.
	                System.out.println(messagelist[i]);
	         //     returnMsg = returnMsg.concat(messagelist[i].getText());
	                parmsInfo.get(i).returnMessage = messagelist[i].getText();
	                
	            }
	            return parmsInfo;
	        }
	        // Else no error, get output data.
	        else
	        { 	
	        	if(singleLinkparm){
	        		int linkparmOffset = 0;
	        		int linkparmLength = parameterList[0].getOutputDataLength();
	        		byte[] retLinkparm = parameterList[0].getOutputData();
    				
    				
    				
    				
	        		for(int i = 0; i < parmsInfo.size(); i++){
	        			if (parmsInfo.get(i).dataType.equals(CHAR)){
	    					//AS400Text text = new AS400Text(parmsInfo.get(i).dataLength);
	        				//parmsInfo.get(i).dataValue = ((String) text.toObject(parameterList[i].getOutputData()));
	    					// linkparm work to do here to set outparm values
	        				int numBytes = parmsInfo.get(i).byteLength;
	        				byte[] thisparm = new byte[numBytes];
	        				AS400Text text = new AS400Text(numBytes);
	        				thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	        				parmsInfo.get(i).dataValue = ((String) text.toObject(Arrays.copyOfRange(thisparm, 0, numBytes)));
	        				linkparmOffset += numBytes;
	    				}
	    				if(parmsInfo.get(i).dataType.equals(NUM)){
	        				int numBytes = parmsInfo.get(i).byteLength;
	        				byte[] thisparm = new byte[numBytes];	    					
	    					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
	    					thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	    					parmsInfo.get(i).dataValue = (((BigDecimal) decParm.toObject(thisparm)).toString());
	    					//parmsInfo.get(i).dataValue = (((BigDecimal) decParm.toObject(parameterList[i].getOutputData())).toString());
	    					linkparmOffset += numBytes;
	    					
	    				}
	    				if(parmsInfo.get(i).dataType.equals(ZON)){
	        				int numBytes = parmsInfo.get(i).byteLength;
	        				byte[] thisparm = new byte[numBytes];	    					
	    					AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
	    					thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	    					parmsInfo.get(i).dataValue = (((BigDecimal) zonParm.toObject(thisparm)).toString());
	    					linkparmOffset += numBytes;
	    				}	        			
	        		}
	        	}else{
	        		for(int i = 0; i < parmsInfo.size(); i++){
	        			if (parmsInfo.get(i).dataType.equals(CHAR)){
	    					AS400Text text = new AS400Text(parmsInfo.get(i).dataLength);parmsInfo.get(i).dataValue = ((String) text.toObject(parameterList[i].getOutputData()));
	    				}
	    				if(parmsInfo.get(i).dataType.equals(NUM)){
	    					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
	    					parmsInfo.get(i).dataValue = (((BigDecimal) decParm.toObject(parameterList[i].getOutputData())).toString());
	    				}
	    				if(parmsInfo.get(i).dataType.equals(ZON)){
	    					AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).dataLength, parmsInfo.get(i).decimalLength);
	    					parmsInfo.get(i).dataValue = (((BigDecimal) zonParm.toObject(parameterList[i].getOutputData())).toString());
	    				}
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
	protected class parmInfo{
		public String dataName;
		public String dataType;
		public int dataLength;
		public String dataValue;
		public int decimalLength;
		public int byteLength;
		public String returnMessage;
		public byte[] storedParm;
		public parmInfo(String dataName, String dataType, String dataLength, String dataValue){
			this.dataName = dataName;
			this.dataType = dataType;
			this.returnMessage = "";
			this.byteLength = 0;
			if ((dataLength.contains(","))){
				String[] splitLength = dataLength.split(",");
				String totalLength = splitLength[0];
				String decimalLength = splitLength[1];
				this.dataLength = Integer.parseInt(totalLength);
				this.decimalLength = Integer.parseInt(decimalLength);
			}else{
				this.dataLength = Integer.parseInt(dataLength);
				this.decimalLength = 0;
			}

			this.dataValue = dataValue;
		}
	}
	@Override
	public Class<?> getTargetClass() {
		return parmInfo.class;
	}

	  @Override
	  public Object[] query() throws Exception {
		return runpgm().toArray();
	}
}

