package co.luminositylabs.utils.orientdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests the url-building functionality of OrientDBUtil class.
 *
 * @author Phillip Ross
 */
public class DBUrlBuildingTest {

    private static final Logger logger = LoggerFactory.getLogger(DBUrlBuildingTest.class);


    @Test
    public void testEngineEnumValueConversions() {
        final String dbName = "validDatabaseName";
        final String dbHostname = "localhost";
        final String dbPortSpecification = "7654";
        final String dbPath = "/validPath";
        for (OrientDBUtil.Engine validEngine : OrientDBUtil.Engine.values()) {
            Assert.assertEquals(
                    validEngine,
                    OrientDBUtil.Engine.valueOf(validEngine.toString())
            );
            Assert.assertEquals(
                    validEngine,
                    OrientDBUtil.Engine.fromString(validEngine.toString())
            );
            OrientDBUtil.buildDatabaseUrl(dbName, validEngine, dbHostname, dbPortSpecification, dbPath);
        }
    }


    @Test
    public void testBuildDatabaseUrlDatabaseNameNotNull() {
        boolean exceptionOccurred = false;
        try {
            OrientDBUtil.buildDatabaseUrl(null, null, null, null, null);
        } catch (NullPointerException npe) {
            exceptionOccurred = true;
            Assert.assertTrue(npe.getMessage().contains("databaseName must be provided"));
        }
        Assert.assertTrue(exceptionOccurred);
    }


    @Test
    public void testBuildDatabaseUrlEngineNotNull() {
        final String dbName = "validDatabaseName";
        boolean exceptionOccurred = false;
        try {
            OrientDBUtil.buildDatabaseUrl(dbName, null, null, null, null);
        } catch (NullPointerException npe) {
            exceptionOccurred = true;
            Assert.assertTrue(npe.getMessage().contains("engine must be provided"));
        }
        Assert.assertTrue(exceptionOccurred);
    }


    @Test
    public void testValidEngine() {
        final String dbName = "validDatabaseName";
        final String dbHostname = "localhost";
        final String dbPortSpecification = "7654";
        final String dbPath = "/";
        boolean exceptionOccurred = false;
        try {
            OrientDBUtil.buildDatabaseUrl(
                    dbName,
                    OrientDBUtil.Engine.valueOf("invalidEngine"),
                    dbHostname,
                    dbPortSpecification,
                    dbPath
            );
        } catch (IllegalArgumentException iae) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);
    }


    @Test
    public void testBuildDatabaseUrlHostnameOnlyNullWhenEngineNotRemote() {
        final String dbName = "validDatabaseName";
        final String dbPortSpecification = "7654";
        final String dbPath = "/";
        for (OrientDBUtil.Engine engine : OrientDBUtil.Engine.values()) {
            boolean exceptionOccurred = false;
            try {
                OrientDBUtil.buildDatabaseUrl(dbName, engine, null, dbPortSpecification, dbPath);
            } catch (NullPointerException npe) {
                exceptionOccurred = true;
                Assert.assertTrue(npe.getMessage().contains("hostname must be provided"));
            }
            Assert.assertTrue(!exceptionOccurred || engine.equals(OrientDBUtil.Engine.REMOTE));
        }
    }


    @Test
    public void testBuildDatabaseUrlValidPortRange() {
        // Null port ranges are allowed... when null, the port will not be included.
        final String dbName = "validDatabaseName";
        final String engine = "remote";
        final String dbHostname = "localhost";
        final String dbPath = "/";
        String url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                null,
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + dbPath + dbName);

        // A string representing a single integer value is valid.
        url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                "7654",
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + ":" + "7654" + dbPath + dbName);

        // A string representing an integer range is valid.
        url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                "7654-9098",
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + ":" + "7654" + dbPath + dbName);

        String[] invalidPortRangeSpecifications = new String[] {"A123", "-1234"};
        for (String portRangeSpecification : invalidPortRangeSpecifications) {
            boolean exceptionOccurred = false;
            try {
                OrientDBUtil.buildDatabaseUrl(
                        dbName,
                        OrientDBUtil.Engine.REMOTE,
                        dbHostname,
                        portRangeSpecification,
                        dbPath
                );
            } catch (IllegalArgumentException iae) {
                exceptionOccurred = true;
                Assert.assertTrue(iae.getMessage().contains("portRange must be a string representing an integer"));
            }
            Assert.assertTrue(exceptionOccurred);
        }
    }


    @Test
    public void testBuildDatabaseUrlValidDbPaths() {
        final String dbName = "validDatabaseName";
        final String engine = "remote";
        final String dbHostname = "localhost";
        final String dbPortRange = "7654";

        // Specfy a dbpath that is null.
        String dbPath = null;
        String url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                dbPortRange,
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + ":" + dbPortRange + "/" + dbName);

        // Specify a dbpath that ends with a slash
        dbPath = "validPath/";
        url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                dbPortRange,
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + ":" + dbPortRange + dbPath + dbName);

        // Specify a dbpath that does NOT end with a slash
        dbPath = "validPath";
        url = OrientDBUtil.buildDatabaseUrl(
                dbName,
                OrientDBUtil.Engine.REMOTE,
                dbHostname,
                dbPortRange,
                dbPath
        );
        Assert.assertEquals(url, engine + ":" + dbHostname + ":" + dbPortRange + dbPath + "/" + dbName);
    }


}
