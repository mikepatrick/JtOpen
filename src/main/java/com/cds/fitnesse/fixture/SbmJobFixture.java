package com.cds.fitnesse.fixture;

import java.util.Scanner;

public class SbmJobFixture extends CmdCallFixture{

	private class qualifiedJob 
	{
		public String jobName;
		public String jobNumber;
		public String jobUser;
		
	}
	public String submitJob() throws Exception{
		if (args.length == 0){
			return "No arguments passed";
		}
		if (args.length < 3){
			return "Not enough arguments found";
		}
		
		String cardLib = args[0];
		String cardFile = args[1];
		String cardMember = args[2];
		
		String command = constructCommand(cardLib, cardFile, cardMember);
		
		String returnValue = runcmd(command);
		
		// Get qualified job name here for job log retrieval
		String qualifiedJobName = getJobDetails(returnValue);
		/*String parts[] = qualifiedJobName.split("/");
		
		String jobNumber = parts[0];
		String jobUser = parts[1];
		String jobName = parts[2];
		*/

		//return returnValue;
		return qualifiedJobName;
		
		
	}
	
	public String getJobLog(){
		
		return "";
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
}
