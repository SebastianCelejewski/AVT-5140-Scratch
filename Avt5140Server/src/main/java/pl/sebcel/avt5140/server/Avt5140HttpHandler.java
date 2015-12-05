package pl.sebcel.avt5140.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pl.sebcel.avt5140.driver.Avt5140Driver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class Avt5140HttpHandler implements HttpHandler {

    private final static Logger log = Logger.getLogger(Avt5140HttpHandler.class);

    private Avt5140Server avt5140Server;
    private Avt5140Driver avt5140Driver;

    public Avt5140HttpHandler(Avt5140Server avt5140Server, Avt5140Driver avt5140Driver) {
        this.avt5140Server = avt5140Server;
        this.avt5140Driver = avt5140Driver;
    }

    public void handle(HttpExchange t) throws IOException {
        try {
            log.debug("Handle " + t.getRequestURI() + " from " + t.getRemoteAddress().getHostName());
            String path = t.getRequestURI().getPath();

            if (path.equals("/scratch")) {
                returnFile(t, "/avt-5140-extension.json");
                return;
            }

            String message = "";

            if (avt5140Server.isInitialized()) {
                Pattern p = Pattern.compile("\\/(\\w+)\\/((\\d+))");
                Matcher m = p.matcher(path);
                if (m.matches()) {
                    String command = m.group(1);
                    int outputNumber = Integer.parseInt(m.group(2));
                    if (command.equals("on")) {
                        avt5140Driver.write(outputNumber, true);
                        message = "OK";
                    } else if (command.equals("off")) {
                        avt5140Driver.write(outputNumber, false);
                        message = "OK";
                    } else {
                        message = "Invalid command: " + command + ". Recognized commands: on, off.";
                    }
                } else {
                    message = "Invalid URL. Recognized URL format: /[on|off]/outputNumber";
                }
            } else {
                message = "AVT-5140 is not initialized";
            }

            returnString(t, message);
        } catch (Exception ex) {
            returnError(t, ex);
        }
    }

    private void returnString(HttpExchange t, String message) throws IOException {
        log.debug("Returning " + message);
        byte[] messageBytes = message.getBytes();
        t.sendResponseHeaders(200, messageBytes.length);
        t.setAttribute("Content-Type", "text/html");
        OutputStream os = t.getResponseBody();
        os.write(messageBytes);
        os.close();
    }

    private void returnFile(HttpExchange t, String filePath) throws IOException {
        log.debug("Returning file " + filePath);
        byte[] messageBytes = loadFile(filePath);
        t.sendResponseHeaders(200, messageBytes.length);
        t.setAttribute("Content-Type", "text/html");
        OutputStream os = t.getResponseBody();
        os.write(messageBytes);
        os.close();
    }

    private void returnError(HttpExchange t, Exception ex) {
        log.debug("Returning error " + ex.getMessage());
        try {
            byte[] messageBytes = ("Internal Server Error\n" + ex.getMessage()).getBytes();
            t.sendResponseHeaders(500, messageBytes.length);
            t.setAttribute("Content-Type", "text/html");
            OutputStream os = t.getResponseBody();
            os.write(messageBytes);
            os.close();
        } catch (Exception e) {
            ex.printStackTrace();
            e.printStackTrace();
        }
    }

    private byte[] loadFile(String filePath) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = this.getClass().getResourceAsStream(filePath);
            if (in == null) {
                throw new RuntimeException("File " + filePath + " does not exist in the classpath");
            }
            int readBytes = 0;
            byte[] buffer = new byte[1024];
            do {
                readBytes = in.read(buffer);
                if (readBytes > 0) {
                    out.write(buffer, 0, readBytes);
                }
            } while (readBytes > 0);
            in.close();
            out.close();
            return out.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load file " + filePath + ": " + ex.getMessage(), ex);
        }
    }
}