package edu.wpi.first.wpilibj.templates;

/*
 * VisionClient in Thread Form
 * Use to fetch and parse vision data
 */

import edu.wpi.first.wpilibj.Timer;
import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class VisionClient extends Thread {

    double distance, deltax, deltay;
    double timeRecieved;

    public void run() {
        while (true) {
            //If we get disconnected, try to reconnect
            try {
                /// Open socket
                SocketConnection sc = (SocketConnection) Connector.open("socket://10.1.77.91:10177");
                InputStream is = sc.openInputStream();
                byte[] buffer = new byte[1000];
                int byteCnt;
                int j;

                /// When data is available
                while ((byteCnt = is.read(buffer)) > 0) {
                    String data[] = new String[3];
                    int start = 0;
                    j = 0;
                    /// Split on commas - String.split not in FRC libraries
                    for (int i = 0; i < byteCnt && j < 3; i++) {
                        if (buffer[i] == ',' || i == byteCnt - 1) {
                            data[j++] = new String(buffer, start, i - 1);
                            start = i + 1;
                        }
                    }

                    if (j == 3) {
                        distance = Double.parseDouble(data[0]);
                        deltax = Double.parseDouble(data[1]);
                        deltay = Double.parseDouble(data[2]);

                        //Keep track of the time so we know how old the data is.
                        timeRecieved = Timer.getFPGATimestamp();
                    } else {
                        System.out.println("Error Parsing Vision Packet: " + buffer);
                    }
                }

            } catch (IOException e) {
                System.out.println("VisionClient ERROR: " + e);
            }
        }
    }

    public synchronized double DataAge() {
        return Timer.getFPGATimestamp() - timeRecieved;
    }
}