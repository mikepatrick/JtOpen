package com.cds.fitnesse.fixture;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.cds.fitnesse.utils.CdsAS400Connection;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.DataQueueEntry;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IllegalObjectTypeException;
import com.ibm.as400.access.ObjectAlreadyExistsException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import fitlibrary.SequenceFixture;

public class DataQueueFixture extends SequenceFixture {

	private String dbFile = "db.properties";
	private CdsAS400Connection dbConn = null;
	private static final String SERV = "SERV";
	private DataQueue dq = null;
	
	public AS400 getAS400(String sys, String user, String password) throws IOException, AS400SecurityException {
		
		AS400 serv = new AS400(sys, user, password);
		serv.connectService(AS400.DATAQUEUE);
		return serv;
	}
	private String getPath(String lib, String qName){
		String returnVal = "";
		returnVal = returnVal.concat("/QSYS.LIB/").concat(lib).concat(".LIB/").concat(qName).concat(".DTAQ");
		return returnVal;
	}
	public String create(String lib, String dtaq) {
		dbConn = new CdsAS400Connection(dbFile);
		AS400 serv;
		String path = getPath(lib, dtaq);
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e1) {
			e1.printStackTrace();
			return "IO Exception @ getAS400()";
		} catch (AS400SecurityException e1) {
			e1.printStackTrace();
			return "AS400 security exception @ getAS400()";
		}
		
		dq = new DataQueue(serv, path);

       
	//  Try to create the data queue on the iSeries using the java object
		try {
			dq.create(400);
		} catch (ErrorCompletingRequestException e) {
			e.printStackTrace();
			return "ErrorCompletingRequestException @ dq.create()";
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "InterruptedException @ dq.create()";
		} catch (ObjectAlreadyExistsException e) {
			e.printStackTrace();
			return "OjbectAlreadyExistsException @ dq.create()";
		} catch (ObjectDoesNotExistException e) {
			e.printStackTrace();
			return "ObjectDoesNotExistException @ dq.create()";
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "AS400 Security exception @ dq.create()";
		} catch (IOException e) {
			e.printStackTrace();
			return "IO Exception @ dq.create()";
		}
		
		return "Data Queue created successfully - I think";
	}
	
	public String send(String lib, String dtaq, String data){
		dbConn = new CdsAS400Connection(dbFile);
		AS400 serv;
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e1) {
			e1.printStackTrace();
			return "IO Exception @ getAS400()";
		} catch (AS400SecurityException e1) {
			e1.printStackTrace();
			return "AS400 security exception @ getAS400()";
		}

		String path = getPath(lib, dtaq);
		dq = new DataQueue(serv, path);		
		
		//TODO get the 500 byte hard coding out of here
		AS400Text textBytes = new AS400Text(data.length());
		byte[] dataToSend = new byte[500];
		dataToSend = textBytes.toBytes(data);
		
		try {
			dq.write(dataToSend);
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "AS400 Security Exception @ dq.write()";
		} catch (ErrorCompletingRequestException e) {
			e.printStackTrace();
			return "ErrorCompletingRequestException @ dq.write()";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException @ dq.write()";
		} catch (IllegalObjectTypeException e) {
			e.printStackTrace();
			return "Illegal Object Type Exception @ dq.write()";
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "InterruptedException @ dq.write()";
		} catch (ObjectDoesNotExistException e) {
			e.printStackTrace();
			return "ObjectDoesNotExistException @ dq.write()";
		}
		return "data queue message sent ok";
	}
	public String peek(String lib, String dtaq){
		
		dbConn = new CdsAS400Connection(dbFile);
		AS400 serv;
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e1) {
			e1.printStackTrace();
			return "IO Exception @ getAS400()";
		} catch (AS400SecurityException e1) {
			e1.printStackTrace();
			return "AS400 security exception @ getAS400()";
		}
		
		String path = getPath(lib, dtaq);
		dq = new DataQueue(serv, path);		
		DataQueueEntry dqe = null;
		
		try {
			dqe = dq.peek();
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "AS400 Security Exception @ dq.read()";
		} catch (ErrorCompletingRequestException e) {
			e.printStackTrace();
			return "ErrorCompletingRequestException @ dq.read()";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException @ dq.read()";
		} catch (IllegalObjectTypeException e) {
			e.printStackTrace();
			return "Illegal Object Type Exception @ dq.read()";
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "InterruptedException @ dq.read()";
		} catch (ObjectDoesNotExistException e) {
			e.printStackTrace();
			return "ObjectDoesNotExistException @ dq.read()";
		}	
		if (dqe == null){
			return "No entries to peek at on data queue " + dtaq;
		}
		try {
			return dqe.getString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "UnsupportedEncodingException @ peek.dqe.getString()";
		}
	}
	
	public String receive(String lib, String dtaq){
		
		dbConn = new CdsAS400Connection(dbFile);
		AS400 serv;
		
		try {
			serv = getAS400(SERV, dbConn.getUser(), dbConn.getPassword());
		} catch (IOException e1) {
			e1.printStackTrace();
			return "IO Exception @ getAS400()";
		} catch (AS400SecurityException e1) {
			e1.printStackTrace();
			return "AS400 security exception @ getAS400()";
		}
		
		String path = getPath(lib, dtaq);
		dq = new DataQueue(serv, path);		
		DataQueueEntry dqe = null;
		
		try {
			dqe = dq.read();
		} catch (AS400SecurityException e) {
			e.printStackTrace();
			return "AS400 Security Exception @ dq.read()";
		} catch (ErrorCompletingRequestException e) {
			e.printStackTrace();
			return "ErrorCompletingRequestException @ dq.read()";
		} catch (IOException e) {
			e.printStackTrace();
			return "IOException @ dq.read()";
		} catch (IllegalObjectTypeException e) {
			e.printStackTrace();
			return "Illegal Object Type Exception @ dq.read()";
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "InterruptedException @ dq.read()";
		} catch (ObjectDoesNotExistException e) {
			e.printStackTrace();
			return "ObjectDoesNotExistException @ dq.read()";
		}
		
		if(dqe == null){
			return "No entries to receive on data queue " + dtaq;
		}
		try {
			return dqe.getString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "UnsupportedEncodingException @ receive.dqe.getString()";
		}
	}
}
