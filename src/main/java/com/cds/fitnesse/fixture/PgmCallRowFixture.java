package com.cds.fitnesse.fixture;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.ParmInfo;

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
 
	public enum dataType {
		CHAR("CHAR"), NUM("NUM"), ZON("ZON");
		private String dType;
		private dataType(String s){
			this.dType = s;
		}
		public String toString(){
			return this.dType;
		}
	}
	private static final String SERV = "SERV";
	protected String applicationName = null;
	protected Properties dbProperties = null;
	private CdsAS400Connection dbConn = null;
	private String dbFile = "db.properties";
	private ArrayList<ParmInfo> parmsInfo = null;
	private ProgramParameter[] parameterList = null;
	private String qualifiedProgramName;
	private byte[] linkparm;
	private boolean singleLinkparm = false;
	
	public AS400 getAS400(String sys, String user, String password) throws IOException, AS400SecurityException {
		AS400 serv = new AS400(sys, user, password);
		serv.connectService(AS400.COMMAND);
		
		return serv;
	}

	private ArrayList<ParmInfo> setUpParameters() throws PropertyVetoException{
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
		parmsInfo = new ArrayList<ParmInfo>();
		
		for(int i = 4; i < numCells; ){
			parmsInfo.add(new ParmInfo(args[i], args[i+1], args[i+2], args[i+3]));
			i = i+4;
		}

        parameterList = new ProgramParameter[parmsInfo.size()];
      		
		for(int i = 0; i < parmsInfo.size(); i++){
			parameterList[i] = new ProgramParameter(parmsInfo.get(i).getDataLength());
			dataType dt = dataType.valueOf(parmsInfo.get(i).getDataType());
			switch (dt){
			case CHAR:
		        AS400Text nametext = new AS400Text(parmsInfo.get(i).getDataLength());
		        parameterList[i] = new ProgramParameter(nametext.toBytes(parmsInfo.get(i).getDataValue()));
		        parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());
		        linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;
		        parmsInfo.get(i).setByteLength(parameterList[i].getInputData().length);
		        parmsInfo.get(i).setStoredParm(new byte[parmsInfo.get(i).getByteLength()]);
		        parmsInfo.get(i).setStoredParm(parameterList[i].getInputData());
		        break;
			case NUM:   
				AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
				parameterList[i] = new ProgramParameter(decParm.toBytes(new BigDecimal(parmsInfo.get(i).getDataValue())));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());
				linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;				
		        parmsInfo.get(i).setByteLength(parameterList[i].getInputData().length);
		        parmsInfo.get(i).setStoredParm(new byte[parmsInfo.get(i).getByteLength()]);
		        parmsInfo.get(i).setStoredParm(parameterList[i].getInputData());
		        break;
			case ZON:
				AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
				parameterList[i] = new ProgramParameter(zonParm.toBytes(new BigDecimal(parmsInfo.get(i).getDataValue())));
				parameterList[i].setOutputDataLength(parmsInfo.get(i).getDataLength());
				linkparm = concatenateLinkparm(linkparm, lpIndex, parameterList[i]);
		        lpIndex += parameterList[i].getInputData().length;		
		        parmsInfo.get(i).setByteLength(parameterList[i].getInputData().length);
		        parmsInfo.get(i).setStoredParm(new byte[parmsInfo.get(i).getByteLength()]);
		        parmsInfo.get(i).setStoredParm(parameterList[i].getInputData());
				break;		
			}
		}		
		return parmsInfo;
	}
	
	public byte[] concatenateLinkparm(byte[] linkparm, int offset, ProgramParameter progParm){
		
		byte[] thisparm = progParm.getInputData();
		int parmSize = thisparm.length;
		System.arraycopy(thisparm, 0, linkparm, offset, parmSize);
		
		return linkparm;
	}
	public ArrayList<ParmInfo> runpgm() throws Exception  {
		
		parmsInfo = setUpParameters();
		dbConn = new CdsAS400Connection(dbFile);

		AS400 serv = null;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
			parmsInfo.get(0).setReturnMessage(e.toString());
			return parmsInfo;
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			parmsInfo.get(0).setReturnMessage(e.toString());
			return parmsInfo;
		}		
		
	    ProgramCall pgm = new ProgramCall(serv);
	    
	    try
	    {   
	    	if(singleLinkparm){
	    		parameterList = new ProgramParameter[1];
	    		parameterList[0] = new ProgramParameter(linkparm, linkparm.length);
	    	}
	    	
	        pgm.setProgram(qualifiedProgramName, parameterList);

	        if (pgm.run() != true)
	        {
	            System.out.println("Program failed - pgm.run() did not return true");
	            AS400Message[] messagelist = pgm.getMessageList();

	            for(int i = 0; i < parmsInfo.size(); i++)
	            {
	                System.out.println(messagelist[i]);
	                parmsInfo.get(i).setReturnMessage(messagelist[i].getText());    
	            }
	            return parmsInfo;
	        } else { 	
	        	if(singleLinkparm){
	        		int linkparmOffset = 0;
	        		byte[] retLinkparm = parameterList[0].getOutputData();
	        		int numBytes = 0;
	        		byte[] thisparm;
	        		
	        		for(int i = 0; i < parmsInfo.size(); i++){
	        			dataType dt = dataType.valueOf(parmsInfo.get(i).getDataType());
	        			switch (dt) {
	        			case CHAR:
	        				numBytes = parmsInfo.get(i).getByteLength();
	        				thisparm = new byte[numBytes];
	        				AS400Text text = new AS400Text(numBytes);
	        				thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	        				String charParm = ((String) text.toObject(Arrays.copyOfRange(thisparm, 0, numBytes)));
	        				//trim() to make validation in a RowFixture table simpler
	        				parmsInfo.get(i).setDataValue(charParm.trim());
	        				linkparmOffset += numBytes;
	        				break;
//	    				
	        			case NUM:
	        				numBytes = parmsInfo.get(i).getByteLength();
	        				thisparm = new byte[numBytes];	    					
	    					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    					thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	    					parmsInfo.get(i).setDataValue((((BigDecimal) decParm.toObject(thisparm)).toString()));
	    					linkparmOffset += numBytes;
	    					break;
	    					
	        			case ZON:
	        				numBytes = parmsInfo.get(i).getByteLength();
	        				thisparm = new byte[numBytes];	    					
	    					AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    					thisparm = Arrays.copyOfRange(retLinkparm, linkparmOffset, linkparmOffset + numBytes);
	    					parmsInfo.get(i).setDataValue((((BigDecimal) zonParm.toObject(thisparm)).toString()));
	    					linkparmOffset += numBytes;
	    					break; 
	        			}
	        		}
	        	}else{
	        		for(int i = 0; i < parmsInfo.size(); i++){
	        			dataType dt = dataType.valueOf(parmsInfo.get(i).getDataType());
	        			switch (dt) {
	        			case CHAR:
	    					AS400Text text = new AS400Text(parmsInfo.get(i).getDataLength());
	    					parmsInfo.get(i).setDataValue(((String) text.toObject(parameterList[i].getOutputData())));
	    					break;
	        			case NUM:
	    					AS400PackedDecimal decParm = new AS400PackedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    					parmsInfo.get(i).setDataValue((((BigDecimal) decParm.toObject(parameterList[i].getOutputData())).toString()));
	    					break;	
	    				case ZON:
	    					AS400ZonedDecimal zonParm = new AS400ZonedDecimal(parmsInfo.get(i).getDataLength(), parmsInfo.get(i).getDecimalLength());
	    					parmsInfo.get(i).setDataValue((((BigDecimal) zonParm.toObject(parameterList[i].getOutputData())).toString()));
	    					break;
	        			}
	    			}
	        	}
	        	serv.disconnectAllServices();
	    		return parmsInfo;
	        }
	    }
	    catch (Exception e)
	    {
	        System.out.println("Program " + pgm.getProgram() + " issued an exception!");
	        parmsInfo.get(0).setReturnMessage(e.toString());
	        e.printStackTrace();
	        return parmsInfo;
	    }	
	}
	
	@Override
	public Class<?> getTargetClass() {
		return ParmInfo.class;
	}

	  @Override
	  public Object[] query() throws Exception {
		return runpgm().toArray();
	}
}
