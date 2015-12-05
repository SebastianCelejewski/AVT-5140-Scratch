package pl.sebcel.avt5140.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pl.sebcel.avt5140.driver.Avt5140Driver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class Avt5140HttpHandler implements HttpHandler {

    private Avt5140Server avt5140Server;
    private Avt5140Driver avt5140Driver;

    public Avt5140HttpHandler(Avt5140Server avt5140Server, Avt5140Driver avt5140Driver) {
        this.avt5140Server = avt5140Server;
        this.avt5140Driver = avt5140Driver;
    }

    public void handle(HttpExchange t) throws IOException {
        try {
            System.out.println("Handle " + t.getRequestURI() + " from " + t.getRemoteAddress().getHostName());
            String path = t.getRequestURI().getPath();

            if (path.equals("/scratch")) {
                returnFile(t, "/avt-5140-extension.json");
            }

            if (avt5140Server.isInitialized()) {
                if (path.contains("/0/on")) {
                    avt5140Driver.write(0, true);
                }
                if (path.contains("/0/off")) {
                    avt5140Driver.write(0, false);
                }
            }

            returnString(t, "OK");
        } catch (Exception ex) {
            returnError(t, ex);
        }
    }

    private void returnString(HttpExchange t, String string) throws IOException {
        byte[] messageBytes = string.getBytes();
        t.sendResponseHeaders(200, messageBytes.length);
        t.setAttribute("Content-Type", "text/html");
        OutputStream os = t.getResponseBody();
        os.write(messageBytes);
        os.close();
    }

    private void returnFile(HttpExchange t, String filePath) throws IOException {
        byte[] messageBytes = loadFile(filePath);
        t.sendResponseHeaders(200, messageBytes.length);
        t.setAttribute("Content-Type", "text/html");
        OutputStream os = t.getResponseBody();
        os.write(messageBytes);
        os.close();
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

    private void returnError(HttpExchange t, Exception ex) {
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
}