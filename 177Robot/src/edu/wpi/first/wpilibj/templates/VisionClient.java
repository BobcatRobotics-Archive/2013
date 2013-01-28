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
                DataInputStream is = sc.openDataInputStream();
                System.out.println("Connection To Vision System Established");

                //Make sure data is avaliable
                while(true) {
                    if(is.available() >= 8*3) {                       
                        distance = is.readDouble();
                        deltax = is.readDouble();
                        deltay = is.readDouble();
                        timeRecieved = Timer.getFPGATimestamp();
                        System.out.println("Vision Data Recived: distance: "+distance +" deltax: "+deltax+" deltay: " +deltay);
                        
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("VisionClient ERROR: " + e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    public synchronized double DataAge() {
        return Timer.getFPGATimestamp() - timeRecieved;
    }
}