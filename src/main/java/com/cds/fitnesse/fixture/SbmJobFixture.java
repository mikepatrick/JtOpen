package com.cds.fitnesse.fixture;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.cds.fitnesse.utils.CommandExecution;
import com.cds.fitnesse.utils.SubmittedJob;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
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
import static com.cds.fitnesse.utils.CdsFixtureUtils.*;

public class SbmJobFixture extends CmdCallFixture{

	private SubmittedJob theJob;
	
	public ArrayList<CommandExecution> submitJob() throws Exception{
		ArrayList<CommandExecution> commandToExecute = new ArrayList<CommandExecution>();
		commandToExecute.add(new CommandExecution());
		
		if (args.length == 0){
			
			commandToExecute.get(0).setReturnMsg("No arguments passed");
			return commandToExecute;
		}
		if (args.length < 3){
			commandToExecute.get(0).setReturnMsg("Not enough arguments found");
			return commandToExecute;
		}
		
		//Location of the job card
		String cardLib = args[0];
		String cardFile = args[1];
		String cardMember = args[2];
		
		String command = constructCommand(cardLib, cardFile, cardMember);
		commandToExecute.get(0).setCmd(command);
		ArrayList<CommandExecution> cmdRan = new ArrayList<CommandExecution>();
		cmdRan = runcmd(command);
		String retmsg = cmdRan.get(0).getReturnMsg();
		String retAbbrMsg = getJobDetails(retmsg);
		commandToExecute.get(0).setReturnMsg(getJobDetails(retmsg));
		Fixture.setSymbol("qualJobName", retAbbrMsg);
		return commandToExecute;
	}
	
	public String[] getJobLog() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException{
		AS400 serv = null;
		CdsAS400Connection dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		
		serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
		if(theJob == null || theJob.getJobNumber() == null){
			String returnVal[] = {"Job not set"};
			return returnVal;
		}
		Job job = new Job(serv, theJob.getJobName(), theJob.getJobUser(), theJob.getJobNumber());
		job.loadInformation();
		
		int numloops = 0;
		while(!job.getStatus().equals(job.JOB_STATUS_OUTQ)){
			Thread.sleep(2000);
			job.loadInformation();
			if (++numloops > 10){
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
	
	private ArrayList<String> getSpooledJobLog() throws PropertyVetoException, AS400Exception, ErrorCompletingRequestException, InterruptedException, RequestNotSupportedException, AS400SecurityException, IOException{
		CdsAS400Connection dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
			
		theJob = CdsFixtureUtils.parseJobName((String) Fixture.getSymbol("qualJobName"));
		
		SpooledFileList spoolList = new SpooledFileList(serv);
		spoolList.setUserDataFilter(theJob.getJobName());
		spoolList.openSynchronously();
		int numSpooledFiles = spoolList.size();
		
		AS400Text txt = null;
		ArrayList<String> logMsgs = new ArrayList<String>();
		PrintObjectInputStream is = null;
		
		for(int i = 0; i < numSpooledFiles; i++){
			SpooledFile splf = (SpooledFile) spoolList.getObject(i);
			if(splf.getJobName().equals(theJob.getJobName()) && splf.getJobNumber().equals(theJob.getJobNumber())){
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
		StringBuilder sb = new StringBuilder();
		sb.append("SBMDBJOB FILE(").append(cardLib).append("/").append(cardFile).append(") ").append("MBR(").append(cardMember).append(")");
		return sb.toString();
//		result = result.concat("SBMDBJOB FILE(");
//		result = result.concat(cardLib).concat("/").concat(cardFile).concat(") ");
//		result = result.concat("MBR(").concat(cardMember).concat(")");
//		
//		return result;
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
	     			s.close();
	     			return "Could not parse job name - ".concat(fullMessage);
	     		} 
	     }else{
	    	 s.close();
	    	 return "Could not parse job name - ".concat(fullMessage);
		 }
	     s.close();
	     return fullJobName;
	}
	@Override
	public Object[] query() throws Exception {
		
		return submitJob().toArray();
	}
}
