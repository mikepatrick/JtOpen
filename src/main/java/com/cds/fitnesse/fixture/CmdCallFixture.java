package com.cds.fitnesse.fixture;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.cds.fitnesse.utils.CdsFixtureUtils;
import com.cds.fitnesse.utils.CommandExecution;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CommandCall;

import fit.RowFixture;

public class CmdCallFixture extends RowFixture {
	/*
	 * A RowFixture expects an ArrayList of elements to be returned from a query() method.
	 * This fixture returns an ArrayList of CommandExecution objects.
	 * 
	 */
	private static final String SERV = "SERV";
	protected String applicationName = null;
	protected Properties dbProperties = null;
	private CdsAS400Connection dbConn = null;
	private String returnMsg = "";
	private String dbFile = "db.properties";
	
	public ArrayList<CommandExecution> runcmd(String command) throws Exception  {

		dbConn = new CdsAS400Connection(dbFile);
		AS400 serv = null;
		ArrayList<CommandExecution> thisCall = new ArrayList<CommandExecution>();
		thisCall.add(new CommandExecution());
		thisCall.get(0).setCmd(command);
		
		serv = CdsFixtureUtils.getAS400(SERV, dbConn.getUser(), dbConn.getPassword());			
		CommandCall cmd = new CommandCall(serv, command);
		 
		try{
			if (cmd.run() != true){
				System.out.println("Command failed - cmd.run() did not return true");	
			}
			
			AS400Message[] messagelist = cmd.getMessageList();
			returnMsg = "";
			for (int i = 0; i < messagelist.length; ++i){
        
				System.out.println(messagelist[i].getText());
				returnMsg = returnMsg.concat(messagelist[i].getText());
//				thisCall.add(new CommandExecution("", messagelist[i].getText()));
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
		dbConn = new CdsAS400Connection(dbFile);
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
