package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py4j.GatewayServer;
import uk.org.tombolo.importer.PythonImporter;

public class Py4jServer {
    private static final Logger log = LoggerFactory.getLogger(Py4jServer.class);
    public static GatewayServer server;
    public static void main(String[] args) {
        server = new GatewayServer(new PythonImporter());
        server.start();
        log.info("Server has started!!!!");
        // System.out.println("Server has started!!!!");
    }
}