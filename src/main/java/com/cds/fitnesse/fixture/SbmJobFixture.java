package com.cds.fitnesse.fixture;

import java.io.IOException;
import java.util.Scanner;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.JobLog;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.QueuedMessage;

public class SbmJobFixture extends CmdCallFixture{

	private String parts[];
	private String jobName;
	private String jobNumber;
	private String jobUser;
	private static final String SERV = "SERV";	
	private String dbFile = "db.properties";
	
	public String submitJob() throws Exception{
		if (args.length == 0){
			return "No arguments passed";
		}
		if (args.length < 3){
			return "Not enough arguments found";
		}
		
		//Location of the job card
		String cardLib = args[0];
		String cardFile = args[1];
		String cardMember = args[2];
		
		String command = constructCommand(cardLib, cardFile, cardMember);
		
		String returnValue = runcmd(command);
		
		// Get qualified job name here for job log retrieval
		String qualifiedJobName = getJobDetails(returnValue);
		if (!qualifiedJobName.contains("/")){
			return "Could not parse job name";
		}
		parts = qualifiedJobName.split("/");
		
		jobNumber = parts[0];
		jobUser = parts[1];
		jobName = parts[2];

		//return returnValue;
		return qualifiedJobName;
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
		while(!job.getStatus().equals(job.JOB_STATUS_OUTQ)){
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
	
//	private String[] getSpooledJobLog(){
//		
//	}
	
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
}
