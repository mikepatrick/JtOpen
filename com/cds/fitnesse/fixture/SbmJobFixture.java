package com.cds.fitnesse.fixture;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CommandExecution;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.PrintObjectInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.QueuedMessage;
import com.ibm.as400.access.RequestNotSupportedException;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;

import fit.Fixture;

public class SbmJobFixture extends CmdCallFixture{

	private String parts[];
	private String jobName;
	private String jobNumber;
	private String jobUser;
	private static final String SERV = "SERV.cdsfulfillment.com";	
	private String dbFile = "db.properties";
	
	public ArrayList<CommandExecution> submitJob() throws Exception{
		ArrayList<CommandExecution> returnVal = new ArrayList<CommandExecution>();
		returnVal.add(new CommandExecution());
		
		if (args.length == 0){
			
			returnVal.get(0).setReturnMsg("No arguments passed");
			return returnVal;
		}
		if (args.length < 3){
			returnVal.get(0).setReturnMsg("Not enough arguments found");
			return returnVal;
		}
		
		//Location of the job card
		String cardLib = args[0];
		String cardFile = args[1];
		String cardMember = args[2];
		
		String command = constructCommand(cardLib, cardFile, cardMember);
		returnVal.get(0).setCmd(command);
		ArrayList<CommandExecution> cmdRan = new ArrayList<CommandExecution>();
		cmdRan = runcmd(command);
		String retmsg = cmdRan.get(0).getReturnMsg();
		String retAbbrMsg = getJobDetails(retmsg);
		returnVal.get(0).setReturnMsg(getJobDetails(retmsg));
		Fixture.setSymbol("qualJobName", retAbbrMsg);
		return returnVal;
		
/*		String returnValue = cmdRan.get(0).getReturnMsg();
		// Get qualified job name here for job log retrieval
		String qualifiedJobName = getJobDetails(returnValue);
		if (!qualifiedJobName.contains("/")){
			returnVal.get(0).setReturnMsg("Could not parse job name");
			return returnVal;
		}
		parts = qualifiedJobName.split("/");
		
		jobNumber = parts[0];
		jobUser = parts[1];
		jobName = parts[2];

		//return returnValue;
		return returnVal;
		*/
	}
	
	public String[] getJobLog() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException{
		AS400 serv = null;
		CdsAS400Connection dbConn = new CdsAS400Connection(dbFile);
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AS400SecurityException e) {
			e.printStackTrace();
		}			
		if(jobName == null){
			String returnVal[] = {"Job not set"};
			return returnVal;
		}
		Job job = new Job(serv, jobName, jobUser, jobNumber);
		job.loadInformation();
		
		int numloops = 0;
		while(!job.getStatus().equals(Job.JOB_STATUS_OUTQ)){
			Thread.sleep(2000);
			job.loadInformation();
			numloops++;
			if (numloops > 10){
				break;
			}
		}
		JobLog jobLog = job.getJobLog();
		int numMsgs = jobLog.getLength();
		
		QueuedMessage jobLogMsgs[] = jobLog.getMessages(-1, numMsgs);
		
		String returnVal[] = new String[numMsgs];
		
		for(int i = 0; i < numMsgs; i++){
			returnVal[i] = jobLogMsgs[i].getMessage(); 
		}
		return returnVal;
	}
	
	private ArrayList<String> getSpooledJobLog() throws PropertyVetoException, AS400Exception, ConnectionDroppedException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException{
		AS400 serv = null;
		CdsAS400Connection dbConn = new CdsAS400Connection(dbFile);
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AS400SecurityException e) {
			e.printStackTrace();
		}				
		SpooledFileList spoolList = new SpooledFileList(serv);
		Object fullJob = Fixture.getSymbol("qualJobName");
		String fullJobName = (String) fullJob;
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
					is = splf.getInputStream(pParms);
					int numBytes = is.available();
					byte byteJar[] = new byte[132];
					for(int j = 0; j < numBytes; j+=132){
						int numLeft = is.available();
						if (numLeft >= 132){
						txt = new AS400Text(132);
						}else{
							txt = new AS400Text(numLeft);
						}
						is.read(byteJar);
						String logline = (String) txt.toObject(byteJar);
						logMsgs.add(logline);
					}
					
				}
			}
		}
		return logMsgs;
//		SpooledFile spooledLog = createSpooledFile();
	}
	
	private String constructCommand(String cardLib, String cardFile, String cardMember){
		String result = "";
		result = result.concat("SBMDBJOB FILE(");
		result = result.concat(cardLib).concat("/").concat(cardFile).concat(") ");
		result = result.concat("MBR(").concat(cardMember).concat(")");
		
		return result;
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
	public Object[] query() throws Exception {
		
		return submitJob().toArray();
	}
}
