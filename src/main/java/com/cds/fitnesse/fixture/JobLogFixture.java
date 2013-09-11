package com.cds.fitnesse.fixture;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.SubmittedJob;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.PrintObjectInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.RequestNotSupportedException;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

import fit.Fixture;
import fit.RowFixture;

public class JobLogFixture extends RowFixture{

	private static final String SERV = "SERV";
	private String dbFile = "db.properties";
	private String parts[];
	private String jobName;
	private String jobNumber;
	private String jobUser;
	
	public AS400 getAS400(String sys, String user, String password) throws IOException, AS400SecurityException {
		AS400 serv = new AS400(sys, user, password);
		serv.connectService(AS400.COMMAND);
		return serv;
	}
	
	private ArrayList<String> getSpooledJobLog() throws PropertyVetoException, AS400Exception, ConnectionDroppedException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException{
		AS400 serv = null;
		CdsAS400Connection dbConn = new CdsAS400Connection(dbFile);
		String fullJobName = (String) Fixture.getSymbol("qualJobName");
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AS400SecurityException e) {
			e.printStackTrace();
		}				
		SpooledFileList spoolList = new SpooledFileList(serv);
		//String fullReturnMessage = args[0];
		//if fullReturnMessage does not contain /, do something else
		parts = fullJobName.split("/");
		jobNumber = parts[0];
		jobUser = parts[1];
		jobName = parts[2];
		
		spoolList.setUserDataFilter(jobName);
		spoolList.openSynchronously();
		int numSpooledFiles = spoolList.size();
		AS400Text txt = null;
		ArrayList<String> logMsgs = new ArrayList<String>();
		PrintObjectInputStream is = null;
		for(int i = 0; i < numSpooledFiles; i++){
			SpooledFile splf = (SpooledFile) spoolList.getObject(i);
			if(splf.getJobName().equals(jobName) && splf.getJobNumber().equals(jobNumber)){
				if(splf.getName().equals("QPJOBLOG")){
					PrintParameterList pParms = new PrintParameterList();
		//			pParms.setParameter(SpooledFile.ATTR_MFGTYPE, "*WSCST");
					pParms.setParameter(SpooledFile.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST");					
		//			pParms.setParameter(SpooledFile.ATTR_SCS2ASCII, "*YES");
					is = splf.getInputStream(pParms);
	//				BufferedReader buf = new BufferedReader(new InputStreamReader(is));
					
	//				String thisLine = "";
	//				while((thisLine = buf.readLine()) != null){
	//					logMsgs.add(thisLine);
	//				}
					
					int numBytes = is.available();
					byte byteJar[] = new byte[132];
					for(int j = 0; j < numBytes; j+=132){
						int numLeft = is.available();
						if (numLeft < 132){
							txt = new AS400Text(numLeft);
							byteJar = new byte[numLeft];
						}else{
							txt = new AS400Text(132);
							byteJar = new byte[132];
						}
		//		}else{
				//			txt = new AS400Text(numLeft);
				//		}
						is.read(byteJar);
						String logline = ((String) txt.toObject(byteJar)).trim();
				//      String logline2 = Arrays.toString(byteJar);
						String logline3 = new String(byteJar, "CP037");
						String logline4 = new String(byteJar, "ASCII");
						
				//		String logline4 = new String(byteJar, "IBM0037");
				//		String logline = Arrays.toString(byteJar);
						logMsgs.add(logline);
					}
					
				}
			}
		}
		return logMsgs;
//		SpooledFile spooledLog = createSpooledFile();
	}	
	
	private String getJobDetails(String fullMessage){
		 String firstToken = "";
		 String fullJobName = "";
		 Scanner s = new Scanner(fullMessage);
	     if (s.hasNext()){
	    	 firstToken = s.next();
	     }
	     	if (firstToken.equals("Job")){
	     		if (s.hasNext()){
	     			fullJobName = s.next();	
	     		}else{
	     			return "Could not parse job name - ".concat(fullMessage);
	     		} 
	     }else{
	    	 return "Could not parse job name - ".concat(fullMessage);
		 }
	     return fullJobName;
	}	
	@Override
	public Class<?> getTargetClass() {
		
		return String.class;
	}

	@Override
	public Object[] query() throws Exception {
		return getSpooledJobLog().toArray();
	}

}
