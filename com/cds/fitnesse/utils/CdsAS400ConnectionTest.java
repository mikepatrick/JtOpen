package com.cds.fitnesse.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CdsAS400ConnectionTest extends TestCase{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CdsAS400ConnectionTest( String testName )
    {
        super( testName );
    }
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CdsAS400ConnectionTest.class );
    }    
    /** When a valid connection properties file is available,
     * verify the object is created and contains non-null properties.
     */
    public void testCdsAS400ConnectionOk()
    {
    	CdsAS400Connection con = new CdsAS400Connection("C:/users/mpatrick/db.properties");
    	assertNotNull(con);
    	assertEquals("username", "MPATRICK", con.getUser());
    }
    /** When a valid connection properties file is not found,
     * verify that null is returned.
     */
    public void testCdsAS400ConnectionFileNotFound()
    {
    	CdsAS400Connection con = new CdsAS400Connection("db9.properties");
    	// There should be something better than the '==' operator here
    	assert(con == null);
    }    
    /** Changing user name to the empty string is not allowed.
     * Note that setters return the 
     */
    public void testUser()
    {
    	CdsAS400Connection con = new CdsAS400Connection("c:/users/mpatrick/db.properties");
    	assertEquals("username", "MPATRICK", con.getUser());
    	assertEquals("username", "HOUDINI", con.setUser("HOUDINI"));
    	assertEquals("username", "HOUDINI", con.getUser());
    	String emptyString = "";
    	assertEquals("username", "HOUDINI", con.setUser(emptyString));
    	assertEquals("username", "HOUDINI", con.getUser());
    }    
    public void testPassword()
    {
    	CdsAS400Connection con = new CdsAS400Connection("c:/users/mpatrick/db.properties");
    	assertEquals("password", "MYPASS", con.getPassword());
    	assertEquals("password", "DUMMYPASS", con.setPassword("DUMMYPASS"));
    	assertEquals("DUMMYPASS", "DUMMYPASS", con.getPassword());
    	String emptyString = "";
    	assertEquals("password", "DUMMYPASS", con.setPassword(emptyString));
    	assertEquals("password", "DUMMYPASS", con.getPassword());
    }        
    public void testDataSource()
    {
    	String url = "jdbc:as400://serv.cdsfulfillment.com;naming=sql;";
    	CdsAS400Connection con = new CdsAS400Connection("c:/users/mpatrick/db.properties");
    	assertEquals("url", url, con.getDataSource());
    	assertEquals("url", "http://google.com", con.setDataSource("http://google.com"));
    	assertEquals("password", "http://google.com", con.getDataSource());
    	String emptyString = "";
    	assertEquals("url", "http://google.com", con.setDataSource(emptyString));
    	assertEquals("url", "http://google.com", con.getDataSource());
    }     
} 
