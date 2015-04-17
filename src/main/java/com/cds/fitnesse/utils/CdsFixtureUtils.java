package com.cds.fitnesse.utils;

import java.io.IOException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;

public class CdsFixtureUtils {

	public static AS400 getAS400(final String sys, final String user, final String password)  {
		AS400 serv = new AS400(sys, user, password);
		try {
			serv.connectService(AS400.COMMAND);
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
