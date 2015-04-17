package com.cds.fitnesse.utils;

import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;

public class CdsFixtureUtils {

	public static final String SERV = "SERV";
	public static final String DB_PROPS_FILE = "db.properties";
	public static final String CHAR = "CHAR";
	public static final String NUM = "NUM";
	public static final String ZON = "ZON";
	public static final int MAX_DQ_ENTRY_SIZE = 400;
	
	public static AS400 getAS400(final String sys, final String user, final String password)  {
		return getAS400(sys, user, password, AS400.COMMAND);
	}
	
	public static AS400 getAS400(final String sys, final String user, final String password, final int type)  {
		AS400 serv = new AS400(sys, user, password);
		try {
			serv.connectService(type);
		} catch (AS400SecurityException | IOException e) {
			e.printStackTrace();
			return null;
		}
		return serv;
	}
	
	public static SubmittedJob parseJobName(String fullJobName)
	{
		String parts[] = fullJobName.split("/");
		return new SubmittedJob(parts[0], parts[1], parts[2]);
	}
}
