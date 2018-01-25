package uk.org.tombolo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py4j.GatewayServer;
import uk.org.tombolo.importer.PythonImporter;

/*
Py4jServer class opens a TCP connection to jvm 
that allows flow of objects between python and jvm
*/
public class Py4jServer {
    private static final Logger log = LoggerFactory.getLogger(Py4jServer.class);
    public static GatewayServer server;
    public static void main(String[] args) {
        server = new GatewayServer(new PythonImporter());
        server.start();
        log.info("Py4jServer has started!!!!");
    }
}
