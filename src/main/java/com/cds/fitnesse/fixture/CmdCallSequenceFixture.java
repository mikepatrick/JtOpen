package com.cds.fitnesse.fixture;


import java.io.IOException;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;
import static com.cds.fitnesse.utils.CdsFixtureUtils.*;
import fitlibrary.SequenceFixture;

public class CmdCallSequenceFixture extends SequenceFixture {

	private CdsAS400Connection dbConn = null;

	public String runcmd(String command) throws Exception  {

		String returnMsg = "";
		dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
		CommandCall cmd = new CommandCall(serv, command);

		try{
			if (cmd.run() != true){
				System.out.println("Command failed - cmd.run() did not return true");	
			}

			AS400Message[] messagelist = cmd.getMessageList();
			returnMsg = "";
			for (int i = 0; i < messagelist.length; ++i){
        
            // Show each message.
				System.out.println(messagelist[i].getText());
				returnMsg = returnMsg.concat(messagelist[i].getText());
			}	 
			
			return returnMsg;
		}
		catch (Exception e)
		{
	        System.out.println("Command " + cmd.getCommand() + " issued an exception!");
	        e.printStackTrace();	
	        return "Command failed";
		}			
	}
	public String loginUserPassword(String dataSource, String userName, String password){

		if (!(dataSource.isEmpty())){
			this.dbConn.setDataSource(dataSource);
		}

		dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		this.dbConn.setUser(userName);
		this.dbConn.setPassword(password);		

		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		
		return "Credentials changed";
	}
	public String waitForXseconds(int seconds){
		try {
			Thread.sleep(seconds * 1000);
		}catch(Exception e){
			return "not ok";
		}
		return "ok";
	}
}