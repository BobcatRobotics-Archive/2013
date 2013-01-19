
package edu.wpi.first.wpilibj.templates;

import com.sun.squawk.util.Arrays;
import edu.wpi.first.wpilibj.Timer;
import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.StreamConnection;


/**
 *
 * Create a thread that listens on UDP port 177 for data from the vision system
 * @author schroed
 */
public class VisionListener extends Thread {

    double distance;
    double timeRecieved;

    public VisionListener()  {

    }

    public synchronized double DataAge() {
        return Timer.getFPGATimestamp() - timeRecieved;
    }

    public void run() {

        while(true) { //allow the connection to be reestabilished if lost
            try {

                ServerSocketConnection  ssc = (ServerSocketConnection ) Connector.open("socket://:177");
                StreamConnection sock = ssc.acceptAndOpen();
                System.out.println("Connection Established");
                DataInputStream in = sock.openDataInputStream();
                byte[] data = new byte[1024];

                while(true) {
                    int cnt = in.read(data,0,1024);
                
                    //Keep track of the time so we know how old the data is.
                    timeRecieved = Timer.getFPGATimestamp();

                    /* Parse datagram here */
                    String temp = new String(data);
                    System.out.println(cnt + ": " + temp);                    
                }

            } catch (IOException e) {
                System.out.println("Error in VisionListener: " + e);
            }
        }
    }



}
