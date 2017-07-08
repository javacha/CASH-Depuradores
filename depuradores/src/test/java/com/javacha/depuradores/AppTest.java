package com.javacha.depuradores;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
       
        TLCLDepuraTablas dep = new TLCLDepuraTablas();
        String path1 = "C:/Users/cocho/git/CASH-Depuradores/depuradores/src/main/resources/connDataTLCL.properties";
        String path2 = "C:/Users/cocho/git/CASH-Depuradores/depuradores/src/main/resources/depuradorTLCL.properties";
        
        String[] args = { path1, path2};
        dep.main(args);
        
        assertTrue( true );
    }
}
