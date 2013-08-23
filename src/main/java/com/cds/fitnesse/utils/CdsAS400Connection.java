package com.cds.fitnesse.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CdsAS400Connection {
	protected Properties dbProperties = null;
	protected String applicationName = null;
	
	public CdsAS400Connection() throws Exception {
		this.getDbProperties();
	}
	/**
	 * Gets the dbunit properties that are used to load data for each row.
	 * 
	 * @return Properties
	 * 
	 * @throws Exception
	 */
	protected Properties getDbProperties() throws Exception {
		if (this.dbProperties == null) {
			this.dbProperties = new Properties();
			try {
				if( this.applicationName != null && this.applicationName != "" )
				{
					this.dbProperties.load(new FileInputStream(this.applicationName + ".properties"));
				}
				else
				{
					this.dbProperties.load(new FileInputStream( "db.properties" ) );
				}
			} catch (IOException e) {
				throw new Exception("Couldn't find dbUnit properties file.");
			}
		}
		return this.dbProperties;
	}
	public String getUser() {
		try {
			return this.getDbProperties().getProperty("database.user");
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}
	public String getPassword() {
		try {
			return this.getDbProperties().getProperty("database.password");
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}
	public String getDriverName() {
		try {
			return this.getDbProperties().getProperty("database.driver");
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}
	public String getDataSource(){
		try {
			return this.getDbProperties().getProperty("database.url");
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}	
}
