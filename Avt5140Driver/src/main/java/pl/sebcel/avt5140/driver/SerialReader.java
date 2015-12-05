package pl.sebcel.avt5140.driver;

import java.io.IOException;
import java.io.InputStream;

public class SerialReader implements Runnable {

    private InputStream in;

    private boolean enabled;

    private boolean quiet;

    public SerialReader(InputStream in) {
        this.in = in;
        this.enabled = true;
        this.quiet = false;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = this.in.read(buffer)) > -1 && enabled) {
                if (!quiet) {
                    System.out.print(new String(buffer, 0, len));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from input stream: " + e.getMessage(), e);
        }
    }

    public void close() {
        this.enabled = false;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
}