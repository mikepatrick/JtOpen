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
import static com.cds.fitnesse.utils.CdsFixtureUtils.*;
public class JobLogFixture extends RowFixture{

	private SubmittedJob theJob;
	
	private ArrayList<SpooledFileListing> getSpooledJobLog() throws PropertyVetoException, AS400Exception, ConnectionDroppedException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, RequestNotSupportedException{
		
		CdsAS400Connection dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		String fullJobName = (String) Fixture.getSymbol("qualJobName");
		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
		SpooledFileList spoolList = new SpooledFileList(serv);
		theJob = CdsFixtureUtils.parseJobName(fullJobName);
		
		Thread.sleep(5000);
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
					{	//TODO Use CollectionUtils or something here to deal with parameterized type
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
			
		return logMsgs;
	}	
	
	@Override
	public Class<?> getTargetClass() {
		
		return SpooledFileListing.class;
	}

	@Override
	public Object[] query() throws Exception {
		return getSpooledJobLog().toArray();
	}

}
