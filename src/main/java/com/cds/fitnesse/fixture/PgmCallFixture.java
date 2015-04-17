package com.cds.fitnesse.fixture;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.cds.fitnesse.utils.ParmInfo;
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
	protected Properties dbProperties = null;
	private CdsAS400Connection dbConn = null;
	private String returnMsg = "";
	private String dbFile = "db.properties";
	
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
		ArrayList<ParmInfo> parmsInfo = new ArrayList<ParmInfo>();
		
		for(int i = 3; i < numCells; ){
			parmsInfo.add(new ParmInfo(args[i], args[i+1], args[i+2], args[i+3]));
			i = i+4;
		}
        // Set up the parameters.
        ProgramParameter[] parameterList = new ProgramParameter[parmsInfo.size()];
      		
		for(int i = 0; i < parmsInfo.size(); i++){
			parameterList[i] = new ProgramParameter(parmsInfo.get(i).getDataLength());
			if (parmsInfo.get(i).getDataType().equals(CHAR)){
		        AS400Text nametext = new AS400Text(parmsInfo.get(i).getDataLength());
		        parameterList[i] = new ProgramParameter(nametext.toBytes(parmsInfo.get(i).getDataValue()));
		        parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());
			}else{
				if(parmsInfo.get(i).getDataType().equals(NUM)){
					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
					parameterList[i] = new ProgramParameter(decParm.toBytes(new BigDecimal(parmsInfo.get(i).getDataValue())));
					parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());
				}else{
					if(parmsInfo.get(i).getDataType().equals(ZON)){
						AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
						parameterList[i] = new ProgramParameter(zonParm.toBytes(new BigDecimal(parmsInfo.get(i).getDataValue())));
						parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());				
					}
				}
			}
		}
		
		dbConn = new CdsAS400Connection(dbFile);

		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
	    ProgramCall pgm = new ProgramCall(serv);
	    try
	    {    	
	        pgm.setProgram(qualifiedProgramName, parameterList);
	        // Run the program.
	        if (pgm.run() != true)
	        {
	            System.out.println("Program failed - pgm.run() did not return true");
	            returnMsg = "";
	            AS400Message[] messagelist = pgm.getMessageList();
	            for (int i = 0; i < messagelist.length; ++i)
	            {
	                System.out.println(messagelist[i]);
	                returnMsg = returnMsg.concat(messagelist[i].getText());     
	            }
	            return returnMsg;
	        } else {	
	    		for(int i = 0; i < parmsInfo.size(); i++){
	    			
	    			if (parmsInfo.get(i).getDataType().equals(CHAR)){
	    		        AS400Text text = new AS400Text(parmsInfo.get(i).getDataLength());
	    		        returnMsg = returnMsg.concat(parmsInfo.get(i).getDataName()).concat(": ");
	    		        returnMsg = returnMsg.concat((String) text.toObject(parameterList[i].getOutputData()));
	    		        returnMsg = returnMsg.concat(", ");
	    			}else{
	    				if(parmsInfo.get(i).getDataType().equals(NUM)){
	    					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    					returnMsg = returnMsg.concat(parmsInfo.get(i).getDataName()).concat(": ");
	    					returnMsg = returnMsg.concat(((BigDecimal) decParm.toObject(parameterList[i].getOutputData())).toString());
	    					returnMsg = returnMsg.concat(", ");
	    				}else{
	    					if(parmsInfo.get(i).getDataType().equals(ZON)){
	    						AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    						returnMsg = returnMsg.concat(parmsInfo.get(i).getDataName()).concat(": ");
	    						returnMsg = returnMsg.concat(((BigDecimal) zonParm.toObject(parameterList[i].getOutputData())).toString());
	    						returnMsg = returnMsg.concat(", ");
	    					}
	    				}
	    			}	    			
	    		}
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

