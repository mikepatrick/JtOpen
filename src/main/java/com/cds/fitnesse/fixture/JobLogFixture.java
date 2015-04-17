package com.cds.fitnesse.fixture;

import java.beans.PropertyVetoException;
//import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Scanner;
import java.util.List;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.cds.fitnesse.utils.SpooledFileListing;
import com.cds.fitnesse.utils.SubmittedJob;
//import com.cds.fitnesse.utils.SubmittedJob;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
//import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
//import com.ibm.as400.access.PrintObjectInputStream;
//import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.RequestNotSupportedException;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

import edu.emory.mathcs.backport.java.util.Arrays;
import fit.Fixture;
import fit.RowFixture;

public class JobLogFixture extends RowFixture{

	private static final String SERV = "SERV";
	private String dbFile = "db.properties";
	private SubmittedJob theJob;
	
	private ArrayList<SpooledFileListing> getSpooledJobLog() throws PropertyVetoException, AS400Exception, ConnectionDroppedException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException{
		AS400 serv = null;
		CdsAS400Connection dbConn = new CdsAS400Connection(dbFile);
		String fullJobName = (String) Fixture.getSymbol("qualJobName");
		
		serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
		SpooledFileList spoolList = new SpooledFileList(serv);
		theJob = CdsFixtureUtils.parseJobName(fullJobName);
		
		Thread.sleep(5000);
//		spoolList.setUserDataFilter(jobName);
		spoolList.setUserFilter(theJob.getJobUser());
		spoolList.openSynchronously();
		int numSpooledFiles = spoolList.size();
		ArrayList<SpooledFileListing> logMsgs = new ArrayList<SpooledFileListing>();
		
		for(int i = 0; i < numSpooledFiles; i++){
			SpooledFile splf = (SpooledFile) spoolList.getObject(i);
			if(splf.getJobName().equals(theJob.getJobName()) && splf.getJobNumber().equals(theJob.getJobNumber())){
				logMsgs.add(new SpooledFileListing("File: ".concat(splf.getName())));
				if(splf.getName().equals("QPJOBLOG")){
					//Get the hard coded library (and file?) name out of this command.
					String copyCommand = "CPYSPLF FILE(QPJOBLOG) TOFILE(MPATRICK/JOBLOGPF) JOB(".concat(fullJobName).concat(") SPLNBR(*ANY) MBROPT(*REPLACE)");
					CommandCall cpyCmd = new CommandCall(serv, copyCommand);
					if(cpyCmd.run() != true)
					{
						//TODO;
						;
					}
					else
					{
						List<AS400Message> mlist = Arrays.asList(cpyCmd.getMessageList());
						
						for (AS400Message message: mlist)
						{
							System.out.println(message.getText());
							logMsgs.add(new SpooledFileListing(message.getText()));
						}
						
						logMsgs.add(new SpooledFileListing("Job log copied to JOBLOGPF"));
					}
				}
			}
		}
			
	//				return logMsgs;
	/*				PrintParameterList pParms = new PrintParameterList();
		//			pParms.setParameter(SpooledFile.ATTR_MFGTYPE, "*WSCST");
					pParms.setParameter(SpooledFile.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST");					
					pParms.setParameter(SpooledFile.ATTR_SCS2ASCII, "*YES");
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
		}*/
		return logMsgs;
//		SpooledFile spooledLog = createSpooledFile();
	}	
	
/*	private String getJobDetails(String fullMessage){
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
	}	*/
	@Override
	public Class<?> getTargetClass() {
		
		return SpooledFileListing.class;
	}

	@Override
	public Object[] query() throws Exception {
		return getSpooledJobLog().toArray();
	}

}
