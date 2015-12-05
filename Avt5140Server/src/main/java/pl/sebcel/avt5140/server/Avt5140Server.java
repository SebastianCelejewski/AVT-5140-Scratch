package pl.sebcel.avt5140.server;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import pl.sebcel.avt5140.driver.Avt5140Driver;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class Avt5140Server {

    private final static Logger log = Logger.getLogger(Avt5140Server.class);

    private Avt5140Driver driver = new Avt5140Driver();
    private Avt5140HttpHandler httpHandler;
    private boolean isInitialized = false;

    public static void main(String[] args) {
        new Avt5140Server().run();
    }

    public void run() {
        log.info("Starting AVT-5140 Server");

        try {
            driver.initialize("COM4");
            isInitialized = true;
            log.info("Successfully initialized AVT-5140 driver");
        } catch (Exception ex) {
            isInitialized = false;
            log.warn("Failed to initialize AVT-5140 driver: " + ex.getMessage(), ex);
        }

        httpHandler = new Avt5140HttpHandler(this, driver);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", httpHandler);
            server.setExecutor(null);
            server.start();

            log.info("Successfully initialized HTTP server");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to start AVT-5140 Server: " + ex.getMessage(), ex);
        }

        boolean enabled = true;
        while (enabled) {
        }
        driver.close();
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}