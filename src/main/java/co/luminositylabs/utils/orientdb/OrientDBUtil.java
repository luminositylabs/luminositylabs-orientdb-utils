package co.luminositylabs.utils.orientdb;


import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A utility class to support development with OrientDB.
 *
 * @author Phillip Ross
 */
public class OrientDBUtil {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(OrientDBUtil.class);

    /** Property name constant for specification of embedded OrientDB server. */
    public static final String PROPERTY_NAME_ODB_EMBEDDED = "orientdb.embedded";

    /** Property name constant for specification of the OrientDB server engine. */
    public static final String PROPERTY_NAME_ODB_SERVER_ENGINE = "orientdb.server.engine";

    /** Property name constant for specification of the hostname of the remote OrientDB server. */
    public static final String PROPERTY_NAME_ODB_SERVER_REMOTE_HOSTNAME = "orientdb.server.remote.hostname";

    /** Property name constant for specification of the port range of the remote OrientDB server. */
    public static final String PROPERTY_NAME_ODB_SERVER_REMOTE_PORT_RANGE = "orientdb.server.remote.portRange";

    /** Property name constant for specification of the OrientDB database path. */
    public static final String PROPERTY_NAME_ODB_DATABASE_PATH = "orientdb.databasePath";

    /** Property name constant for specification of the OrientDB database name. */
    public static final String PROPERTY_NAME_ODB_DATABASE_NAME = "orientdb.databaseName";

    /** Property name constant for specification of the username for the OrientDB server. */
    public static final String PROPERTY_NAME_ODB_USERNAME = "orientdb.server.username";

    /** Property name constant for specification of the password for the OrientDB server. */
    public static final String PROPERTY_NAME_ODB_PASSWORD = "orientdb.server.password";

    /** The default port range of the OrientDB server's binary protocol listener. */
    public static final String DEFAULT_BINARY_PROTOCOL_PORTRANGE = "2424-2430";


    /** Enumerates the supported OrientDB engines. */
    public enum Engine {

        /** "paged local" engine. */
        PLOCAL("plocal"),
        /** The remote engine. */
        REMOTE("remote");

        /** String representation of the enumerated value. */
        private String stringValue;

        /**
         * Constructor allowing the string value to be specified.
         *
         * @param stringValue string value for the enumerated value
         *
         */
        Engine(final String stringValue) {
            this.stringValue = stringValue;
        }

        /**
         * Returns the string value equivalent to the enumerated value.
         *
         * @return the string value
         */
        public String toStringValue() {
            return stringValue;
        }

        /**
         * Returns the enumerated value equivalent to the specified engine name.
         *
         * @param engineName the engine name
         * @return the enumerated value
         */
        public static Engine fromString(final String engineName) {
            Engine engine = null;
            if (engineName != null) {
                for (Engine validEngine : Engine.values()) {
                    if (engineName.equalsIgnoreCase(validEngine.stringValue)) {
                        engine = validEngine;
                    }
                }
            }
            return engine;
        }
    }


    /** Non-public default constrictor. */
    protected OrientDBUtil() {
    }


    /**
     * Builds a database url that can be used to connect to OrientDB.
     *
     * @param databaseName the name of the database
     * @param engine the engine to be used
     * @param hostname the hostname of the OrientDB server
     * @param portRange the port range of the OrientDB server
     * @param databasePath the path of the database
     * @return the properly formatted url to use for connecting to OrientDB
     */
    public static String buildDatabaseUrl(final String databaseName,
                                          final Engine engine,
                                          final String hostname,
                                          final String portRange,
                                          final String databasePath) {
        Objects.requireNonNull(databaseName, "A  databaseName must be provided");
        Objects.requireNonNull(engine, "An engine must be provided");
        StringBuilder databaseUrlStringBuilder = new StringBuilder(engine.toStringValue());
        databaseUrlStringBuilder.append(":");
        if (engine.equals(Engine.REMOTE)) {
            Objects.requireNonNull(hostname, "When remote engine is used, a hostname must be provided");
            databaseUrlStringBuilder.append(hostname);
            if (portRange != null) {
                int endIndex = portRange.length();
                int rangeSeparatorIdx = portRange.indexOf("-");
                if (rangeSeparatorIdx != -1) {
                    endIndex = rangeSeparatorIdx;
                }
                String parsablePortRangeString = portRange.substring(0, endIndex);
                int port;
                try {
                    port = Integer.parseInt(parsablePortRangeString);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(
                            "portRange must be a string representing an integer value or an integer range",
                            nfe
                    );
                }
                databaseUrlStringBuilder.append(":")
                        .append(port);
            }
        }
        if (databasePath != null) {
            databaseUrlStringBuilder.append(databasePath);
            if (!databasePath.endsWith("/")) {
                databaseUrlStringBuilder.append("/");
            }
        } else {
            databaseUrlStringBuilder.append("/");
        }
        databaseUrlStringBuilder.append(databaseName);
        return databaseUrlStringBuilder.toString();
    }


    /**
     * Starts an embedded OrientDB server.
     *
     * @param username the username to be used in configuring the OrientDB server
     * @param password the password to be used in configuring the OrientDB server
     * @param portRange the port range the OrientDB server will listen on
     * @return a reference to the embedded server
     *
     * @throws Exception when server is unable to be configured or activated
     */
    public static OServer startEmbeddedServer(final String username,
                                              final String password,
                                              final String portRange) throws Exception {
        logger.debug("Starting embedded OrientDB server");
        Objects.requireNonNull(username, "Username must be specified");
        Objects.requireNonNull(password, "Password must be specified");
        String embeddedServerPortRange = DEFAULT_BINARY_PROTOCOL_PORTRANGE;
        if (portRange != null) {
            embeddedServerPortRange = portRange;
        }
        OServer oServer = OServerMain.create();
        OServerConfiguration serverConfig = new OServerConfiguration();
        serverConfig.users = new OServerUserConfiguration[] {
                new OServerUserConfiguration("root", "password", "*"),
                new OServerUserConfiguration(username, password, "*")
        };
        serverConfig.network = new OServerNetworkConfiguration();
        serverConfig.network.listeners = new ArrayList<>();
        OServerNetworkListenerConfiguration networkListenerConfig = new OServerNetworkListenerConfiguration();
        networkListenerConfig.ipAddress = "0.0.0.0"; // NOPMD - It's fine to use this IP to bind to all addresses.
        networkListenerConfig.portRange = embeddedServerPortRange;
        serverConfig.network.listeners.add(networkListenerConfig);
        serverConfig.network.protocols = new ArrayList<>();
        OServerNetworkProtocolConfiguration protocolConfig = new OServerNetworkProtocolConfiguration(
                "binary",
                "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary"
        );
        serverConfig.network.protocols.add(protocolConfig);

        serverConfig.properties = new OServerEntryConfiguration[] {
                new OServerEntryConfiguration("server.cache.staticResources", "false"),
                new OServerEntryConfiguration("server.database.path", "target/dbs"),
                new OServerEntryConfiguration("plugin.dynamic", "false")
        };
        oServer.startup(serverConfig);
        oServer.activate();
        return oServer;
    }


}
