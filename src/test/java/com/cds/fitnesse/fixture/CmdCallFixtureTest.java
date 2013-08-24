package com.cds.fitnesse.fixture;

import com.cds.fitnesse.utils.CdsAS400Connection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class CmdCallFixtureTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CmdCallFixtureTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( CmdCallFixtureTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void TestRunCmdGoodConnection(){
    	
    }
    public void TestRunCmdBadConnection(){
    	
    }
}
