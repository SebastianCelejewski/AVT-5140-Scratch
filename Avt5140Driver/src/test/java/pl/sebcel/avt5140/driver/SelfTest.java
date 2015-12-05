package pl.sebcel.avt5140.driver;

import org.junit.Test;

public class SelfTest {

    @Test
    public void selfTest() {
        Avt5140Driver driver = new Avt5140Driver();
        driver.initialize("COM4");
        driver.write(0, true);
        sleep(1000);
        driver.write(0, false);
    }

    private void sleep(int delayInMilliseconds) {
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException ex) {
            // intentional
        }
    }
}