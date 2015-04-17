package com.cds.fitnesse.fixture;


import static com.cds.fitnesse.utils.CdsFixtureUtils.DB_PROPS_FILE;
import static com.cds.fitnesse.utils.CdsFixtureUtils.SERV;

import java.util.ArrayList;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.cds.fitnesse.utils.CommandExecution;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;

import fit.RowFixture;

public class CmdCallFixture extends RowFixture {
	/*
	 * A RowFixture expects an ArrayList of elements to be returned from a query() method.
	 * This fixture returns an ArrayList of CommandExecution objects.
	 * 
	 */
	
	private CdsAS400Connection dbConn = null;
	
	public ArrayList<CommandExecution> runcmd(String command) throws Exception  {

		String returnMsg = "";
		dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		ArrayList<CommandExecution> thisCall = new ArrayList<CommandExecution>();
		thisCall.add(new CommandExecution());
		thisCall.get(0).setCmd(command);
	
		
		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());			
		CommandCall cmd = new CommandCall(serv, command);
		 
		try{
			if (cmd.run() != true) 
			{
				System.out.println("Command failed - cmd.run() did not return true");	
			}
			
			AS400Message[] messagelist = cmd.getMessageList();
			for (int i = 0; i < messagelist.length; ++i)
			{
				System.out.println(messagelist[i].getText());
				returnMsg = returnMsg.concat(messagelist[i].getText());
			}	 
			thisCall.get(0).setReturnMsg(returnMsg);
			thisCall.get(0).setCmd(command);
			return thisCall;
		}
		catch (Exception e)
		{
	        System.out.println("Command " + cmd.getCommand() + " issued an exception!");
	        e.printStackTrace();
	        
	        returnMsg = "Command failed";
	        thisCall.get(0).setReturnMsg(returnMsg);
	        return thisCall;
		}			
	}
	
	/*
	 * This method has not been fully implemented.  The idea was to allow a test table
	 * to establish a different set of credentials for connecting to the 400.
	 */
	public String loginUserPassword(String dataSource, String userName, String password){
		
		if (!(dataSource.isEmpty())){
			this.dbConn.setDataSource(dataSource);
		}
		this.dbConn.setUser(userName);
		this.dbConn.setPassword(password);
		dbConn = new CdsAS400Connection(DB_PROPS_FILE);
		AS400 serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		return "Credentials changed";
	}

	@Override
	public Class<?> getTargetClass() {
		
		return CommandExecution.class;
	}

	@Override
	public Object[] query() throws Exception {
		
		return runcmd(args[0]).toArray();
	}
	
}
