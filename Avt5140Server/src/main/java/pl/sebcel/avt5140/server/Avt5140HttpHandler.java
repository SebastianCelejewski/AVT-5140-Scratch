package pl.sebcel.avt5140.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pl.sebcel.avt5140.driver.Avt5140Driver;
import pl.sebcel.avt5140.server.utils.MorseEncoder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public class Avt5140HttpHandler implements HttpHandler {

    private final static Logger log = Logger.getLogger(Avt5140HttpHandler.class);

    private Avt5140Server avt5140Server;
    private Avt5140Driver avt5140Driver;
    private MorseEncoder morseEncoder;
    private Set<String> connectedClients = new HashSet<String>();

    public Avt5140HttpHandler(Avt5140Server avt5140Server, Avt5140Driver avt5140Driver, MorseEncoder morseEncoder) {
        this.avt5140Server = avt5140Server;
        this.avt5140Driver = avt5140Driver;
        this.morseEncoder = morseEncoder;
    }

    public void handle(HttpExchange t) throws IOException {
        try {
            String path = t.getRequestURI().getPath();
            String remoteAddress = t.getRemoteAddress().getHostName();

            if (path.equals("/poll")) {
                if (!connectedClients.contains(remoteAddress)) {
                    log.info("Registered client: " + remoteAddress);
                    connectedClients.add(remoteAddress);
                }
                returnString(t, "OK", true);
                return;
            }

            log.debug("Handle " + t.getRequestURI() + " from " + remoteAddress);

            if (path.equals("/scratch")) {
                returnFile(t, "/avt-5140-extension.json");
                return;
            }

            if (!avt5140Server.isInitialized()) {
                returnString(t, "AVT-5140 is not initialized", false);
                return;
            }

            if (path.startsWith("/morse/")) {
                handleMorseMessage(t, path);
                return;
            }

            String message = "";
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

            returnString(t, message, false);
        } catch (Exception ex) {
            returnError(t, ex);
        }
    }

    private void handleMorseMessage(HttpExchange t, String path) throws Exception {
        Pattern p = Pattern.compile("\\/morse\\/([\\d;]+)\\/((.+))");
        Matcher m = p.matcher(path);
        if (!m.matches()) {
            returnString(t, "Invalid format. Valid format is /morse/pin_specification/message where pin_specification is semicolon-delimited list of numbers", false);
            return;
        }

        String pinSpecification = m.group(1);
        String[] pinStrs = pinSpecification.split(";");
        int[] pinNumbers = new int[pinStrs.length];
        for (int i = 0; i < pinNumbers.length; i++) {
            pinNumbers[i] = Integer.parseInt(pinStrs[i]);
        }

        String message = m.group(2);
        System.out.println(message);

        String morseMessage = morseEncoder.encode(message);
        for (int i = 0; i < morseMessage.length(); i++) {
            char c = morseMessage.charAt(i);
            switch (c) {
            case ' ': {
                System.out.println("SPACJA");
                Thread.sleep(300);
                break;
            }
            case '.': {
                System.out.println(".");
                for (int pin = 0; pin < pinNumbers.length; pin++) {
                    avt5140Driver.write(pinNumbers[pin], true);
                }
                Thread.sleep(100);
                for (int pin = 0; pin < pinNumbers.length; pin++) {
                    avt5140Driver.write(pinNumbers[pin], false);
                }
                Thread.sleep(100);
                break;
            }
            case '-': {
                System.out.println("-");
                for (int pin = 0; pin < pinNumbers.length; pin++) {
                    avt5140Driver.write(pinNumbers[pin], true);
                }
                Thread.sleep(300);
                for (int pin = 0; pin < pinNumbers.length; pin++) {
                    avt5140Driver.write(pinNumbers[pin], false);
                }
                Thread.sleep(100);
                break;
            }
            }
        }
        returnString(t, "OK", false);
    }

    private void returnString(HttpExchange t, String message, boolean quiet) throws IOException {
        if (!quiet) {
            log.debug("Returning " + message);
        }
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