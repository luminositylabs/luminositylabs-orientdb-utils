package co.luminositylabs.utils.orientdb;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.OMetadata;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.server.OServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests the embedded server functionality of OrientDBUtil class.
 *
 * @author Phillip Ross
 */
public class EmbeddedServerTest {

    private static final Logger logger = LoggerFactory.getLogger(DBUrlBuildingTest.class);


    @Test
    public void testStartServerRequiresUsername() throws Exception {
        boolean exceptionOccurred = false;
        try {
            OrientDBUtil.startEmbeddedServer(null, "password", null);
        } catch (NullPointerException npe) {
            exceptionOccurred = true;
            Assert.assertTrue(npe.getMessage().contains("Username must be specified"));
        }
        Assert.assertTrue(exceptionOccurred);
    }


    @Test
    public void testStartServerRequiresPassword() throws Exception {
        boolean exceptionOccurred = false;
        try {
            OrientDBUtil.startEmbeddedServer("user1", null, null);
        } catch (NullPointerException npe) {
            exceptionOccurred = true;
            Assert.assertTrue(npe.getMessage().contains("Password must be specified"));
        }
        Assert.assertTrue(exceptionOccurred);
    }


    @Test
    public void testStartStopServer() throws Exception {
        logger.debug("testing");
        final String dbName = "db1";
        final OrientDBUtil.Engine dbEngine = OrientDBUtil.Engine.REMOTE;
        final String dbHostname = "localhost";
        final String dbPortRange = "7654";
        final String dbUsername = "user1";
        final String dbPassword = "user1password";

        OServer oServer = null;
        OServerAdmin oServerAdmin = null;
        OPartitionedDatabasePool databasePool = null;
        ODatabaseDocumentTx oDatabaseDocumentTx = null;
        OMetadata oMetadata = null;
        OSchema oSchema = null;

        try {
            oServer = OrientDBUtil.startEmbeddedServer(dbUsername, dbPassword, dbPortRange);
            String dbUrl = OrientDBUtil.buildDatabaseUrl(dbName, dbEngine, dbHostname, dbPortRange, null);
            oServerAdmin = new OServerAdmin(dbUrl);
            oServerAdmin.connect(dbUsername, dbPassword);
            if (!oServerAdmin.existsDatabase()) {
                oServerAdmin.createDatabase("graph", "plocal");
            }

            databasePool = new OPartitionedDatabasePool(dbUrl, dbUsername, dbPassword);
            oDatabaseDocumentTx = databasePool.acquire();
            oMetadata = oDatabaseDocumentTx.getMetadata();
            oSchema = oMetadata.getSchema();
            Assert.assertTrue(oSchema.countClasses() > 0);
        } finally {
            if (oMetadata != null) {
                oMetadata.close();
            }
            if ((oDatabaseDocumentTx != null) && (!(oDatabaseDocumentTx.isClosed()))) {
                oDatabaseDocumentTx.close();
            }
            if ((databasePool != null) && (!(databasePool.isClosed()))) {
                databasePool.close();
            }
            if (oServerAdmin != null) {
                oServerAdmin.close();
            }
            if ((oServer != null) && (oServer.isActive())) {
                oServer.shutdown();
            }
        }

    }

}
