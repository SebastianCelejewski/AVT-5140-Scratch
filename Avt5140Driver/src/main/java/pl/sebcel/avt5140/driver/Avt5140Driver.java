package pl.sebcel.avt5140.driver;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

public class Avt5140Driver {

    private final static Logger log = Logger.getLogger(Avt5140Driver.class);

    private SerialPort serialPort;
    private PrintWriter out;
    private SerialReader reader;

    public void initialize(String portName) {
        try {
            log.info("Connecting to " + portName);
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            Thread.sleep(1000);

            if (portIdentifier.isCurrentlyOwned()) {
                throw new RuntimeException("Port " + portName + " is currently in use by " + portIdentifier.getCurrentOwner());
            }

            log.info("Setting communcation parameters");

            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            OutputStream os = serialPort.getOutputStream();
            InputStream is = serialPort.getInputStream();
            out = new PrintWriter(os);
            reader = new SerialReader(is);
            reader.setQuiet(true);
            (new Thread(reader)).start();

            log.info("Resetting controller");
            out.println("@");
            out.flush();

            out.println("FF DDRB |");
            out.flush();

            log.info("Initialization complete.");
        } catch (Exception ex) {
            throw new RuntimeException("Initialization failed: " + ex.getMessage(), ex);
        }
    }

    public void close() {
        log.info("Shutdown started");
        reader.close();

        if (out != null) {
            log.info("Closing output stream.");
            out.close();
        }

        if (serialPort != null) {
            log.info("Releasing serial port.");
            serialPort.close();
        }

        log.info("Shutdown complete");
    }

    public void write(int portIdx, boolean value) {
        log.debug("Writing value " + value + " to pin " + portIdx);
        int bitmask = 1 << portIdx;
        if (value) {
            or(bitmask);
        } else {
            and(~bitmask);
        }
    }

    private void or(int value) {
        String string = hex(value);
        String command = string + " portb |";
        log.debug(command);
        out.println(command);
        out.flush();
    }

    private void and(int value) {
        String string = hex(value);
        String command = string + " portb &";
        log.debug(command);
        out.println(command);
        out.flush();
    }

    private String hex(int value) {
        String string = Integer.toHexString(value);
        if (string.length() == 1) {
            string = "0" + string;
        }
        return string;
    }
}