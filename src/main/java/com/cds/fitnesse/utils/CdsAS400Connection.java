package com.cds.fitnesse.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CdsAS400Connection {
	protected Properties dbProperties = null;
	protected String applicationName = null;
	
	public CdsAS400Connection(String filename)  {
		this.getDbProperties(filename);
	}
	/**
	 * Gets the properties that are used to connect to the 400.
	 * 
	 * @return Properties
	 * 
	 * @throws Exception
	 */
	protected Properties getDbProperties(String filename)  {
		if (this.dbProperties == null) {
			this.dbProperties = new Properties();
			try {	
				this.dbProperties.load(new FileInputStream(filename) );
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.dbProperties;
	}
	public String getUser() {
	
		return this.dbProperties.getProperty("database.user");
		
	}
	public String getPassword() {
		
		return this.dbProperties.getProperty("database.password");

	}
	public String getDriverName() {
	
		return this.dbProperties.getProperty("database.driver");

	}
	public String getDataSource(){
		
		return this.dbProperties.getProperty("database.url");
	
	}	
	public String setUser(String userName){
		if (userName.isEmpty()){
			return this.dbProperties.getProperty("database.user"); 
		}
		this.dbProperties.setProperty("database.user", userName);
		return this.dbProperties.getProperty("database.user");
	}
	public String setPassword(String pwd){
		if (pwd.isEmpty()){
			return this.dbProperties.getProperty("database.password"); 
		}		
		this.dbProperties.setProperty("database.password", pwd);
		return this.dbProperties.getProperty("database.password");
	}	
	public String setDataSource(String url){
		if (url.isEmpty()){
			return this.dbProperties.getProperty("database.url"); 
		}		
		this.dbProperties.setProperty("database.url", url);
		return this.dbProperties.getProperty("database.url");
	}		
}
